package com.CAAS.data;

import java.util.ArrayList;

import com.CAAS.network.protocol.ChainMessageProtocol;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;


public class CameraNode {
	public static ArrayList<CameraNode> instance = new ArrayList<CameraNode>();
	public Vector2D pos; //위치
	public Vector2D dirNormal; //방향벡터
	public double vAngle; //시야각
	public double vDis; //시야 범위
	public int radius = 7; //반지름
	public double sAngle; //시야 환산
	public boolean active; //활성화
	public boolean inSight; //시야 범위 내 존재

	public int id; //식별자
	public int port; // 포트

	public boolean rotate = true; //실제 카메라 연동 여부
	public String camIP; //실제 카메라 ip주소
	public int camPort;
	public double rState;

	Texture nodeTexture;
	Texture visionTexture;
	ArrayList<BlockData> blockList;
	ArrayList<Texture> blockTex;
	ArrayList<Vector2D> routeList;
	public double prvRouteTime;
	int routeProg;

	public EventBus eventBus;

	public static final float[] r = {0.7f, 0.933f, 0.547f , 0.191f};
	public static final float[] g = {0.7f, 0.3f	 , 0.547f , 0.980f};
	public static final float[] b = {0.7f, 0.176f, 0.547f , 0.566f};
	public static final String[] blockTxt = {"Route block","Data block","Information Block","Device Block"};
	
	public static ArrayList<CameraNode> getInstance()
	{
		return instance;
	}	
	
	public CameraNode(double x,double y,double v_x, double v_y,double vAngle,double vDis,int id, int port, boolean rotate, String temp1, int temp2, EventBus eventBus)
	{
		pos 		= new Vector2D(x, y);
		dirNormal 	= new Vector2D(v_x,v_y);	
		
		this.vAngle	= vAngle;
		this.vDis	= vDis;
		this.id		= id;
		this.port = port;
		this.rotate = rotate;
		blockList = new ArrayList<BlockData>();
		routeList = new ArrayList<Vector2D>();
		this.eventBus = eventBus;
		this.camIP = temp1;
		this.camPort = temp2;
		active = false;
		rState = 0;
		
		dirNormal.normalize();

		calcAngle();

		//텍스쳐 정의
		blockTex = new ArrayList<Texture>();
		Pixmap pixmap;
		pixmap = new Pixmap(100,30,Pixmap.Format.RGBA8888);
		pixmap.setColor( new Color(0.562f,0.750f,1.0f,0.8f) );
		pixmap.fillRectangle(0,0,150,40);
		blockTex.add( new Texture(pixmap) );

		pixmap = new Pixmap(100,30,Pixmap.Format.RGBA8888);
		pixmap.setColor(new Color(1.0f,0.398f,0f ,0.8f) );
		pixmap.fillRectangle(0,0,150,40);
		blockTex.add( new Texture(pixmap) );

		pixmap = new Pixmap(100,30,Pixmap.Format.RGBA8888);
		pixmap.setColor(new Color(0.547f,0.547f,0.547f ,0.8f) );
		pixmap.fillRectangle(0,0,150,40);
		blockTex.add( new Texture(pixmap) );

		blockList.add(new BlockData(3,"INFO_BLOCK",-1) );

	/*	String[] h = {"e4503b3d0c47242d7a4f638c0f365755715440cf","fb9cb6cfb47c4779a10bd48a77a5bc59f2c6301f","a5ba2da44e19706c26a284f2d39707d5dc77572b","ed1b8d80793e70c0608e8a8508a8dd80f6aa56f9","ba04d15f253b24d64c687936ccc5d92299409a18","8d7dbf43ce17daa110065deedb22e227e3e43cb8"};
		for(int i=0 ; i<6 ; i++)
		{
			blockList.add(new BlockData(1+i%2,h[i],i+1));
		}*/

	}

	public void update()
	{
		if(SimulatorState.elapsedTime - prvRouteTime>=(SimulatorState.routeDelay-0.0001))
		{
			if(routeProg<routeList.size()-1)
			{


				routeProg++;
				this.pos.x = routeList.get(routeProg).x;
				this.pos.y = routeList.get(routeProg).y;


				this.dirNormal.x = routeList.get(routeProg).x - routeList.get(routeProg-1).x;
				this.dirNormal.y = routeList.get(routeProg).y - routeList.get(routeProg-1).y;
				this.dirNormal.normalize();
				calcAngle();
			}
			prvRouteTime = SimulatorState.elapsedTime;
		}
		if(rotate == true)
		{
			//rotateViewVector();
		}
	}
	//Shaperenderer 사용
	public void draw(ShapeRenderer sRenderer)
	{
		
		//시야 범위
		if(active==false)
			sRenderer.setColor(0.7f, 0.7f, 0.7f, 0.4f);
		else
		{
			if(inSight==true)
				sRenderer.setColor(0.0f , 1.0f, 0.0f, 0.8f);
			else
				sRenderer.setColor(1.0f , 1.0f, 0.0f, 0.4f + 0.8f);
		}
		sRenderer.arc((float)pos.x, (float)pos.y, (float)vDis, (float)sAngle, (float)vAngle);
		
		//노드
		if(rotate ==false)
			sRenderer.setColor(Color.BLUE);
		else
			sRenderer.setColor(Color.RED);

		sRenderer.circle((float)pos.x,(float)pos.y, radius);
	}
	
	//Spritebatch 사용
	public void draw(SpriteBatch spriteBatch, BitmapFont font)
	{

	}
	public void drawBlock(SpriteBatch spriteBatch, BitmapFont font)
	{
		if(SimulatorState.blockToggle==true)
		{
			for(int i=0 ; i<blockList.size() ; i++)
			{
				BlockData block = blockList.get(i);
				int t = block.type-1;

				spriteBatch.draw(blockTex.get(t),(float)pos.x + 2,(float)pos.y + 2 + i*31);

				font.draw(spriteBatch,blockTxt[t], (float)pos.x+4,(float)pos.y+29+31*i);
				font.draw(spriteBatch,"Hash: "+block.hash.substring(0,10)+"...", (float)pos.x+4,(float)pos.y+15+31*i);
			}
		}
	}
	
	public void calcAngle()
	{
		double theta;
		if(dirNormal.x!=0)
			theta = Math.atan( dirNormal.y/dirNormal.x ) / (2*Math.PI) * (double)360  ;
		else
			theta = 90 + 90*(1 - dirNormal.y);

		if( dirNormal.x<0 )
			theta+=180;
		
		sAngle = theta - vAngle/2;
	//	System.out.println(sAngle +" "+(sAngle+vAngle));
	
	}
	public void addBlock(ChainMessageProtocol msg)
	{
		int type;


		String blockType = msg.getType();
		String hash = msg.getData().getJsonObject("block").getJsonObject("header").getString("blockHash");
		int id = msg.getData().getJsonObject("block").getJsonObject("header").getInteger("blockID");
		if(blockType.equals("push_route_block"))
			type = 1;
		else if(blockType.equals("push_data_block"))
			type = 2;
		else
			type = -1;

		blockList.add(new BlockData(type,hash,id));
	}
	public static int findPort(int port)
	{
		for(int i=0 ; i<instance.size() ; i++)
		{
			if(port == instance.get(i).port)
				return i;
		}
		return -1;
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
	public void sendRotationMessage(int angle)
	{
		ChainMessageProtocol msg = new ChainMessageProtocol("rotate_node");


		msg.put("angle",angle);

		DeliveryOptions options = new DeliveryOptions()
				.setCodecName("HashChainCodec")
				.addHeader("port",""+port);
		eventBus.send("rotate_node",msg,options);
	}
	public void rotateViewVector(double delta)
	{
		double temp;


		if(rState==delta)
			return;
		temp = delta;

		delta = (delta-rState)/(double)360;


		System.out.println(delta);


		this.dirNormal = new Vector2D( Math.cos(2*Math.PI * delta)*this.dirNormal.x - Math.sin(2*Math.PI * delta)*this.dirNormal.y , Math.sin(2*Math.PI * delta)*this.dirNormal.x + Math.cos(2*Math.PI * delta)*this.dirNormal.y );

		calcAngle();
		sendRotationMessage((int)temp+90);
		rState = temp;
	}
}
