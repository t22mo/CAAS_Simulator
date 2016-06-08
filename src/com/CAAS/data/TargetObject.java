package com.CAAS.data;

import java.util.ArrayList;

import com.CAAS.network.model.Global;
import com.CAAS.network.protocol.ChainMessageProtocol;
import com.CAAS.network.protocol.HashChainCodec;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;

public class TargetObject {
	public Vector2D	pos;
	public Vector2D	dirNormal;
	public double	speed;
	public double	prvRouteTime;
	public boolean	inSight; //임의의 노드 시야 내에 있는지에 대한 여부
	Texture			texture;
	int				select; //현재 시야범위 내에 속한 노드의 인덱스
	float			prv = 0;
	ArrayList<Vector2D> routeList;
	public static int routeProg;

	Vector2D		sightStart;
	Vector2D		sightEnd;
	Vector2D		prvLoc; //
	boolean			traceLineEnabled;
	EventBus		eventBus;

	//공유 인스턴스
	public static TargetObject instance;
	public static TargetObject getInstance(EventBus eventBus)
	{
		if(instance == null) instance = new TargetObject(eventBus);
		return instance;
	}

	public TargetObject(EventBus eventBus)
	{
		pos			= new Vector2D(15, 15);
		dirNormal	= new Vector2D(0,0);
		speed		= 1;
		prvLoc		= new Vector2D(-100,-100);
		//원형 텍스쳐 생성

		texture = new Texture(Gdx.files.internal("res/img/thief.png"));
		routeList = new ArrayList<Vector2D>();
		this.eventBus = eventBus;
	}

	public void update() {
		ArrayList<CameraNode> arr = CameraNode.getInstance();

		//방향에 따라 이동
		/*pos.x += speed * dirNormal.x;
		pos.y += speed * dirNormal.y;
		
		if(pos.x<15)
			pos.x = 15;
		if(pos.x>585)
			pos.x = 585;
		if(pos.y<15)
			pos.y = 15;
		if(pos.y>585)
			pos.y = 585;*/

		if(SimulatorState.elapsedTime - prvRouteTime>=(SimulatorState.routeDelay-0.0001))
		{
			if(routeProg<routeList.size()-1)
			{
				routeProg++;
				this.pos.x = routeList.get(routeProg).x;
				this.pos.y = routeList.get(routeProg).y;
				System.out.println(this.pos.x+" "+this.pos.y);
			}
			prvRouteTime = SimulatorState.elapsedTime;
		}


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

						if( new Vector2D(this.pos.x-prvLoc.x,this.pos.y-prvLoc.y).getLength()>20 ) //거리가 10이상 움직이면
						{
							prvLoc.x = this.pos.x;
							prvLoc.y = this.pos.y;
							ChainMessageProtocol msg = new ChainMessageProtocol("location_info");
							msg.put("x", "" + this.pos.x);
							msg.put("y", "" + this.pos.y);
							sendNetworkMessage("location_info", arr.get(select).port, msg);
						}
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

		/*sRenderer.setColor(Color.RED);
		sRenderer.circle((float)pos.x,(float)pos.y, 15);*/
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
	public void draw(SpriteBatch spriteBatch)
	{
		spriteBatch.draw(texture,(float)pos.x-12.5f,(float)pos.y-12.5f,25.0f,25.0f);
	}

	/*도둑이 전체 카메라의 시야 범위중 속하는지 확인하는 메세드.
	반환값: 시야범위에 속하는 노드의 인덱스. 존재하지 않을시, -1
	 */
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

	//도둑이 특정 카메라의 시야 범위 내에 속하는지 확인하는 메서드
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

	/*
	arr		: 카메라 노드의 목록.
	반환값	: 가장 넓은 범위가 겹치는 카메라 노드의 ArrayList상에서의 인덱스. 모두 겹치지 않는다면, -1.
	 */
	public int selectMaximumArea(ArrayList<CameraNode> arr) //모든 카메라 노드 중에서 도둑이 예상 이동범위와 가장 많이 겹치는 카메라를 찾음.
	{
		double max=-99999;
		int sel=-1;
		
		for(int i=0 ; i<arr.size() ; i++)
		{
			CameraNode cn = arr.get(i);

			double area = getIntersectingArea(cn, 20, new Vector2D(sightEnd.x-sightStart.x,sightEnd.y-sightStart.y), 200, 60); //분할횟수, 예상 이동 거리는 200, 각도는 60으로 고정함.
			if(max<area && area>0)
			{
				max = area;
				sel = i;
			}
		}
		if(sel==-1) //만일 위의 범위에서 겹치는 노드를 찾지 못하였다면,
		{
			for(int i=0 ; i<arr.size() ; i++)
			{
				CameraNode cn = arr.get(i);

				double area = getIntersectingArea(cn, 20, new Vector2D(sightEnd.x-sightStart.x,sightEnd.y-sightStart.y), 300, 90); //거리를 300, 각도는 90으로 고정하여 재검사.
				if(max<area && area>0)
				{
					max = area;
					sel = i;
				}
			}
		}
		return sel; //있으면 카메라 노드 인덱스 리턴. 없으면 -1
	}


	/*
	target	: 대상 카메라 노드의 인스턴스
	n		: 겹치는 면적 계산 시 분할 횟수
	normal	: 도둑의 현재 이동 방향 벡터
	len		: 도둑의 예상 이동 거리
	angle	: 도둑의 예상 이동 범위 각도
	반환값	: 도둑의 예상 이동 범위와 카메라 노드의 시야 범위중 겹치는 부분의 근사값.
	 */
	public double getIntersectingArea( CameraNode target, int n, Vector2D normal, double len, double angle) //임의의 카메라 노드 시야와 도둑의 예상 이동 범위와의 겹치는 면적 근사값. target:카메라 노드 인스턴스, n: 분할횟수, len:도둑의 예상 거리
	{
		double area = 0, tanTheta = Math.tan(angle/2/360*2*Math.PI);
		ArrayList<Vector2D> vList = new ArrayList<Vector2D>();
		boolean[] chk = new boolean[6];
		
		if(normal.x==0 && normal.y==0)
			return -1;
		normal.normalize();

		for(int i=0 ; i<6 ; i++)
			vList.add(new Vector2D(0,0));
		vList.get(1).x = vList.get(3).x = vList.get(5).x = this.pos.x;
		vList.get(1).y = vList.get(3).y = vList.get(5).y = this.pos.y;
		
		for(int i=1 ; i<=n ; i++)
		{
			vList.get(0).x = vList.get(1).x;
			vList.get(0).y = vList.get(1).y;
			
			vList.get(1).x += (len / (double)n) * normal.x;
			vList.get(1).y += (len / (double)n) * normal.y;
			
			vList.get(2).x = vList.get(3).x;  
			vList.get(2).y = vList.get(3).y; 
			
			vList.get(3).x += ((len * tanTheta) / (double)n) * normal.y;
			vList.get(3).x += (len / (double)n) * normal.x;
			vList.get(3).y -= ((len * tanTheta) / (double)n) * normal.x;
			vList.get(3).y += (len / (double)n) * normal.y;
			
			vList.get(4).x = vList.get(5).x;
			vList.get(4).y = vList.get(5).y;
			
			vList.get(5).x -= ((len * tanTheta) / (double)n) * normal.y;
			vList.get(5).x += (len / (double)n) * normal.x;
			vList.get(5).y += ((len * tanTheta) / (double)n) * normal.x;
			vList.get(5).y += (len / (double)n) * normal.y;
			
			for(int j=0 ; j<6 ; j++)
			{
				if(getDistance(vList.get(j),target.pos) < target.vDis && isInRange(target.sAngle,target.vAngle,calcAngle(target.pos,vList.get(j))))
					chk[j] = true;
				else
					chk[j] = false;
			}

			if(chk[0]==true && chk[1]==true)
				area+=(len/(double)n) * 5;

			if( (chk[0]?1:0) + (chk[1]?1:0) + (chk[2]?1:0) + (chk[3]?1:0) >=3 )
				area+= Math.pow(len,2) * (2*i-1) * tanTheta / ( 2*Math.pow(n, 2) );

			if( (chk[0]?1:0) + (chk[1]?1:0) + (chk[4]?1:0) + (chk[5]?1:0) >=3 )
				area+= Math.pow(len,2) * (2*i-1) * tanTheta / ( 2*Math.pow(n, 2) );
		}
		if(area>0)
		{
			Vector2D tVector = new Vector2D(target.pos.x-this.pos.x,target.pos.y-this.pos.y);
			double cosTheta =  (normal.x * tVector.x + normal.y * tVector.y) / tVector.getLength();
			cosTheta = Math.abs(cosTheta);
			double v = tVector.getLength();
			double k = cosTheta * v;
			area/=k;

			System.out.println(target.id + " : " + cosTheta+", "+ v +", "+k);

		}
		return area;
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
	public void addRoute(Vector2D pos)
	{
		routeList.add(translateRouteData(pos));
	}

	public Vector2D translateRouteData(Vector2D pos)
	{
		pos.x = (pos.x - 230.40) / 1034.3 * SimulatorState.mapWidth;
		pos.y = (pos.y - 1636.26) / 554.09 * SimulatorState.mapHeight;
		return pos;
	}
}

