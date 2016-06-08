package com.CAAS.main;

import com.CAAS.data.SimulatorState;
import com.badlogic.gdx.backends.jglfw.JglfwApplication;
import com.badlogic.gdx.backends.jglfw.JglfwApplicationConfiguration;

public class MainAct {
	public static void main(String args[])
	{
		final JglfwApplicationConfiguration cfg = new JglfwApplicationConfiguration();
		cfg.samples = 4;
		cfg.width = SimulatorState.mapWidth+100;
		cfg.height = SimulatorState.mapHeight;
		
		@SuppressWarnings("unused")
		JglfwApplication jglfwApplication = new JglfwApplication( new AppListen() , cfg);
	}
}
