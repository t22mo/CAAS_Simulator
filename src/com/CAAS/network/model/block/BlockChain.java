package com.CAAS.network.model.block;

import io.vertx.core.json.JsonArray;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * BlockChain Singleton Class
 * Created by tokirin on 2016-04-19.
 **
 * List of Blocks
 */
public class BlockChain {

    /**
     * Singleton variables
     */
    public static ArrayList<Block> blockChain;

    /**
     * Singleton Constructor
     */

    public static BlockChain instance;
    public static synchronized BlockChain getInstance(){
        if(instance == null) instance = new BlockChain();
        return instance;
    }

    public BlockChain(){
        this.blockChain = new ArrayList<Block>();
        System.out.println("BlockChain 초기화");
    }

    public static synchronized boolean pushBlock(Block block) throws NoSuchAlgorithmException {
        if(block.getHeader().blockHash.equals(getCurrentBlockHash())){
            System.out.println("블록이 추가되었습니다 : " + block.getHeader().blockHash);
            blockChain.add(block);
            return true;
        }else{
            return false;
        }
    }

    /*
     * 최신 Block 리턴
     */
    public static synchronized Block popBlock(){
        return blockChain.get(blockChain.size()-1);
    }

    public static synchronized Block getBlock(int index){
        return blockChain.get(index);
    }

    /*
     * Json String Serialize(Array)
     */
    public static synchronized String getSerializedBlockChain(){
        JsonArray chain = new JsonArray();
        for (int i = 0; i < blockChain.size(); i++) {
            chain.add(blockChain.get(i).encode());
        }
        return chain.encode();
    }

    public static synchronized String getCurrentBlockHash() throws NoSuchAlgorithmException {
        String hashStr = "THIS_IS_THE_FIRST_BLOCK";

        /*
         * 블록체인에 블록이 등록되어 있지않다면 첫블록의 해시를 리턴
         */
        if(blockChain.size() == 0){
            return hashStr;
        }
        /*
         * 블록체인에 블록이 하나이상 있을경우 해싱 프로세스 진행
         */
        else {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                for (int i = 0; i < blockChain.size(); i++) {
                    Block b = blockChain.get(i);

                    /*
                     * Hash String(H n) 생성
                     * H(n) = SHA256(H(n-1) + Contents(n)) (단, n > 1)
                     * H(1) = "THIS_IS_THE_FIRST_BLOCK"
                     */

                    hashStr = hashStr + b.getContentString();
                    md.update(hashStr.getBytes());
                    byte byteData[] = md.digest();
                    StringBuffer sb = new StringBuffer();
                    for (int j = 0; j < byteData.length; j++) {
                        sb.append(Integer.toString((byteData[j] & 0xff) + 0x100, 16).substring(1));
                    }
                    hashStr = sb.toString();
                }
                return hashStr;
        }
    }

}
