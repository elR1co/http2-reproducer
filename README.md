## Vert.x HTTP/2 Client problem reproducer

1. Start application in debug mode in IntelliJ :

MainClass : io.vertx.core.Launcher  
Program arguments : run http2.client.reproducer.ReproducerMainVerticle

2. Put a breakpoint on the first line of WebServiceHandler.java handle method

2. Do a GET request in your browser for HTTP/2 client :

http://localhost:8080/wsHttp2

3. When you are blocked on the breakpoint, close the browser tab to simulate a connection crash, remove breakpoint and resume IDE debugger

4. Try another GET request in your browser for HTTP/2 client :

http://localhost:8080/wsHttp2

5. Then your request should be pending until the idle timeout release the connection

6. You can also try to reproduce with HTTP/1 client, but the requests won't be pending

http://localhost:8080/wsHttp1
