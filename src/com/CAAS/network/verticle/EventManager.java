package com.CAAS.network.verticle;

import com.CAAS.data.CameraNode;
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

            int idx = CameraNode.findPort(Integer.parseInt(message.headers().get("port")));
            if(idx!=-1) {
                System.out.println(message.headers().get("port") + "포트 노드로부터 RouteBlock 수신");
                CameraNode.getInstance().get(idx).addBlock(msg);
            }

            try {
                blockChain.pushBlock(block);
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
            int idx = CameraNode.findPort(Integer.parseInt(message.headers().get("port")));
            if(idx!=-1) {
                CameraNode.getInstance().get(idx).addBlock(msg);
                System.out.println(message.headers().get("port") + "포트 노드로부터 DataBlock 수신");
            }

            try {
                blockChain.pushBlock(block);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        });

        eventBus.consumer("location_info",message->{
            int port = Integer.parseInt(message.headers().get("port"));
            ChainMessageProtocol body = (ChainMessageProtocol) message.body();
            NetSocket socket = global.socketList.get(port);
            socket.write(body.encode());
        });

        eventBus.consumer("activate_node",message->{
            int port = Integer.parseInt(message.headers().get("port"));
            ChainMessageProtocol body = (ChainMessageProtocol) message.body();
            NetSocket socket = global.socketList.get(port);
            socket.write(body.encode());
        });

        eventBus.consumer("deactivate_node",message->{
            int port = Integer.parseInt(message.headers().get("port"));
            ChainMessageProtocol body = (ChainMessageProtocol) message.body();
            NetSocket socket = global.socketList.get(port);
            socket.write(body.encode());
        });

        eventBus.consumer("rotate_node",message->{
            int port = Integer.parseInt(message.headers().get("port"));
            ChainMessageProtocol body = (ChainMessageProtocol) message.body();
            NetSocket socket = global.socketList.get(port);
            socket.write(body.encode());
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
