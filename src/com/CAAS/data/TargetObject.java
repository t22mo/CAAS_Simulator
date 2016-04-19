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
	public int checkInRange(ArrayList<CameraNode> arr) //카메라 노드 범위 내에 있는지 확인
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
		return sel; //있으면 카메라 노드 인덱스 리턴. 없으면 -1
	}
	public double getDistance(Vector2D a,Vector2D b) //거리 계산
	{
		return Math.sqrt( Math.pow((a.x-b.x),2) + Math.pow((a.y-b.y),2) );
	}
	public double calcAngle(Vector2D a,Vector2D b) //각도 계산. (1,0)벡터 기준에서의 각도
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
	public boolean isInRange(double start,double offset,double target) //시작, 시작+offset 범위 이내에 목표 각도가 포함되는지 
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
	public double getIntersectingArea( CameraNode target, int n, double len) //임의의 카메라 노드 시야와 도둑의 예상 이동 범위와의 겹치는 면적 근사값. target:카메라 노드 인스턴스, n: 분할횟수, len:도둑의 예상 거리
	{
		double area = 0, sq3 = Math.sqrt(3); //도둑의 예상 이동 각도는 60도로 고정. 계산의 편의를 위해 루트3을 미리 계산해둠
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

