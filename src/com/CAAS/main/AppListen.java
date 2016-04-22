package com.CAAS.main;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import com.CAAS.network.model.Global;
import com.CAAS.network.protocol.ChainMessageProtocol;
import com.CAAS.network.protocol.HashChainCodec;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.lwjgl.opengl.GL11;
import com.CAAS.data.BlockToggleButton;
import com.CAAS.data.CameraNode;
import com.CAAS.data.SimulatorState;
import com.CAAS.data.StartButton;
import com.CAAS.data.TargetObject;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class AppListen implements ApplicationListener {

	ArrayList<CameraNode>	camList;
	TargetObject			targetObj; 
	SpriteBatch				spriteBatch;
	ShapeRenderer			sRenderer;
	BitmapFont				font,largeFont;
	SimulatorState			state;
	BlockToggleButton		blockToggleBtn;
	StartButton				startBtn;

	//Networking Variable
	Global global = Global.getInstance();
	Vertx vertx;
	EventBus eventBus;
	float realTime=0;

	@Override
	public void create() {
		// SimulatorManager instantiate
		vertx = Vertx.vertx();
		eventBus = vertx.eventBus();
		eventBus.registerDefaultCodec(ChainMessageProtocol.class, new HashChainCodec());
		vertx.deployVerticle("com.CAAS.network.verticle.SimulatorManager");

		//load textures
		ArrayList<Texture> bToggleTexList = new ArrayList<Texture>();
		bToggleTexList.add(new Texture(Gdx.files.internal("res/img/blocktoggle_0.png")));
		bToggleTexList.add(new Texture(Gdx.files.internal("res/img/blocktoggle_1.png")));
		bToggleTexList.add(new Texture(Gdx.files.internal("res/img/blocktoggle_2.png")));
		bToggleTexList.add(new Texture(Gdx.files.internal("res/img/blocktoggle_3.png")));
		
		ArrayList<Texture> startTexList = new ArrayList<Texture>();
		startTexList.add(new Texture(Gdx.files.internal("res/img/start_0.png")));
		startTexList.add(new Texture(Gdx.files.internal("res/img/start_1.png")));
		
		//read json file
		JSONObject inputJson = readFile();
		JSONArray nodeListJson = (JSONArray) inputJson.get("nodelist");

		//instantiate node info from json
		camList = CameraNode.getInstance();
		for(int i=0 ; i<nodeListJson.size() ; i++)
		{
			JSONObject nodeJson = (JSONObject)nodeListJson.get(i);
			double	x		= (double)(long)nodeJson.get("x");
			double	y		= (double)(long)nodeJson.get("y");
			double	v_x		= (double)(long)nodeJson.get("view_x");
			double	v_y		= (double)(long)nodeJson.get("view_y");
			double	vAngle	= (double)(long)nodeJson.get("view_angle");
			double	vDis	= (double)(long)nodeJson.get("view_distance");
			int		id		= (int)(long)	nodeJson.get("id");
			int		port	= (int)(long)	nodeJson.get("port");

			// 현재 이용가능한 카메라 노드 ID 리스트 생성
			global.availableCameraNodeID.push(id);
			camList.add(new CameraNode(x,y,v_x,v_y,vAngle,vDis,id,port));
		}
		global.camList = camList;
		//instantiate
		targetObj		= TargetObject.getInstance(eventBus);
		state			= new SimulatorState();
		spriteBatch		= new SpriteBatch();
		sRenderer		= new ShapeRenderer();
		blockToggleBtn	= new BlockToggleButton(600, 540 , 100, 40, bToggleTexList );
		startBtn		= new StartButton(600, 490, 100, 40, startTexList);
		font			= new BitmapFont(Gdx.files.internal("res/font/mspgothic.fnt"),Gdx.files.internal("res/font/mspgothic.png"),false);
		largeFont		= new BitmapFont(Gdx.files.internal("res/font/mspgothic.fnt"),Gdx.files.internal("res/font/mspgothic.png"),false);
		largeFont.getData().setScale(2f);
	}
	
	public void update()
	{
		inputUpdate();
		targetObj.update();
	}
	public void draw()
	{
		
		// shape rendering
		sRenderer.begin(ShapeType.Filled);
		//---------------------------------------
		
		sRenderer.setColor(0,0,0,1);
		sRenderer.rect(600, 0, 100,600);
		
		targetObj.draw(sRenderer);
		for(int i=0 ; i<camList.size() ; i++)
		{
			camList.get(i).draw(sRenderer);
		}
		
		//---------------------------------------
		sRenderer.end();
		
		//text, texture rendering
		spriteBatch.begin();
		//---------------------------------------
		blockToggleBtn.draw(spriteBatch);
		startBtn.draw(spriteBatch);
		font.draw(spriteBatch,"Time: "+ String.format("%.1f",SimulatorState.elapsedTime) , 5,595);
		font.draw(spriteBatch,"X: "+ String.format("%.1f",targetObj.pos.x) +" Y:"+ String.format("%.1f",targetObj.pos.y), 5,580);	

		if(TargetObject.getInstance(eventBus).inSight==true)
		{
			largeFont.setColor(1.0f,1.0f,1.0f,0.5f+0.5f*(float)Math.sin( (Math.PI/2) * (double)(realTime)*3 ));
			largeFont.draw(spriteBatch,"Recording...",490,595);
		}

		for(int i=0 ; i<camList.size() ; i++)
		{
			camList.get(i).drawBlock(spriteBatch,font);
		}
		//---------------------------------------
		spriteBatch.end();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		spriteBatch.dispose();
		sRenderer.dispose();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render() {
		// TODO Auto-generated method stub
		Gdx.gl.glClearColor(1,1,1,1);
		Gdx.gl.glClear(GL11.GL_COLOR_BUFFER_BIT );
		Gdx.gl.glEnable(GL11.GL_BLEND);
		Gdx.gl.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		if(SimulatorState.simulatorState==true)
			SimulatorState.elapsedTime += Gdx.graphics.getDeltaTime();
		realTime += Gdx.graphics.getDeltaTime();

		update();
		draw();
	}

	@Override
	public void resize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}
	public JSONObject readFile()
	{
		try
		{
			File file = new File("./input.txt");
			FileInputStream fis = new FileInputStream(file);
			byte[] bContent = new byte[(int)file.length()];
			fis.read(bContent);
			fis.close();
			
			String fileContent = new String( bContent  , "UTF-8");
			
			return (JSONObject)JSONValue.parse(fileContent);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public void inputUpdate()
	{
		//Direction key input  
		targetObj.dirNormal.x = 0;
		targetObj.dirNormal.y = 0;
		if(Gdx.input.isKeyPressed(Input.Keys.UP ) )
		{
			targetObj.dirNormal.y+=1;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN))
		{
			targetObj.dirNormal.y-=1;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT))
		{
			targetObj.dirNormal.x-=1;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT))
		{
			targetObj.dirNormal.x+=1;
		}
		targetObj.dirNormal.normalize();		
		
		//Button clicks
		blockToggleBtn.inputUpdate();
		startBtn.inputUpdate();
	}
	
}
