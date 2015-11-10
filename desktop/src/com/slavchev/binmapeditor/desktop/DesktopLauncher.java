package com.slavchev.binmapeditor.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.slavchev.binmapeditor.BinMapEditor;

public class DesktopLauncher {
	@SuppressWarnings("unused")
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "BinMapEditor";
		config.width = 1280;
		config.height = 960;
		new LwjglApplication(new BinMapEditor(), config);
	}
}
