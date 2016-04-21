package com.CAAS.network.model;

import com.CAAS.data.CameraNode;
import io.vertx.core.net.NetSocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * Created by tokirin on 2016-04-18.
 */
public class Global {
    private static Global instance;
    public static Stack<Integer> availableCameraNodeID = new Stack<Integer>();
    public static ArrayList<CameraNode> camList; //
    public static HashMap<Integer,NetSocket> socketList = new HashMap<Integer, NetSocket>();

    private Global(){}
    public static synchronized Global getInstance() {
        if (instance == null) instance = new Global();
        return instance;
    }

}
