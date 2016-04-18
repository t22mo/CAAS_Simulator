package com.CAAS.network;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;

/**
 * Created by tokirin on 2016-04-18.
 */
public class SubscribeWorker extends AbstractVerticle {
    Global global = Global.getInstance();

    @Override
    public void start() throws Exception {
        System.out.println("asdfasdf");
        Global global = Global.getInstance();
        EventBus eventBus = getVertx().eventBus();

        eventBus.consumer("registering_client", message ->{
            int port = Integer.parseInt(message.headers().get("port"));
            ChainMessageProtocol msg = (ChainMessageProtocol) message.body();

        });

    }

    @Override
    public void stop() throws Exception {

    }

    public NetSocket getSocket(Message<Object> msg){
        int port = Integer.parseInt(msg.headers().get("port"));
        return global.socketList.get(port);
    }
}
