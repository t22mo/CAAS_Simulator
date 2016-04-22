package com.CAAS.data;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Random;

import com.CAAS.network.protocol.ChainMessageProtocol;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
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
	public Vector2D pos; //위치
	public Vector2D dirNormal; //방향벡터
	public double vAngle; //시야각
	public double vDis; //시야 범위
	public int radius = 5; //반지름
	public double sAngle; //시야 환산
	public boolean active; //활성화
	public boolean inSight; //시야 범위 내 존재
	public int id; //식별자
	public int port; // 포트
	Texture nodeTexture;
	Texture visionTexture;
	ArrayList<BlockData> blockList;
	ArrayList<Texture> blockTex;
	
	public static final float[] r = {0.7f, 0.933f, 0.172f , 0.191f};
	public static final float[] g = {0.7f, 0.3f	 , 0.465f , 0.980f};
	public static final float[] b = {0.7f, 0.176f, 0.938f , 0.566f};
	public static final String[] blockTxt = {"Route block","Data block","Route Block","Device Block"};
	
	public static ArrayList<CameraNode> getInstance()
	{
		return instance;
	}	
	
	public CameraNode(double x,double y,double v_x, double v_y,double vAngle,double vDis,int id, int port)
	{
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

		//텍스쳐 정의
		blockTex = new ArrayList<Texture>();
		Pixmap pixmap;
		pixmap = new Pixmap(150,40,Pixmap.Format.RGBA8888);
		pixmap.setColor( new Color(0.172f,0.465f,0.938f,0.9f) );
		pixmap.fillRectangle(0,0,150,40);
		blockTex.add( new Texture(pixmap) );

		pixmap = new Pixmap(150,40,Pixmap.Format.RGBA8888);
		pixmap.setColor(new Color(0.933f,0.3f,0.176f,0.9f));
		pixmap.fillRectangle(0,0,150,40);
		blockTex.add( new Texture(pixmap) );



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
		sRenderer.setColor(Color.BLUE);
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

				spriteBatch.draw(blockTex.get(t),(float)pos.x + 2,(float)pos.y + 2 + i*42);

				font.draw(spriteBatch,blockTxt[t], (float)pos.x+4,(float)pos.y+39+42*i);
				font.draw(spriteBatch,"Hash: "+block.hash, (float)pos.x+4,(float)pos.y+25+42*i);
				font.draw(spriteBatch,"ID: "+block.id, (float)pos.x+4,(float)pos.y+12+42*i);
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

}
