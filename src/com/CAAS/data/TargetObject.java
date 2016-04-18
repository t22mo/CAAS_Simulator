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
		double max=-99999;
		int sel=-1;
		
		for(int i=0 ; i<arr.size() ; i++)
		{
			CameraNode cn = arr.get(i);
			double dis = getDistance(cn.pos,this.pos);
			double angle = calcAngle(cn.pos,this.pos);
			
			if(dis<cn.vDis && isInRange(cn.sAngle,cn.vAngle,angle))
			{
				double area = getIntersectingArea(cn, 20, 200);
				if(max<area)
				{
					max = area;
					sel = i;
				}
			}
		}		
		return sel;
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
	public double getIntersectingArea( CameraNode target, int n, double len)
	{
		double area = 0, sq3 = Math.sqrt(3);
		ArrayList<Vector2D> vList = new ArrayList<Vector2D>();
		boolean[] chk = new boolean[6];
		
		if(dirNormal.x==0 && dirNormal.y==0)
			return -1;
		
		for(int i=0 ; i<6 ; i++)
			vList.add(new Vector2D(0,0));
		vList.get(1).x = vList.get(3).x = vList.get(5).x = this.pos.x;
		vList.get(1).y = vList.get(3).y = vList.get(5).y = this.pos.y;
		
		for(int i=1 ; i<=n ; i++)
		{
			vList.get(0).x = vList.get(1).x;
			vList.get(0).y = vList.get(1).y;
			
			vList.get(1).x += (len / (double)n) * this.dirNormal.x;
			vList.get(1).y += (len / (double)n) * this.dirNormal.y;
			
			vList.get(2).x = vList.get(3).x;  
			vList.get(2).y = vList.get(3).y; 
			
			vList.get(3).x += ((len / sq3) / (double)n) * this.dirNormal.y;
			vList.get(3).x += (len / (double)n) * this.dirNormal.x;
			vList.get(3).y -= ((len / sq3) / (double)n) * this.dirNormal.x;
			vList.get(3).y += (len / (double)n) * this.dirNormal.y;
			
			vList.get(4).x = vList.get(5).x;
			vList.get(4).y = vList.get(5).y;
			
			vList.get(5).x -= ((len / sq3) / (double)n) * this.dirNormal.y;
			vList.get(5).x += (len / (double)n) * this.dirNormal.x;
			vList.get(5).y += ((len / sq3) / (double)n) * this.dirNormal.x;			
			vList.get(5).y += (len / (double)n) * this.dirNormal.y;
			
			for(int j=0 ; j<6 ; j++)
			{
				if(getDistance(vList.get(j),target.pos) < target.vDis && isInRange(target.sAngle,target.vAngle,calcAngle(target.pos,vList.get(j))))
					chk[j] = true;
				else
					chk[j] = false;
			}
			if(chk[0] && chk[1])
			{
				if(chk[2] && chk[3])
					area+= Math.pow(len,2) * (2*i-1) / ( 2*sq3*Math.pow(n, 2) );
				if(chk[4] && chk[5])
					area+= Math.pow(len,2) * (2*i-1) / ( 2*sq3*Math.pow(n, 2) );
			}
					
		}
		return area;
	}
}

