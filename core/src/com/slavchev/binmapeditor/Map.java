package com.slavchev.binmapeditor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class Map {

	// map
	private String tilesetName;
	private int width, height, factor;
	private byte[][] map;
	
	// drawing on the screen
	private int tileSize = 32;
	private final int numTilesPerRow = 10;
	private Sprite tileSet;
	private TextureRegion tileTexture;

	// GUI
	private int selectedTileID;
	private Vector2 fillMark, cursorIndices;
	private ShapeRenderer shapeRenderer;
	
	// files
	private String dataFilePath = "data/";
	
	public Map() {
		loadProperties();
		shapeRenderer = new ShapeRenderer();
		
		tileSet = new Sprite(new Texture(dataFilePath + tilesetName));
		tileSet.setPosition( 0, Gdx.graphics.getHeight() - tileSet.getHeight());
		
		map = new byte[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				map[i][j] = toSignedByte(0);
			}
		}
	}

	public void update(float offsetX, float offsetY) {
		float mouseX = Gdx.input.getX() + offsetX;
		float mouseY = Gdx.input.getY() - offsetY;

		cursorIndices = calculateIndices((int)mouseX, (int)mouseY);
		
		tileSet.setPosition(offsetX, offsetY + Gdx.graphics.getHeight() - tileSet.getHeight());
		if (Gdx.input.justTouched() && Gdx.input.isButtonPressed(Buttons.RIGHT)) {

			// if selecting a tile
			if (Gdx.input.getX() < tileSet.getWidth() && Gdx.input.getY() < tileSet.getHeight()) {
				selectedTileID = Gdx.input.getX() / tileSize
						+ (Gdx.input.getY() / tileSize) * numTilesPerRow;
			}
		}
		if (Gdx.input.isTouched() && Gdx.input.isButtonPressed(Buttons.LEFT)) {
			if (mouseX >= tileSet.getWidth()) {
				
				int mapYindex = (int) cursorIndices.y;
				int mapXindex = (int) cursorIndices.x;
				

				if (mapYindex >= map.length || mapXindex >= map[0].length
						|| mapYindex < 0 || mapXindex < 0) {
					return;
				}
				map[mapYindex][mapXindex] = toSignedByte(selectedTileID);
			}
		}
	}

	public void drawMap(SpriteBatch batch) {
		// go trough the on-screen map and render the corresponding tile
		for (int u = 0; u < height; u++) {
			for (int v = 0; v < width; v++) {
				int tileID = getTileID(v, u);
				tileTexture = new TextureRegion(tileSet,
						(tileID % numTilesPerRow) * tileSize,
						(tileID / numTilesPerRow) * tileSize, tileSize, tileSize);
				batch.draw(tileTexture, tileSet.getWidth() + v * tileSize,
						Gdx.graphics.getHeight() - ((u + 1) * tileSize),
						tileSize, tileSize);
			}
		}
		
		tileSet.draw(batch);
	}
	
	public void drawCursor(OrthographicCamera camera) {
		if (Gdx.input.getX() >= tileSet.getWidth()) {
			shapeRenderer.setProjectionMatrix(camera.combined);
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(0, 1, 0, 1);
			shapeRenderer.rect(tileSet.getWidth() + cursorIndices.x * tileSize,
					Gdx.graphics.getHeight() - ((cursorIndices.y + 1) * tileSize),
					tileSize, tileSize);
			shapeRenderer.end();
			
			if (fillMark != null) {
				shapeRenderer.begin(ShapeType.Filled);
				shapeRenderer.setColor(1, 0, 0, 1);
				shapeRenderer.rect(tileSet.getWidth() + fillMark.x * tileSize,
						Gdx.graphics.getHeight() - ((fillMark.y + 1) * tileSize),
						tileSize, tileSize);
				shapeRenderer.end();
			}
		}
	}

	public void writeMap(String mapName) {
		FileHandle file = Gdx.files.local(mapName + ".bin");
		file.writeBytes(new byte[] { (byte) (width / factor), (byte) (height / factor), (byte) factor }, false);
		for (int k = 0; k < height; k++) {
			file.writeBytes(map[k], true);
		}
	}

	public void readMap(String mapName) {
		FileHandle file2 = Gdx.files.internal(mapName + ".bin");

		byte[] bytes;
		byte[][] map2 = null;

		try {
			bytes = file2.readBytes();
			width = bytes[0];
			height = bytes[1];
			factor = bytes[2];
			width *= factor;
			height *= factor;

			map2 = new byte[height][width];
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					map2[i][j] = bytes[3 + i * width + j];
				}
			}
		} catch (GdxRuntimeException ex) {
			System.out.println("Error reading from file!");
			return;
		}
		map = map2;
		// printMap();
	}

	@SuppressWarnings("unused")
	private void printMap() {
		System.out.println(width + " " + height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				System.out.print(getTileID(x, y) + " ");
			}
			System.out.println();
		}
	}

	/* takes: 0-255; returns -128 to 127 */
	@SuppressWarnings("static-method")
	private byte toSignedByte(int value) {
		if (value < 0 || value > 255) {
			System.out.println("Cannot convert " + value + " to signed byte!");
		}
		return (byte) (value - 127);
	}
	
	/* load setup info from the properties file */
	public void loadProperties() {
		Properties properties = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream(dataFilePath + "mapconfig.properties");
			properties.load(input);

			tilesetName = properties.getProperty("tilesetName");
			width = Integer.valueOf(properties.getProperty("width"));
			height = Integer.valueOf(properties.getProperty("height"));
			factor = Integer.valueOf(properties.getProperty("factor"));

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void randomizeGroundTiles() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (getTileID(x, y) < 9) {
					setTileID(x, y, (int)(Math.random() * 9));
				} else if (getTileID(x, y) >= 10 && getTileID(x, y) < 19) {
					setTileID(x, y, (int)(10 + Math.random() * 9));
				} else if (getTileID(x, y) >= 20 && getTileID(x, y) < 29) {
					setTileID(x, y, (int)(20 + Math.random() * 9));
				} else if (getTileID(x, y) >= 30 && getTileID(x, y) < 39) {
					setTileID(x, y, (int)(30 + Math.random() * 9));
				}
			}
		}
		//printMap();
	}
	
	// mark the coords of the first fill mark
	public void markFillCoords(float offsetX, float offsetY) {
		int mouseX = (int) (Gdx.input.getX() + offsetX);
		int mouseY = (int) (Gdx.input.getY() - offsetY);
		
		fillMark = calculateIndices(mouseX, mouseY);
	}
	
	public Vector2 calculateIndices(int x, int y) {
		return new Vector2((int) ((x - tileSet.getWidth()) / tileSize), y/tileSize );
	}
	
	
	public void fill(float offsetX, float offsetY) {
		if (fillMark == null) {
			return;
		}
		int mouseX = (int) (Gdx.input.getX() + offsetX);
		int mouseY = (int) (Gdx.input.getY() - offsetY);
		
		Vector2 mapIndices = calculateIndices(mouseX, mouseY);
		
		int smallerX = (int) ((mapIndices.x < fillMark.x) ? mapIndices.x : fillMark.x);
		int biggerX = (int) ((mapIndices.x < fillMark.x) ? fillMark.x : mapIndices.x);
		int smallerY = (int) ((mapIndices.y < fillMark.y) ? mapIndices.y : fillMark.y);
		int biggerY = (int) ((mapIndices.y < fillMark.y) ? fillMark.y : mapIndices.y);
		
		for (int i = smallerX; i <= biggerX; i++) {
			for (int j = smallerY; j <= biggerY; j++) {
				setTileID(i, j, selectedTileID);
			}
		}
		
	}

	/* takes: x and y and returns the tileID as an int */
	public int getTileID(int x, int y) {
		if (x < 0 || y < 0
				|| x >= map[0].length
				|| y >= map.length) {
			System.out.println(x + "; " + y + "getting invalid tileID!");
			return 0;
		}
		return map[y][x] + 127;
	}
	
	/* modifies the map */
	public void setTileID(int xIndex, int yIndex, int tileID) {
		if (xIndex < 0 || xIndex >= map[0].length
				|| yIndex < 0 || yIndex >= map.length) {
			return;
		}
		map[yIndex][xIndex] = toSignedByte(tileID);
	}
	
	public int getTotalHeight() {
		return map.length * tileSize;
	}

	public Sprite getTileSet() {
		return tileSet;
	}

	public int getTileSize() {
		return tileSize;
	}

	public void disposeShapeRenderer() {
		shapeRenderer.dispose();
	}
}
