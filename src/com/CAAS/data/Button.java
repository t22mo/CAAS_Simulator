package com.CAAS.data;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Button {
	float x,y;
	float width,height;
	ArrayList<Texture> textures;
	boolean touchFlag;
	
	public Button(float x,float y,float width,float height,ArrayList<Texture> textures)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.textures = textures;
	}
	public void update()
	{
		
	}	
	public void draw(SpriteBatch spriteBatch)
	{
	}
	
	public void inputUpdate()
	{
		if(Gdx.input.isTouched())
		{
			touchFlag=true;
			double x = Gdx.input.getX();
			double y = Gdx.input.getY();
			
			if(isContaining(x, y))
			{
				//동작
			}
		}
		else
			touchFlag=false;
	}
	public boolean isContaining(double x,double y)
	{
		
		if(x>=this.x && x<=this.x+width && y>=this.y && y<=this.y+height)
			return true;
		return false;
	}
}
