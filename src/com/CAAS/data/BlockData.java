package com.CAAS.data;

public class BlockData {
	
	public static final int routeBlock		= 1;
	public static final int dataBlock		= 2;
	
	int type;
	String hash;
	int id;
	
	public BlockData(int type,String hash, int id)
	{
		this.type = type;
		this.hash = hash;
		this.id = id;
	}
}
