package com.CAAS.data;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * JSON Message Protocol
 * {type: <String> ,
 *  data: <JsonObject>}
 * 타입별 데이터 포맷은 Connection 참조
 */

public class Message {
    JsonObject message = new JsonObject();
    JsonObject data = new JsonObject();

    public Message(String type){
        message.put("type",type);
    }

    public JsonObject put(String key, String value){
        data.put(key,value);
        return data;
    }

    public JsonObject put(String key, int value){
        data.put(key,value);
        return data;
    }

    public JsonObject put(String key, JsonObject value){
        data.put(key,value);
        return data;
    }

    public JsonObject put(String key, JsonArray value){
        data.put(key,value);
        return data;
    }

    public String encode(){
        message.put("data",data);
        return message.encode();
    }
}
