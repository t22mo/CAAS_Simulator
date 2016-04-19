package com.CAAS.network.protocol;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

/**
 * Created by tokirin on 2016-04-19.
 */
public abstract class JsonWrapper {
    protected JsonObject object;
    protected JsonArray array;
    protected ArrayList<JsonObject> objectArray = new ArrayList<JsonObject>();

    public JsonWrapper(JsonObject object){
        this.object = object;
    }

    public JsonWrapper(JsonArray array){
        this.array = array;
        for(int i = 0; i<array.size();i++){
            objectArray.add(array.getJsonObject(i));
        }
    }

    public JsonWrapper(String str){
        this.object = new JsonObject(str);
    }

    public JsonObject getObject(){
        return this.object;
    }
    public JsonArray getArray(){
        try {
            return this.array;
        }catch(NullPointerException e){
            System.out.println("JsonArray가 존재하지 않습니다.");
            return null;
        }
    }
    public ArrayList<JsonObject> getObjectArray(){
        try {
            return this.objectArray;
        }catch(NullPointerException e){
            System.out.println("Json Object ArrayList가 존재하지 않습니다.");
            return null;
        }
    }
    public String encode(){
        return this.object.encode();
    }
    public String encodeArray(){
        return this.array.encode();
    }

}
