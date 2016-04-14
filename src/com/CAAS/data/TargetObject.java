package com.CAAS.data;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class TargetObject {
	public Vector2D pos;
	public Vector2D dirNormal;
	public double speed;
	Texture texture;

	public TargetObject()
	{
		pos = new Vector2D(0, 0);
		dirNormal = new Vector2D(0,0);
		speed = 1;
		
		Pixmap pixmap;
		pixmap = new Pixmap(40,40,Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.RED);
		pixmap.fillCircle(20,20,19);
		texture = new Texture(pixmap);
	}

	public void update() {
		pos.x += speed * dirNormal.x;
		pos.y += speed * dirNormal.y;
	}
	public void draw(ShapeRenderer sRenderer)
	{
		//spriteBatch.draw(texture,(float)pos.x,(float)pos.y);
		sRenderer.setColor(Color.RED);
		sRenderer.circle((float)pos.x,(float)pos.y, 15);
	}
	
}
