package com.CAAS.data;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;


public class CameraNode {
	public static ArrayList<CameraNode> instance = new ArrayList<CameraNode>();
	Vector2D pos; //위치 
	Vector2D dirNormal; //방향벡터
	double vAngle; //시야각
	double vDis; //시야 범위
	int radius = 5; //반지름
	double sAngle; //시야 환산
	boolean active;
	int id; //식별자
	int port; // 포트
    NetSocket socket; // 클라이언트 커넥션 소켓
	Texture nodeTexture;
	Texture visionTexture;
	ArrayList<BlockData> blockList;
	Vertx vertx;
	
	public static final float[] r = {0.7f, 0.933f, 0.172f , 0.191f};
	public static final float[] g = {0.7f, 0.3f	 , 0.465f , 0.980f};
	public static final float[] b = {0.7f, 0.176f, 0.938f , 0.566f};
	public static final String[] blockTxt = {"Device info","Monitoring block","Route Block","Device Block"};
	
	public static ArrayList<CameraNode> getInstance()
	{
		return instance;
	}	
	
	public CameraNode(double x,double y,double v_x, double v_y,double vAngle,double vDis,int id, int port, Vertx vertx)
	{
		this.vertx = vertx;
        setConnection();
		pos 		= new Vector2D(x, y);
		dirNormal 	= new Vector2D(v_x,v_y);	
		
		this.vAngle	= vAngle;
		this.vDis	= vDis;
		this.id		= id;
		this.port = port;
		blockList = new ArrayList<BlockData>();
		
		active = false;
		
		dirNormal.normalize();
		
		calcAngle();
		
		for(int i=0 ; i<5 ; i++)
		{
			blockList.add(new BlockData( (new Random().nextInt(4)+1 )));
		}
		
	}
	
	//Shaperenderer 사용
	public void draw(ShapeRenderer sRenderer)
	{
		
		//시야 범위 
		sRenderer.setColor(1.0f,1.0f,0.0f,0.5f);
		sRenderer.arc((float)pos.x, (float)pos.y, (float)vDis, (float)sAngle, (float)vAngle);
		
		//노드
		sRenderer.setColor(Color.BLUE);
		sRenderer.circle((float)pos.x,(float)pos.y, radius);

		//블록 표시 사각형
		if(SimulatorState.blockToggle==true)
		{
			for(int i=0 ; i<blockList.size() ; i++)
			{
				BlockData block = blockList.get(i);
				int t = block.type-1;
				sRenderer.setColor(r[t],g[t],b[t],1);
			
				sRenderer.rect((float)pos.x+2, (float)pos.y+2+21*i, 110, 20);
				
			}
		}
	}
	
	//Spritebatch 사용
	public void draw(SpriteBatch spriteBatch, BitmapFont font)
	{
		if(SimulatorState.blockToggle==true)
		{
			for(int i=0 ; i<blockList.size() ; i++)
			{
				BlockData block = blockList.get(i);
				int t = block.type-1;
				font.draw(spriteBatch,blockTxt[t], (float)pos.x+3,(float)pos.y+18+21*i);	
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

	public void setConnection(){
		NetClient client = vertx.createNetClient();
        client.connect(port, "localhost", res -> {
           if(res.succeeded()) {
               System.out.println("Connected to " + port + "port ClientNode");
               NetSocket socket = res.result();
           }
        });
	}
}
