package com.CAAS.network.model.block;

import io.vertx.core.json.JsonObject;

/**
 * Route Block Class
 * Created by tokirin on 2016-04-19.
 **
 * Block Format은 Block.java 참조
 **
 * Route Block Body Format
 * {
 *     something: <String>
 * }
 */

public class RouteBlock extends Block{
    public RouteBody body;
    public RouteBlock(JsonObject objectStr) {
        super(objectStr);
        this.body = new RouteBody(this.object.getJsonObject("body"));
    }

    class RouteBody extends BlockBody {
        public String something;
        public RouteBody(JsonObject object) {
            super(object);
            // test code
            this.something = object.getString("something");
        }
    }
}
