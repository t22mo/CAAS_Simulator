package com.CAAS.data;

public class SimulatorState {
	public static boolean blockToggle; //블록 표시 토글
	public static boolean simulatorState; //시뮬레이터 0 = 멈춤, 1 = 실행중 
	
	public SimulatorState()
	{
		blockToggle = false;
		simulatorState = false;
	}
}
