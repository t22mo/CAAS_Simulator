package com.CAAS.main;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.CAAS.data.BlockToggleButton;
import com.CAAS.data.CameraNode;
import com.CAAS.data.SimulatorState;
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

	float elapsedTime = 0;
	
	ArrayList<CameraNode>	camList;
	TargetObject			targetObj; 
	SpriteBatch				spriteBatch;
	ShapeRenderer			sRenderer;
	BitmapFont				font;
	SimulatorState			state;
	BlockToggleButton		blockToggleBtn;
	
	@Override
	public void create() {
		
		ArrayList<Texture> bToggleTexList = new ArrayList<Texture>();
		bToggleTexList.add(new Texture(Gdx.files.internal("res/img/blocktoggle_0.png")));
		bToggleTexList.add(new Texture(Gdx.files.internal("res/img/blocktoggle_1.png")));
		bToggleTexList.add(new Texture(Gdx.files.internal("res/img/blocktoggle_2.png")));
		bToggleTexList.add(new Texture(Gdx.files.internal("res/img/blocktoggle_3.png")));
		
		//read json file
		JSONObject inputJson = readFile();
		JSONArray nodeListJson = (JSONArray) inputJson.get("nodelist");
		
		//instantiate node info from json
		camList = new ArrayList<CameraNode>();
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

			
			camList.add(new CameraNode(x,y,v_x,v_y,vAngle,vDis,id));
		}
		
		//instantiate
		state			= new SimulatorState();
		targetObj		= new TargetObject();
		spriteBatch		= new SpriteBatch();
		sRenderer		= new ShapeRenderer();
		blockToggleBtn	= new BlockToggleButton(600, 540 , 100, 40, bToggleTexList );
		font			= new BitmapFont(Gdx.files.internal("res/font/mspgothic.fnt"),Gdx.files.internal("res/font/mspgothic.png"),false);
		//font.getData().setScale(0.9f);
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
		font.draw(spriteBatch,"Time: "+ String.format("%.1f",elapsedTime) , 5,595);	
		font.draw(spriteBatch,"X: "+ String.format("%.1f",targetObj.pos.x) +" Y:"+ String.format("%.1f",targetObj.pos.y), 5,580);	
		
		for(int i=0 ; i<camList.size() ; i++)
		{
			camList.get(i).draw(spriteBatch,font);
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
		elapsedTime += Gdx.graphics.getDeltaTime();
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
		//방향키 처리
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
		
		//버튼 입력 처리
		blockToggleBtn.inputUpdate();
	}
	
}
