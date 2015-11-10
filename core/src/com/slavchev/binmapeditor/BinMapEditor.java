package com.slavchev.binmapeditor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BinMapEditor extends ApplicationAdapter implements TextInputListener{
	//map data
	SpriteBatch batch;
	Map map;
	
	OrthographicCamera camera;
	
	String userInput = "";
	boolean opened = false;

	@Override
	public void create() {
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
		//camera.setToOrtho(false, camera.viewportWidth / 2f, camera.viewportHeight / 2f);
        camera.update();
        
		map = new Map();
		batch = new SpriteBatch();
	}

	@Override
	public void render() {
		openAndSaveInputHandling();
				
		if (Gdx.input.isKeyPressed(Keys.DOWN)) {	
			camera.translate(0, -180 * Gdx.graphics.getDeltaTime());
		} else if (Gdx.input.isKeyPressed(Keys.UP)) {
			camera.translate(0, 180 * Gdx.graphics.getDeltaTime());
		} else if (Gdx.input.isKeyPressed(Keys.LEFT)) {	
			camera.translate(-180 * Gdx.graphics.getDeltaTime(), 0);
		} else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
			camera.translate(180 * Gdx.graphics.getDeltaTime(), 0);
		}
		camera.update();
		
		map.update((int)(camera.position.x - camera.viewportWidth / 2f),
				(int)(camera.position.y - camera.viewportHeight / 2f));
		batch.setProjectionMatrix(camera.combined);
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		map.drawMap(batch);
		batch.end();
	}

	private void openAndSaveInputHandling() {
		if (Gdx.input.isKeyJustPressed(Keys.O)) {
			opened = true;
			Gdx.input.getTextInput(this, "Open map file by name", "map", null);
		} else if (Gdx.input.isKeyJustPressed(Keys.S)) {
			opened = false;
			Gdx.input.getTextInput(this, "Save map file by name", "map", null);
		}
	}
	
	public void dispose() {
		batch.dispose();
		map.getTileSet().getTexture().dispose();
	}
	
	@Override
	public void input(String text) {
		userInput = text;
		if (opened) {
			map.readMap(userInput);
		} else {
			map.writeMap(userInput);
		}
	}

	@Override
	public void canceled() {
		userInput = "";		
	}
}
