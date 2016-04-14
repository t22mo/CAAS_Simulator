package com.CAAS.data;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class CameraNode {
	Vector2D pos; //위치 
	Vector2D dirNormal; //방향벡터
	double vAngle; //시야각
	double vDis; //시야 범위
	int radius; //반지름
	double sAngle; //시야각 환산
	Texture nodeTexture;
	Texture visionTexture;
	
	public CameraNode(double x,double y,double v_x, double v_y,double vAngle,double vDis,int radius)
	{
		pos 		= new Vector2D(x, y);
		dirNormal 	= new Vector2D(v_x,v_y);		
		
		this.radius	= radius;
		this.vAngle	= vAngle;
		this.vDis	= vDis;
		
		dirNormal.normalize();
		System.out.println(dirNormal.x+" "+dirNormal.y);
		calcAngle();
	}
	public void draw(ShapeRenderer sRenderer)
	{
		sRenderer.setColor(Color.YELLOW);
		sRenderer.arc((float)pos.x, (float)pos.y, (float)vDis, (float)sAngle, (float)vAngle);
		
		//spriteBatch.draw(nodeTexture,(float)pos.x,(float)pos.y);
		
		sRenderer.setColor(Color.BLUE);
		sRenderer.circle((float)pos.x,(float)pos.y, radius);
		
		
	}
	public void calcAngle()
	{
		double theta;
		if(dirNormal.x!=0)
			theta = Math.atan( dirNormal.y/dirNormal.x ) / 2*Math.PI * (double)360  ;
		else
			theta = 90 + 90*(1 - dirNormal.y);
		sAngle = theta - vAngle/2;		
	}
	
}
