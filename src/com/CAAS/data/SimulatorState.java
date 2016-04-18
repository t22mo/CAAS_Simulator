package com.CAAS.data;

public class SimulatorState {
	public static boolean blockToggle;
	public static boolean simulatorState; //0 = 멈춤, 1 = 실행중
	
	public SimulatorState()
	{
		blockToggle = false;
		simulatorState = false;
	}
}
