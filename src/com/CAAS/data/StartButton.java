package com.CAAS.data;

import java.util.ArrayList;

import com.CAAS.network.protocol.ChainMessageProtocol;
import com.CAAS.network.protocol.HashChainCodec;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;

public class StartButton extends Button {

	@Override
	public void draw(SpriteBatch spriteBatch) {
		// TODO Auto-generated method stub
		spriteBatch.draw(textures.get(touchFlag?1:0),x,y,width,height);
	}

	@Override
	public void inputUpdate() {

		int sel;
		// TODO Auto-generated method stub
		if(Gdx.input.isTouched())
		{
			if(touchFlag==false)
			{
				double x = Gdx.input.getX();
				double y = SimulatorState.mapHeight-Gdx.input.getY();
				
				if(isContaining(x, y))
				{
					SimulatorState.simulatorState = !SimulatorState.simulatorState;
					touchFlag = true;
					
					/*if(SimulatorState.simulatorState == false)
					{
						if(TargetObject.getInstance(null).findInRangeNode ( CameraNode.getInstance() )!=-1)
							SimulatorState.simulatorState = !SimulatorState.simulatorState;
						else
						{
							//도둑을 범위 안으로 추가해야 함 
						}
					}
					else
						SimulatorState.simulatorState = !SimulatorState.simulatorState;*/
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
