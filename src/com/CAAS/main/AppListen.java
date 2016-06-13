package com.CAAS.main;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import com.CAAS.data.*;
import com.CAAS.network.model.Global;
import com.CAAS.network.protocol.ChainMessageProtocol;
import com.CAAS.network.protocol.HashChainCodec;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import it.polito.appeal.traci.SumoTraciConnection;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.lwjgl.opengl.GL11;
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
	SpriteBatch				spriteBatch2;
	ShapeRenderer			sRenderer;
	BitmapFont				font,largeFont;
	SimulatorState			state;
	BlockToggleButton		blockToggleBtn;
	StartButton				startBtn;
	StepButton				stepBtn;
	Texture					backgroundTex;
	Texture					dummy;

	//Networking Variable
	Global global = Global.getInstance();
	Vertx vertx;
	EventBus eventBus;
	float realTime=0;

	//SUMO variable
	SumoTraciConnection conn;
	private static final Logger log = LogManager.getLogger(AppListen.class);
	static String sumo_bin = "D:/Program Files (x86)/DLR/Sumo/bin/sumo-gui.exe";
	static String config_file = "C:/Users/T22mo/workspace/caas_trass/KU_CAAS_traas/net/osm.sumocfg";
	static double step_length = 0.01;
	String v_crm;

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

		ArrayList<Texture> stepTexList = new ArrayList<Texture>();
		stepTexList.add(new Texture(Gdx.files.internal("res/img/step_0.png")));
		stepTexList.add(new Texture(Gdx.files.internal("res/img/step_1.png")));

		backgroundTex = new Texture(Gdx.files.internal("res/img/map.png"));
		dummy = new Texture(Gdx.files.internal("res/img/thief.png"));

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
			boolean rotate = (boolean)	nodeJson.get("rotate");
			String	temp1	= (String)		nodeJson.get("temp1");
			int		temp2	= (int)(long)		nodeJson.get("temp2");

			// 현재 이용가능한 카메라 노드 ID 리스트 생성
			global.availableCameraNodeID.push(id);
			camList.add(new CameraNode(x,y,v_x,v_y,vAngle,vDis,id,port,rotate,temp1,temp2,eventBus));
		}
		global.MAX_PORT_NUMBER = 1000 + camList.size();
		global.camList = camList;
		//instantiate
		targetObj		= TargetObject.getInstance(eventBus);
		state			= new SimulatorState();
		spriteBatch		= new SpriteBatch();
		spriteBatch2	= new SpriteBatch();
		sRenderer		= new ShapeRenderer();
		blockToggleBtn	= new BlockToggleButton(SimulatorState.mapWidth, SimulatorState.mapHeight - 60 , 100, 40, bToggleTexList );
		startBtn		= new StartButton(SimulatorState.mapWidth, SimulatorState.mapHeight - 110, 100, 40, startTexList);
		stepBtn			= new StepButton(SimulatorState.mapWidth, SimulatorState.mapHeight - 160, 100, 40, stepTexList);
		font			= new BitmapFont(Gdx.files.internal("res/font/mspgothic.fnt"),Gdx.files.internal("res/font/mspgothic.png"),false);
		largeFont		= new BitmapFont(Gdx.files.internal("res/font/mspgothic.fnt"),Gdx.files.internal("res/font/mspgothic.png"),false);
		largeFont.getData().setScale(2f);

		//Start Sumo
		conn = new SumoTraciConnection(sumo_bin, config_file);
		conn.addOption("step-length", "0.1"); //timestep 100 ms
		v_crm = "veh0";

		try {
			conn.runServer();

			//load routes and initialize the simulation
			conn.do_timestep();
			conn.do_timestep();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void update()
	{
		inputUpdate();
		targetObj.update(conn,v_crm);
		for(int i=0 ; i<camList.size() ; i++)
		{
			camList.get(i).update();
		}
	}
	public void draw()
	{
		spriteBatch2.enableBlending();
		spriteBatch2.begin();
		spriteBatch2.draw(backgroundTex,0,0,SimulatorState.mapWidth,SimulatorState.mapHeight);
		spriteBatch2.end();


		spriteBatch.begin();


		// shape rendering
		sRenderer.begin(ShapeType.Filled);
		//---------------------------------------

		sRenderer.setColor(0,0,0,1);
		targetObj.draw(sRenderer);
		for(int i=0 ; i<camList.size() ; i++)
		{
			camList.get(i).draw(sRenderer);
		}

		//---------------------------------------

		
		//text, texture rendering
		//---------------------------------------


		font.draw(spriteBatch,"Time: "+ String.format("%.1f",SimulatorState.elapsedTime) , 5,SimulatorState.mapHeight - 5);
		font.draw(spriteBatch,"X: "+ String.format("%.1f",targetObj.pos.x) +" Y:"+ String.format("%.1f",targetObj.pos.y), 5,SimulatorState.mapHeight - 20);


		blockToggleBtn.draw(spriteBatch);
		startBtn.draw(spriteBatch);
		stepBtn.draw(spriteBatch);

		if(TargetObject.getInstance(eventBus).inSight==true)
		{
			largeFont.setColor(1.0f,1.0f,1.0f,0.5f+0.5f*(float)Math.sin( (Math.PI/2) * (double)(realTime)*3 ));
			largeFont.draw(spriteBatch,"Recording...",SimulatorState.mapWidth - 110,SimulatorState.mapHeight - 5);
		}


		for(int i=0 ; i<camList.size() ; i++)
		{
			camList.get(i).drawBlock(spriteBatch,font);
		}
		targetObj.draw(spriteBatch);

		spriteBatch.draw(dummy,0,0,0,0);


		//---------------------------------------
		sRenderer.end();
		spriteBatch.end();


	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		spriteBatch.dispose();
		sRenderer.dispose();
		conn.close();
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

	public void inputUpdate() {
		//Direction key input  
		targetObj.dirNormal.x = 0;
		targetObj.dirNormal.y = 0;

		if (Gdx.input.isKeyPressed(Input.Keys.A))
		{
			System.out.println("x= " + Gdx.input.getX() + " y= " + Gdx.input.getY() );

		}

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
		if(Gdx.input.isKeyPressed(Input.Keys.C))
		{
			camList.get(2).rotateViewVector((double)-90);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.X))
		{
			camList.get(2).rotateViewVector((double)0);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.Z))
		{
			camList.get(2).rotateViewVector((double)90);
		}
		targetObj.dirNormal.normalize();		
		
		//Button clicks
		blockToggleBtn.inputUpdate();
		startBtn.inputUpdate();
		stepBtn.inputUpdate();
	}
	public void parseObjectRoute()
	{
		try
		{
			File file = new File("./route.txt");
			FileInputStream fis = new FileInputStream(file);
			byte[] bContent = new byte[(int)file.length()];
			fis.read(bContent);
			fis.close();

			String fileContent = new String( bContent  , "UTF-8");
			String[] list = fileContent.split("\\\n");
			for(int i=0 ; i<list.length ; i++)
			{
				String[] line = list[i].split("\\ ");
				double x = Double.parseDouble(line[3]);
				double y = Double.parseDouble(line[5]);
				targetObj.addRoute(new Vector2D(x,y));
				camList.get(0).addRoute(new Vector2D(x,y));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
