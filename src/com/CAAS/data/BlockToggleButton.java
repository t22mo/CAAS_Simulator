package com.CAAS.data;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BlockToggleButton extends Button{

	public BlockToggleButton(float x, float y, float width, float height,
			ArrayList<Texture> textures) {
		super(x, y, width, height, textures);
	}

	@Override
	public void draw(SpriteBatch spriteBatch)
	{
		int texSel;
		
		texSel = (SimulatorState.blockToggle?2:0) + (touchFlag?1:0); // 0,1,2,3
		spriteBatch.draw(textures.get(texSel),x,y,width,height);
	}
	
	@Override
	public void inputUpdate() {
		
		if(Gdx.input.isTouched())
		{
			if(touchFlag==false)
			{
				double x = Gdx.input.getX();
				double y = 600-Gdx.input.getY();
				
				if(isContaining(x, y))
				{
					touchFlag = true;
					SimulatorState.blockToggle = !SimulatorState.blockToggle;
				}
			}
		}
		else
			touchFlag = false;
	}

}
