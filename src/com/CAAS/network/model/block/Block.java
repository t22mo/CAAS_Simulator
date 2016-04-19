package com.CAAS.network.model.block;

import com.CAAS.network.protocol.JsonWrapper;
import io.vertx.core.json.JsonObject;

/**
 * Block Super Class
 * Created by tokirin on 2016-04-19.
 **
 * Block JSON Format
 * {
 *  header:
 *      {
 *          blockHash: <String>,
 *          blockID: <Integer>,
 *          blockType: <String> ["route" or "data"]
 *      },
 *  body: <JsonObject>
 * }
 */

public class Block extends JsonWrapper {
    private BlockHeader header;
    private BlockBody body;

    public Block(JsonObject objectStr){
        super(objectStr);
        this.header = new BlockHeader(this.object.getJsonObject("header"));
        this.body = new BlockBody(this.object.getJsonObject("body"));
        /**
         * body는 어차피 상속되서 재정의 되므로
         * body는 상속 클래스에서 정의한뒤 setting
         */

    }

    public BlockHeader getHeader() {
        return this.header;
    }

    public BlockBody getBody(){
        return this.body;
    }

    public String getContentString(){
        return this.header.encode() + this.body.encode();
    }
    /**
     * Block BlockHeader Class
     * 모든 블록이 동일함
     */
    class BlockHeader extends JsonWrapper{
        public String blockHash;
        public Integer blockID;
        public String blockType;

        public BlockHeader(JsonObject headerObject){
            super(headerObject);
            this.blockHash = headerObject.getString("blockHash");
            this.blockID = headerObject.getInteger("blockID");
            this.blockType = headerObject.getString("blockType");
        }
    }

    /**
     * Block BlockBody Abstract Class
     * 블록마다 다르게 구현됨(내용이 다름)
     */

    class BlockBody extends JsonWrapper{
        public BlockBody(JsonObject object){
            super(object);
        }
    }

}
