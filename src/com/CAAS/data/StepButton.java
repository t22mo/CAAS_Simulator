package com.CAAS.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

/**
 * Created by T22mo on 2016-05-04.
 */
public class StepButton extends Button {
    @Override
    public void draw(SpriteBatch spriteBatch) {
        // TODO Auto-generated method stub
        spriteBatch.draw(textures.get(touchFlag?1:0),x,y,width,height);
    }

    @Override
    public void inputUpdate() {

        if(Gdx.input.isTouched())
        {
            if(touchFlag==false)
            {
                double x = Gdx.input.getX();
                double y = SimulatorState.mapHeight -Gdx.input.getY();

                if(isContaining(x, y))
                {
                    SimulatorState.elapsedTime += SimulatorState.routeDelay;
                    touchFlag = true;
                }
            }

        }
        else
            touchFlag = false;
    }

    public StepButton(float x, float y, float width, float height, ArrayList<Texture> textures) {
        super(x, y, width, height, textures);
    }
}
