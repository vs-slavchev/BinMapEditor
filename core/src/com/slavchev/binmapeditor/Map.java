package com.slavchev.binmapeditor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class Map {

	// map
	private String tilesetName;
	private int width, height, factor;
	private byte[][] map;
	

	// drawing on the screen
	private float scale = 0.5f;
	private float tileSetWidth, tileSetHeight;
	private int tileSize = 32;
	private final int numTilesPerRow = 10;
	private Sprite tileSet;
	private TextureRegion tileTexture;

	// GUI
	private int selectedTileID;
	
	public Map() {
		loadProperties();
		
		tileSet = new Sprite(new Texture(tilesetName));
		tileSet.setScale(scale);
		tileSetWidth = tileSet.getWidth() * scale;
		tileSetHeight = tileSet.getHeight() * scale;
		tileSet.setPosition( 0 - tileSetWidth * scale, Gdx.graphics.getHeight() - tileSetHeight*(scale + 1));
		
		map = new byte[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				map[i][j] = toSignedByte(0);
			}
		}
	}

	public void update(int offsetX, int offsetY) {
		int mouseX = Gdx.input.getX() + offsetX;
		int mouseY = Gdx.input.getY() - offsetY;

		tileSet.setPosition( offsetX - tileSetWidth * scale,
				offsetY + Gdx.graphics.getHeight() - tileSetHeight*(scale + 1));
		if (Gdx.input.justTouched() && Gdx.input.isButtonPressed(Buttons.RIGHT)) {

			// if selecting a tile
			if (Gdx.input.getX() < tileSetWidth && Gdx.input.getY() < tileSetHeight) {
				selectedTileID = (int) (Gdx.input.getX() / (tileSize * scale)
						+ (Gdx.input.getY() / (int)(tileSize * scale)) * numTilesPerRow);
			}
		}
		if (Gdx.input.isTouched() && Gdx.input.isButtonPressed(Buttons.LEFT)) {
			if (mouseX >= tileSetWidth) {
				int mapYindex = mouseY / (int)(tileSize * scale);
				int mapXindex = (int) ((mouseX - tileSetWidth) / (int)(tileSize * scale));

				if (mapYindex >= map.length || mapXindex >= map[0].length
						|| mapYindex <= 0 || mapXindex <= 0) {
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
				batch.draw(tileTexture, tileSetWidth + v * tileSize * scale,
						Gdx.graphics.getHeight() - ((u + 1) * tileSize * scale),
						tileSize * scale, tileSize * scale);
			}
		}

		tileSet.draw(batch);
	}

	public void writeMap(String mapName) {
		FileHandle file = Gdx.files.local(mapName + ".bin");
		file.writeBytes(new byte[] { (byte) (width / factor), (byte) (height / factor), (byte) factor }, false);
		for (int k = 0; k < height; k++) {
			file.writeBytes(map[k], true);
		}
	}

	@SuppressWarnings("static-method")
	public void readMap(String mapName) {
		FileHandle file2 = Gdx.files.internal(mapName + ".bin");
		
		byte[] bytes;
		int mapWidth, mapHeight, multFactor;
		byte[][] map2 = null;
		
		try {
		bytes = file2.readBytes();
		mapWidth = bytes[0];
		mapHeight = bytes[1];
		multFactor = bytes[2];
		mapWidth *= multFactor;
		mapHeight *= multFactor;

		map2 = new byte[mapHeight][mapWidth];
		for (int i = 0; i < mapHeight; i++) {
			for (int j = 0; j < mapWidth; j++) {
				map2[i][j] = bytes[3 + i * mapWidth + j];
			}
		}
		} catch (GdxRuntimeException ex) {
			System.out.println("Error reading from file!");
			return;
		}
		map = map2;
		//printMap(mapWidth, mapHeight, map2);
	}

	@SuppressWarnings("static-method")
	private void printMap(int mapWidth, int mapHeight, byte[][] map) {
		System.out.println(mapWidth + " " + mapHeight);
		for (int k = 0; k < mapHeight; k++) {
			for (int l = 0; l < mapWidth; l++) {
				int tileID = map[k][l];
				System.out.print((tileID + 127) + " ");
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

			input = new FileInputStream("mapconfig.properties");
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

	/* takes: x and y and returns the tileID as an int */
	public int getTileID(int x, int y) {
		return map[y][x] + 127;
	}

	public Sprite getTileSet() {
		return tileSet;
	}
}
