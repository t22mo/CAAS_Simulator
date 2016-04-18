package com.CAAS.data;

public class BlockData {
	
	public static final int deviceInfo		= 1;
	public static final int monitorBlock	= 2;
	public static final int routeBlock		= 3;
	public static final int deviceBlock		= 4;
	
	int type;
	
	public BlockData(int type)
	{
		this.type = type;
	}
}
