package com.CAAS.network.verticle;

import com.CAAS.data.CameraNode;
import com.CAAS.network.model.Global;
import com.CAAS.network.protocol.ChainMessageProtocol;
import com.CAAS.network.protocol.HashChainCodec;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;

import java.util.EmptyStackException;

public class SimulatorManager extends AbstractVerticle {
    Vertx vertx;
    Global global;
    EventBus eventBus;
    HashChainCodec myCodec = new HashChainCodec();
    @Override
    public void start(){
        global = Global.getInstance();
        this.vertx = getVertx();
        eventBus = vertx.eventBus();

        DeploymentOptions options = new DeploymentOptions().setWorker(true).setInstances(10);
        vertx.deployVerticle("com.CAAS.network.verticle.EventManager",options);
        createServer();

    }


    public void createServer() {
        NetServerOptions serverOptions = new NetServerOptions().setReceiveBufferSize(1024*1024).setSendBufferSize(1024*1024);
        NetServer server = vertx.createNetServer(serverOptions);
        // 커넥션 요청 핸들러
        server.connectHandler(socket -> {
            int id = global.availableCameraNodeID.pop();
            CameraNode camNode = global.camList.get(id-1);
            try {
                // 커넥션 요청에 대한 Ack Message 전송
                ChainMessageProtocol msg = new ChainMessageProtocol("simulator_connection_ok");
                msg.put("id",camNode.id);
                msg.put("location_x",camNode.pos.x);
                msg.put("location_y",camNode.pos.y);
                msg.put("active",camNode.active);
                msg.put("vAngle",camNode.vAngle);
                msg.put("vDis",camNode.vDis);
                msg.put("port",camNode.port);
                socket.write(msg.encode());

                // 전역 socket 리스트에 추가
                global.socketList.put(camNode.port,socket);
                System.out.println("클라이언트 (ID : " + camNode.id + ")가 연결 되었습니다.");
            }catch(EmptyStackException e){
                // 커넥션 풀 다찼으므로 거부
                ChainMessageProtocol msg = new ChainMessageProtocol("simulator_connection_refuse");
                socket.write(msg.encode());
                System.out.println("클라이언트 노드가 꽉 찼습니다. 연결을 진행 할 수 없습니다.");
            }

            // Data Handler
            socket.handler(buffer -> {
                JsonObject object = new JsonObject(buffer.getString(0,buffer.length()));
                ChainMessageProtocol msg = new ChainMessageProtocol(object);
                DeliveryOptions options = new DeliveryOptions()
                        .setCodecName(myCodec.name())
                        .addHeader("port", String.valueOf(camNode.port));
                eventBus.send(msg.getType(), msg, options);
            });

            socket.closeHandler(v -> {
                global.socketList.remove(camNode.port);
                global.availableCameraNodeID.push(camNode.id);
                System.out.println("ID" + camNode.id + " 클라이언트 노드 연결이 끊겼습니다. 해당 카메라 노드가 다른 클라이언트에 할당됩니다.");
            });
        });

        server.listen(1058, "localhost", res -> {
            if (res.succeeded()) {
                System.out.println("SimulatorManager Start on port 1058");
            } else {
                System.out.println("Port Binding Failed");
            }
        });
    }

}
