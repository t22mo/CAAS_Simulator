package com.CAAS.network;

import com.CAAS.data.CameraNode;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;

import java.util.EmptyStackException;

/**
 * Created by tokirin on 2016-04-18.
 */
public class ClientConnection {
    Global global = Global.getInstance();
    NetSocket socket;
    CameraNode camNode;
    EventBus eventBus;
    HashChainCodec myCodec = new HashChainCodec();

    public ClientConnection(NetSocket socket, Vertx vertx){
        this.socket = socket;
        this.eventBus = vertx.eventBus();
    }

    public void init(){
        //ClientConnection 초기화
        try {

            // 커넥션 요청에 대한 Ack Message 전송
            int id = global.availableCameraNodeID.pop();

            this.camNode = global.camList.get(id-1);
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
            eventBus.registerCodec(myCodec);
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
    }


}
