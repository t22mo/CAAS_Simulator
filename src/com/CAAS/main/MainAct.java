package com.CAAS.main;

import com.badlogic.gdx.backends.jglfw.JglfwApplication;
import com.badlogic.gdx.backends.jglfw.JglfwApplicationConfiguration;

public class MainAct {
	public static void main(String args[])
	{
		@SuppressWarnings("unused")
		final JglfwApplicationConfiguration cfg = new JglfwApplicationConfiguration();
		cfg.samples = 4;
		cfg.width = 700;
		cfg.height = 600;
		
		JglfwApplication jglfwApplication = new JglfwApplication( new AppListen() , cfg);
	}
}
