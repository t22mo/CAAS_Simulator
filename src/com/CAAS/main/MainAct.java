package com.CAAS.main;

import com.badlogic.gdx.backends.jglfw.JglfwApplication;

public class MainAct {
	public static void main(String args[])
	{
		@SuppressWarnings("unused")
		JglfwApplication jglfwApplication = new JglfwApplication( new AppListen() , "CAAS Simulator", 800, 800) ;
	}
}
