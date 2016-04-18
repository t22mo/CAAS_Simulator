package com.CAAS.network;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * JSON ChainMessageProtocol Protocol
 * {type: <String> ,
 *  data: <JsonObject>}
 * 타입별 데이터 포맷은 Connection 참조
 */

public class ChainMessageProtocol {
    JsonObject message = new JsonObject();
    JsonObject data = new JsonObject();

    public ChainMessageProtocol(String type){
        message.put("type",type);
    }


    public ChainMessageProtocol(JsonObject object){
        this.message = object;
        this.data = object.getJsonObject("data");
    }


    public String getType(){
        return message.getString("type");
    }

    public JsonObject getMessage(){
        return this.message;
    }

    public JsonObject getData(){
        return message.getJsonObject("data");
    }

    public JsonObject put(String key, String value){
        data.put(key,value);
        return data;
    }

    public JsonObject put(String key, double value){
        data.put(key,value);
        return data;
    }

    public JsonObject put(String key, boolean value){
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
