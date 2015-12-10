package com.slavchev.binmapeditor;


import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.sun.prism.GraphicsPipeline.ShaderType;

public class BinMapEditor extends ApplicationAdapter implements TextInputListener{
	//map data
	private SpriteBatch batch;
	private Map map;
	private OrthographicCamera camera;
	
	private String userInput = "";
	boolean opened = false;
	int cameraSpeed = 400;

	@Override
	public void create() {
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
        camera.update();
        
		map = new Map();
		batch = new SpriteBatch();
	}

	@Override
	public void render() {
		openAndSaveInputHandling();
				
		controlCamera();
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		
		Vector2 cameraOffset = new Vector2(
				camera.position.x - camera.viewportWidth / 2f,
				camera.position.y - camera.viewportHeight / 2f);
		
		processInput(cameraOffset);
		
		map.update(cameraOffset);
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		map.drawMap(batch);
		batch.end();
		map.drawCursor(camera, cameraOffset);
	}

	private void controlCamera() {
		if (Gdx.input.isKeyPressed(Keys.DOWN)) {	
			camera.translate(0, -cameraSpeed * Gdx.graphics.getDeltaTime());
		} else if (Gdx.input.isKeyPressed(Keys.UP)) {
			camera.translate(0, cameraSpeed * Gdx.graphics.getDeltaTime());
		} else if (Gdx.input.isKeyPressed(Keys.LEFT)) {	
			camera.translate(-cameraSpeed * Gdx.graphics.getDeltaTime(), 0);
		} else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
			camera.translate(cameraSpeed * Gdx.graphics.getDeltaTime(), 0);
		}
	}

	private void processInput(Vector2 cameraOffset) {
		if (Gdx.input.isKeyPressed(Keys.R)) {
			map.randomizeGroundTiles();
		}
		if (Gdx.input.isKeyPressed(Keys.M)) {
			map.markFillCoords(cameraOffset);
		}
		if (Gdx.input.isKeyPressed(Keys.F)) {
			map.fill(cameraOffset);
		}
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
		map.disposeShapeRenderer();
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
