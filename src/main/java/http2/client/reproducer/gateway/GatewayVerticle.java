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

package http2.client.reproducer.gateway;

import http2.client.reproducer.WebClientProvider;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;

import static io.vertx.core.http.HttpMethod.GET;

public class GatewayVerticle extends AbstractVerticle {

    @Override
    public Completable rxStart() {
        ServiceDiscovery serviceDiscovery = ServiceDiscovery.create(vertx);
        WebClientProvider webClientProvider = WebClientProvider.create(serviceDiscovery);

        return Single.zip(webClientProvider.getHttp1WebClient(), webClientProvider.getHttp2WebClient(), (webClientHttp1, webClientHttp2) -> {
            Router router = Router.router(vertx);
            router.route("/wsHttp1").method(GET).handler(new GatewayHandler(webClientHttp1));
            router.route("/wsHttp2").method(GET).handler(new GatewayHandler(webClientHttp2));
            return router;
        }).flatMapCompletable(router -> {
            HttpServerOptions httpServerOptions = new HttpServerOptions()
                    .setUseAlpn(true)
                    .setCompressionSupported(true);

            return vertx.createHttpServer(httpServerOptions)
                    .requestHandler(router)
                    .rxListen(8080)
                    .ignoreElement();
        });
    }
}
