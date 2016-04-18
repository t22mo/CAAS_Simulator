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
            this.camNode = global.camList.get(id);
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
            System.out.println("Client Node on id " + camNode.id + " is connected");
        }catch(EmptyStackException e){
            // 커넥션 풀 다찼으므로 거부
            ChainMessageProtocol msg = new ChainMessageProtocol("simulator_connection_refuse");
            socket.write(msg.encode());
            System.out.println("Client Node is refused because of full of connection");
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
        });
    }


}
