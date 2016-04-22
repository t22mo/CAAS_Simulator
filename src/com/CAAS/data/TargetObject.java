package com.CAAS.data;

import java.util.ArrayList;

import com.CAAS.network.model.Global;
import com.CAAS.network.protocol.ChainMessageProtocol;
import com.CAAS.network.protocol.HashChainCodec;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;

public class TargetObject {
	public Vector2D	pos;
	public Vector2D	dirNormal;
	public double	speed;
	public boolean	inSight; //임의의 노드 시야 내에 있는지에 대한 여부
	Texture			texture;
	int				select; //현재 시야범위 내에 속한 노드의 인덱스
	float			prv = 0;

	Vector2D		sightStart;
	Vector2D		sightEnd;
	boolean			traceLineEnabled;
	Vertx			vertx;
	EventBus		eventBus;

	//공유 인스턴스
	public static TargetObject instance = new TargetObject();
	public static TargetObject getInstance()
	{
		return instance;
	}

	public TargetObject()
	{
		pos			= new Vector2D(15, 15);
		dirNormal	= new Vector2D(0,0);
		speed		= 1;

		//원형 텍스쳐 생성
		Pixmap pixmap;
		pixmap = new Pixmap(40,40,Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.RED);
		pixmap.fillCircle(20,20,19);
		texture = new Texture(pixmap);

		vertx = Vertx.vertx();
		eventBus = vertx.eventBus();
		eventBus.registerDefaultCodec(ChainMessageProtocol.class, new HashChainCodec());
		vertx.deployVerticle("com.CAAS.network.verticle.SimulatorManager");
	}

	public void update() {
		ArrayList<CameraNode> arr = CameraNode.getInstance();

		//방향에 따라 이동
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

		//노드 상태 업데이트
		int sel = findInRangeNode(CameraNode.getInstance());
		if(sel!=-1) //범위안으로 들어감
		{
			if(inSight==false) //새로 범위로 들어옴
			{
				activateSightTracking(arr,sel);
			}
			else
			{
				if(!checkInRange(arr.get(select))) //다른 범위로 갈아탐
				{
					deactivateNode(arr);
					activateNode(arr,sel);
					activateSightTracking(arr,sel);
				}
				else //아직 같은 노드 내에 있음.
				{
					if(SimulatorState.elapsedTime - prv>=1)
					{
						prv = SimulatorState.elapsedTime;
						//TODO: send location info to "select"
						ChainMessageProtocol msg = new ChainMessageProtocol("location_info");
						msg.put("x",""+this.pos.x);
						msg.put("y",""+this.pos.y);
						sendNetworkMessage("location_info",arr.get(select).port, msg);

					}
				}
			}
		}
		else //범위밖
		{
			if(inSight==true) //범위 밖으로 나감
			{
				deactivateNode(arr);
			}
		}
	}

	public void activateNode(ArrayList<CameraNode> arr, int idx) //idx노드 활성화
	{
		//TODO: Send activation message to "idx" node - 2
		ChainMessageProtocol msg = new ChainMessageProtocol("activate_node");
		msg.put("x",String.format("%.2f",this.pos.x));
		msg.put("y",String.format("%.2f",this.pos.y));
		sendNetworkMessage("activate_node",arr.get(idx).port ,msg);

		select = idx;
		for(int i=0 ; i<arr.size() ; i++)
		{
			arr.get(i).active = false;
		}
		arr.get(select).active = true;
	}
	public void activateSightTracking(ArrayList<CameraNode> arr, int idx)
	{
		inSight = true;
		traceLineEnabled = false;
		arr.get(idx).inSight = true;
		sightStart = pos.clone();

		if(arr.get(idx).active==false) //아무것도 활성화 되지 않은 상태서 들어옴
		{
			activateNode(arr,idx);
		}
	}
	public void deactivateNode(ArrayList<CameraNode> arr) //현재 활성화된 노드 비활성화
	{
		//TODO: Send deactivation message to "select" node - 4
		ChainMessageProtocol msg = new ChainMessageProtocol("deactivate_node");
		msg.put("sx",String.format("%.2f",sightStart.x));
		msg.put("sy",""+String.format("%.2f",sightStart.y));
		msg.put("ex",""+String.format("%.2f",this.pos.x));
		msg.put("ey",""+String.format("%.2f",this.pos.y));
		sendNetworkMessage("deactivate_node",arr.get(select).port, msg);

		inSight = false;
		traceLineEnabled = true;
		arr.get(select).inSight = false;
		arr.get(select).active = false;
		select = -1;
		sightEnd = pos.clone();
		int mx = selectMaximumArea(CameraNode.getInstance());
		if(mx!=-1)
		{
			activateNode(arr,mx);
		}
	}
	public void sendNetworkMessage(String channel, int port,ChainMessageProtocol msg)
	{
		//network part
		DeliveryOptions options = new DeliveryOptions()
				.setCodecName("HashChainCodec")
				.addHeader("port",""+port);
		eventBus.send(channel,msg,options);
	}
	public void draw(ShapeRenderer sRenderer)
	{
		//spriteBatch.draw(texture,(float)pos.x,(float)pos.y);
		sRenderer.setColor(Color.RED);
		sRenderer.circle((float)pos.x,(float)pos.y, 15);
		if(traceLineEnabled == true)
		{
			double sq3 = Math.sqrt(3);
			sRenderer.setColor(Color.RED);
			sRenderer.rectLine( (float)sightStart.x, (float)sightStart.y, (float)sightEnd.x, (float)sightEnd.y,2 );
			Vector2D temp = new Vector2D(sightStart.x-sightEnd.x, sightStart.y - sightEnd.y);
			temp.normalize();
			Vector2D r = new Vector2D( sq3/2*temp.x - temp.y/2, temp.x/2 + sq3/2*temp.y );
			Vector2D l = new Vector2D( sq3/2*temp.x + temp.y/2, -1*temp.x/2 + sq3/2*temp.y );
			sRenderer.rectLine( (float)sightEnd.x, (float)sightEnd.y, (float)(sightEnd.x + 15*r.x), (float)(sightEnd.y + 15*r.y),2 );
			sRenderer.rectLine( (float)sightEnd.x, (float)sightEnd.y, (float)(sightEnd.x + 15*l.x), (float)(sightEnd.y + 15*l.y),2 );
		}
	}
	public int findInRangeNode(ArrayList<CameraNode> arr)
	{
		for(int i=0 ; i<arr.size() ; i++)
		{
			if(checkInRange(arr.get(i)))
			{
				return i;
			}
		}
		return -1;
	}
	public boolean checkInRange(CameraNode cn)
	{
		double dis = getDistance(cn.pos,this.pos);
		double angle = calcAngle(cn.pos,this.pos);

		if(dis<cn.vDis && isInRange(cn.sAngle,cn.vAngle,angle))
		{
			return true;
		}
		return false;
	}
	public int selectMaximumArea(ArrayList<CameraNode> arr) //카메라 노드 범위 내에 있는지 확인
	{
		double max=-99999;
		int sel=-1;
		
		for(int i=0 ; i<arr.size() ; i++)
		{
			CameraNode cn = arr.get(i);

			double area = getIntersectingArea(cn, 20, 200);
			if(max<area && area>0)
			{
				max = area;
				sel = i;
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


			if( (chk[0]?1:0) + (chk[1]?1:0) + (chk[2]?1:0) + (chk[3]?1:0) >=3 )
				area+= Math.pow(len,2) * (2*i-1) / ( 2*sq3*Math.pow(n, 2) );

			if( (chk[0]?1:0) + (chk[1]?1:0) + (chk[4]?1:0) + (chk[5]?1:0) >=3 )
				area+= Math.pow(len,2) * (2*i-1) / ( 2*sq3*Math.pow(n, 2) );

		}
		return area;
	}
}

