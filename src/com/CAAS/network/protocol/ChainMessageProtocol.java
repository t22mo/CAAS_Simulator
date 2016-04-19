package com.CAAS.network.protocol;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * JSON ChainMessageProtocol Protocol
 * {
 *  type: <String> ,
 *  data: <JsonObject>
 * }
 * 타입별 데이터 포맷은 Connection 참조
 */

public class ChainMessageProtocol extends JsonWrapper{

    JsonObject data = new JsonObject();

    public ChainMessageProtocol(String type){
        super("{}");
        this.object.put("type",type);
    }

    public ChainMessageProtocol(JsonObject object){
        super(object);
        this.data = this.object.getJsonObject("data");
    }


    public String getType(){
        return object.getString("type");
    }

    public JsonObject getData(){
        return object.getJsonObject("data");
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
        object.put("data",data);
        return object.encode();
    }
}
