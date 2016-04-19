package com.CAAS.network.verticle;

import com.CAAS.network.model.Global;
import com.CAAS.network.model.block.BlockChain;
import com.CAAS.network.model.block.DataBlock;
import com.CAAS.network.model.block.RouteBlock;
import com.CAAS.network.protocol.ChainMessageProtocol;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;

import java.security.NoSuchAlgorithmException;

/**
 * Created by tokirin on 2016-04-18.
 */
public class EventManager extends AbstractVerticle {
    Global global = Global.getInstance();
    BlockChain blockChain = BlockChain.getInstance();
    @Override
    public void start() throws Exception {
        Global global = Global.getInstance();
        EventBus eventBus = getVertx().eventBus();

        /*
         * 라우트 블럭 생성 요청 handler
         */
        eventBus.consumer("push_route_block",message->{
            ChainMessageProtocol msg = (ChainMessageProtocol) message.body();
            RouteBlock block = new RouteBlock(msg.getData().getJsonObject("block"));
            try {
                if(blockChain.pushBlock(block)){
                    int port = Integer.parseInt(message.headers().get("port"));
                    ChainMessageProtocol mail = new ChainMessageProtocol("push_route_block");
                    mail.put("block",block.getObject());
                    global.socketList.forEach((k,v)->{
                        if(k != port){
                            v.write(mail.encode());
                        }
                    });
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        });

        /*
         * 데이터 블럭 생성 요청 handler
         */
        eventBus.consumer("push_data_block",message->{
            ChainMessageProtocol msg = (ChainMessageProtocol) message.body();
            DataBlock block = new DataBlock(msg.getData().getJsonObject("block"));
            try {
                if(blockChain.pushBlock(block)) {
                    int port = Integer.parseInt(message.headers().get("port"));
                    ChainMessageProtocol mail = new ChainMessageProtocol("push_data_block");
                    mail.put("block", block.getObject());
                    global.socketList.forEach((k, v) -> {
                        if (k != port) {
                            v.write(mail.encode());
                        }
                    });
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        });

        eventBus.consumer("test_push_data_block",message->{
            JsonObject blockObj = new JsonObject();
            JsonObject headerObj = new JsonObject();
            JsonObject bodyObj = new JsonObject();
            try {
                headerObj.put("blockHash",blockChain.getCurrentBlockHash());
                headerObj.put("blockID",blockChain.blockChain.size());
                headerObj.put("blockType","data");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            blockObj.put("header",headerObj);
            bodyObj.put("something","happened");
            blockObj.put("body",bodyObj);

            DataBlock dataBlock = new DataBlock(blockObj);

            try {
                if(blockChain.pushBlock(dataBlock)){
                    System.out.println(global.socketList.toString());
                    NetSocket socket = global.socketList.get(1008);
                    ChainMessageProtocol mail = new ChainMessageProtocol("push_data_block");
                    mail.put("block", dataBlock.getObject());
                    socket.write(mail.encode());
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
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
