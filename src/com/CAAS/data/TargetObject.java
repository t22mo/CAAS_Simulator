package com.CAAS.data;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class TargetObject {
	public Vector2D pos;
	public Vector2D dirNormal;
	public double speed;
	Texture texture;
	
	public static TargetObject instance = new TargetObject(); 
	
	public static TargetObject getInstance()
	{
		return instance;
	}
	public TargetObject()
	{
		pos = new Vector2D(15, 15);
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
		
		if(pos.x<15)
			pos.x = 15;
		if(pos.x>585)
			pos.x = 585;
		if(pos.y<15)
			pos.y = 15;
		if(pos.y>585)
			pos.y = 585;
	}
	public void draw(ShapeRenderer sRenderer)
	{
		//spriteBatch.draw(texture,(float)pos.x,(float)pos.y);
		sRenderer.setColor(Color.RED);
		sRenderer.circle((float)pos.x,(float)pos.y, 15);
	}
	public int checkInRange(ArrayList<CameraNode> arr)
	{
		for( CameraNode cn : arr)
		{
			double dis = getDistance(cn.pos,this.pos);
			double angle = calcAngle(cn.pos,this.pos);
			
			if(dis<cn.vDis && isInRange(cn.sAngle,cn.vAngle,angle))
			{
				System.out.println(cn.id);
			}
		}		
		return 0;
	}
	
	public double getDistance(Vector2D a,Vector2D b)
	{
		return Math.sqrt( Math.pow((a.x-b.x),2) + Math.pow((a.y-b.y),2) );
	}
	public double calcAngle(Vector2D a,Vector2D b)
	{
		double theta;
		Vector2D diff = new Vector2D(b.x-a.x, b.y-a.y);
		diff.normalize();		
		
		if(diff.x!=0)
			theta = Math.atan( diff.y/diff.x ) / (2*Math.PI) * (double)360  ;
		else
			theta = 90 + 90*(1 - diff.y);
		
		if( diff.x<0 )
			theta+=180;
		
		return theta;
	}
	public boolean isInRange(double start,double offset,double target)
	{
		if(start<0)
			start+=360;
		
		if(target<0)
			target+=360;
		
		if(start<= target && target <= start+offset)
			return true;
		
		if(start+offset>360)
		{
			target+=360;
			if(start<= target && target <= start+offset)
				return true;
		}
		
		return false;
	}
}
