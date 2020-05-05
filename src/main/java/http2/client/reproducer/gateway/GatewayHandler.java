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

import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.client.predicate.ResponsePredicate;
import io.vertx.reactivex.ext.web.codec.BodyCodec;

public class GatewayHandler implements Handler<RoutingContext> {

    private final WebClient webClient;

    public GatewayHandler(WebClient webClient) {
        this.webClient = webClient;
    }

    public void handle(RoutingContext rc) {
        HttpServerRequest httpServerRequest = rc.request();
        HttpServerResponse httpServerResponse = rc.response().setChunked(true);
        Single.just(httpServerRequest)
                .map(HttpServerRequest::pause)
                .flatMap(ok -> {

                    return webClient
                                .get("/ws")
                                .as(BodyCodec.pipe(httpServerResponse))
                                .expect(ResponsePredicate.SC_OK)
                                .rxSend();
                })
                .doFinally(() -> {
                    if (!httpServerRequest.isEnded()) {
                        httpServerRequest.resume();
                    }
                })
                .subscribe(
                        response -> System.out.println("Ws call successful"),
                        error -> {
                            System.err.println("Ws call failed");
                            error.printStackTrace();
                            if (!httpServerResponse.ended()) {
                                httpServerResponse.end();
                            }
                        }
                );
    }
}
