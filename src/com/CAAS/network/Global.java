package com.CAAS.network;

import com.CAAS.data.CameraNode;
import io.vertx.core.net.NetSocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 * Created by tokirin on 2016-04-18.
 */
public class Global {
    private static Global instance;
    public Stack<Integer> availableCameraNodeID = new Stack<Integer>();
    public ArrayList<CameraNode> camList;
    public HashMap<Integer,NetSocket> socketList = new HashMap<Integer, NetSocket>();

    private Global(){}
    public static synchronized Global getInstance() {
        if (instance == null) instance = new Global();
        return instance;
    }

}