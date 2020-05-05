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

package http2.client.reproducer.ws;

import io.reactivex.Completable;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint;

import static io.vertx.core.http.HttpMethod.GET;

public class WebServiceVerticle extends AbstractVerticle {

    @Override
    public Completable rxStart() {
        ServiceDiscovery serviceDiscovery = ServiceDiscovery.create(vertx);

        Router router = Router.router(vertx);
        router.route("/ws").method(GET).produces("application/json").handler(new WebServiceHandler());

        HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setUseAlpn(true)
                .setCompressionSupported(true);

        return vertx.createHttpServer(httpServerOptions)
                .requestHandler(router)
                .rxListen(8181)
                .flatMap(ok -> serviceDiscovery.rxPublish(HttpEndpoint.createRecord("WEB_SERVICE", "localhost", 8181, "/ws")))
                .ignoreElement();
    }
}
