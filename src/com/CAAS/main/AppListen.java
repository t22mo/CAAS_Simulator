package com.CAAS.main;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.CAAS.data.CameraNode;
import com.CAAS.data.TargetObject;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class AppListen implements ApplicationListener {

	float elapsedTime = 0;
	
	ArrayList<CameraNode>	camList;
	TargetObject			targetObj; 
	SpriteBatch				spriteBatch;
	ShapeRenderer			sRenderer;
	
	@Override
	public void create() {
		
		JSONObject inputJson = readFile();
		JSONArray nodeListJson = (JSONArray) inputJson.get("nodelist");
		
		//instantiate
		camList = new ArrayList<CameraNode>();
		
		for(int i=0 ; i<nodeListJson.size() ; i++)
		{
			JSONObject nodeJson = (JSONObject)nodeListJson.get(i);
			double x		= (double)(long)nodeJson.get("x");
			double y		= (double)(long)nodeJson.get("y");
			double v_x		= (double)(long)nodeJson.get("view_x");
			double v_y		= (double)(long)nodeJson.get("view_y");
			double vAngle	= (double)(long)nodeJson.get("view_angle");
			double vDis		= (double)(long)nodeJson.get("view_distance");
			int radius		= (int)(long)	nodeJson.get("radius");
			
			camList.add(new CameraNode(x,y,v_x,v_y,vAngle,vDis,radius));
		}
		targetObj	= new TargetObject();
		spriteBatch	= new SpriteBatch();
		sRenderer	= new ShapeRenderer();
	}
	
	public void update()
	{
		inputUpdate();
		targetObj.update();
	}
	public void draw()
	{

		/*
		
		spriteBatch.begin();
		targetObj.draw(spriteBatch);
		for(int i=0 ; i<camList.size() ; i++)
		{
			camList.get(i).draw(spriteBatch);
		}
		spriteBatch.end();
		*/
		
		sRenderer.begin(ShapeType.Filled);
		
		for(int i=0 ; i<camList.size() ; i++)
		{
			camList.get(i).draw(sRenderer);
		}
		targetObj.draw(sRenderer);
		
		sRenderer.end();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render() {
		// TODO Auto-generated method stub
		Gdx.gl.glClearColor(0,0,0,1);
		Gdx.gl.glClear(GL11.GL_COLOR_BUFFER_BIT );
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
	}
	
}
