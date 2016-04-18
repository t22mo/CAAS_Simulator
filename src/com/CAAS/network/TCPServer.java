package com.CAAS.network;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;

public class TCPServer {
    Vertx vertx;
    Global global = Global.getInstance();

    public TCPServer(Vertx vertx){
        this.vertx =vertx;
        DeploymentOptions options = new DeploymentOptions().setWorker(true).setInstances(10);
        vertx.deployVerticle("com.CAAS.network.SubscribeWorker",options);
    }

    public void createServer() {
        NetServer server = vertx.createNetServer();
        // 커넥션 요청 핸들러
        server.connectHandler(socket -> {
            ClientConnection clientNode = new ClientConnection(socket, vertx);
            clientNode.init();
        });

        server.listen(1058, "localhost", res -> {
            if (res.succeeded()) {
                System.out.println("TCPServer Start on port 1058");
            } else {
                System.out.println("Port Binding Failed");
            }
        });
    }

}
