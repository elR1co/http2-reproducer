/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

package http2.client.reproducer;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class WebClientProvider {

    private WebClient wsHttp1WebClient;
    private WebClient wsHttp2WebClient;

    private final ServiceDiscovery serviceDiscovery;

    private static final JsonObject http2Options = new JsonObject()
            .put("idleTimeout", 1)
            .put("idleTimeoutUnit", TimeUnit.MINUTES.toString())
            .put("protocolVersion", "HTTP_2")
            .put("http2ClearTextUpgrade", false)
            .put("http2MaxPoolSize", 5);

    private static final JsonObject defaultOptions = new JsonObject()
            .put("idleTimeout", 1)
            .put("idleTimeoutUnit", TimeUnit.MINUTES.toString());

    public static WebClientProvider create(ServiceDiscovery serviceDiscovery) {
        return new WebClientProvider(serviceDiscovery);
    }

    private WebClientProvider(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public Completable stop() {
        return Completable.mergeArray(
                Completable.fromAction(() -> Optional.ofNullable(wsHttp1WebClient).ifPresent(this::releaseWebClient)),
                Completable.fromAction(() -> Optional.ofNullable(wsHttp2WebClient).ifPresent(this::releaseWebClient))
        );
    }

    public Single<WebClient> getHttp1WebClient() {
        return wsHttp1WebClient != null ?
                Single.just(wsHttp1WebClient) :
                getWebClient("WEB_SERVICE", defaultOptions)
                        .doOnSuccess(obtainedWebClient -> wsHttp1WebClient = obtainedWebClient);
    }

    public Single<WebClient> getHttp2WebClient() {
        return wsHttp2WebClient != null ?
                Single.just(wsHttp2WebClient) :
                getWebClient("WEB_SERVICE", http2Options)
                        .doOnSuccess(obtainedWebClient -> wsHttp2WebClient = obtainedWebClient);
    }

    Single<WebClient> getWebClient(String serviceName, JsonObject webClientOptions) {
        return HttpEndpoint.rxGetWebClient(serviceDiscovery, record -> record.getName().equals(serviceName), webClientOptions);
    }

    void releaseWebClient(WebClient client) {
        ServiceDiscovery.releaseServiceObject(serviceDiscovery, client);
    }
}
