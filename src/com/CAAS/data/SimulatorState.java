package com.CAAS.data;

public class SimulatorState {
	public static boolean blockToggle; //블록 표시 토글
	public static boolean simulatorState; //시뮬레이터 0 = 멈춤, 1 = 실행중 
	public static float elapsedTime = 0;
	public static final int mapWidth = 1121;
	public static final int mapHeight = 602;
	public static double routeDelay = 0.1;

	public SimulatorState()
	{
		blockToggle = false;
		simulatorState = false;
	}
}
