package com.CAAS.network;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

/**
 * Created by tokirin on 2016-04-18.
 */

public class HashChainCodec implements MessageCodec<ChainMessageProtocol,ChainMessageProtocol> {

    @Override
    public void encodeToWire(Buffer buffer, ChainMessageProtocol chainMessageProtocol) {

        String jsonToStr = chainMessageProtocol.encode();
        int length = jsonToStr.getBytes().length;

        buffer.appendInt(length);
        buffer.appendString(jsonToStr);
    }

    @Override
    public ChainMessageProtocol decodeFromWire(int pos, Buffer buffer) {
        int _pos = pos;
        int length = buffer.getInt(_pos);

        // Get JSON string by it`s length
        // Jump 4 because getInt() == 4 bytes
        String jsonStr = buffer.getString(_pos+=4, _pos+=length);
        JsonObject message = new JsonObject(jsonStr);

        return new ChainMessageProtocol(message);

    }

    @Override
    public ChainMessageProtocol transform(ChainMessageProtocol chainMessageProtocol) {
        return chainMessageProtocol;
    }

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
