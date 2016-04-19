package com.CAAS.network.model.block;

import io.vertx.core.json.JsonObject;

/**
 * Data Block Class
 * Created by tokirin on 2016-04-19.
 **
 * Block Format은 Block.java 참조
 **
 * Data Block Body Format
 * {
 *     something: <String>
 * }
 */

public class DataBlock extends Block{
    private DataBody body;
    public DataBlock(JsonObject objectStr) {
        super(objectStr);
        this.body = new DataBody(object.getJsonObject("body"));
    }

    class DataBody extends BlockBody {
        public String something;
        public DataBody(JsonObject object) {
            super(object);
            this.something = object.getString("something");
        }
    }
}
