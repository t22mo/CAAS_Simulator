package com.CAAS.data;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class StartButton extends Button {

	@Override
	public void draw(SpriteBatch spriteBatch) {
		// TODO Auto-generated method stub
		spriteBatch.draw(textures.get(touchFlag?1:0),x,y,width,height);
	}

	@Override
	public void inputUpdate() {
		// TODO Auto-generated method stub
		if(Gdx.input.isTouched())
		{
			if(touchFlag==false)
			{
				double x = Gdx.input.getX();
				double y = 600-Gdx.input.getY();
				
				if(isContaining(x, y))
				{
					touchFlag = true;
					SimulatorState.simulatorState = !SimulatorState.simulatorState;
					TargetObject.getInstance().checkInRange( CameraNode.getInstance() );
				}
			}
		}
		else
			touchFlag = false;
	}

	public StartButton(float x, float y, float width, float height, ArrayList<Texture> textures) {
		super(x, y, width, height, textures);
	}
	

}
