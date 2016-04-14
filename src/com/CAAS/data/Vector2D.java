package com.CAAS.data;

public class Vector2D {
	public double x,y;
	
	public Vector2D(double x,double y)
	{
		this.x = x;
		this.y = y;
	}
	
	public void normalize()
	{
		double l = Math.sqrt(x*x+y*y);
		if(l!=0)
		{
			x = x/l;
			y = y/l;
		}
	}
}
