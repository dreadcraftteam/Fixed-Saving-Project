package com.mojang.minecraft.gui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.io.File;

public class LoadLevelScreen extends Screen implements Runnable {
	private Screen parent;
	private boolean finished = false;
	private boolean loaded = false;
	private String[] levels = null;
	private String status = "";
	protected String title = "Load level";

	public LoadLevelScreen(Screen screen1) {
		this.parent = screen1;
	}

	public void run() {
		try {
			this.status = "Getting level list..";
			File savesDir = new File("saves");
			if (!savesDir.exists()) {
				savesDir.mkdir();
			}
			
			File[] saveFiles = savesDir.listFiles((dir, name) -> name.endsWith(".dat"));
			this.levels = new String[5];
			
			for (int i = 0; i < 5; i++) {
				if (i < saveFiles.length) {
					String fileName = saveFiles[i].getName();
					this.levels[i] = fileName.substring(0, fileName.length() - 4);
				} else {
					this.levels[i] = "-";
				}
			}
			
			this.setLevels(this.levels);
			this.loaded = true;
		} catch (Exception exception2) {
			exception2.printStackTrace();
			this.status = "Failed to load levels";
			this.finished = true;
		}
	}

	protected void setLevels(String[] string1) {
		for(int i2 = 0; i2 < 5; ++i2) {
			((Button)this.buttons.get(i2)).enabled = !string1[i2].equals("-");
			((Button)this.buttons.get(i2)).msg = string1[i2];
			((Button)this.buttons.get(i2)).visible = true;
		}
	}

	public final void init() {
		(new Thread(this)).start();

		for(int i1 = 0; i1 < 5; ++i1) {
			this.buttons.add(new Button(i1, this.width / 2 - 100, this.height / 4 + i1 * 24, 200, 20, "---"));
			((Button)this.buttons.get(i1)).visible = false;
		}

		this.buttons.add(new Button(5, this.width / 2 - 100, this.height / 4 + 144, 200, 20, "Cancel"));
	}

	protected final void buttonClicked(Button button1) {
		if(button1.enabled) {
			if(this.loaded && button1.id < 5) {
				this.loadLevel(button1.id);
			}

			if(this.finished || this.loaded && button1.id == 5) {
				this.minecraft.setScreen(this.parent);
			}
		}
	}

	protected void loadLevel(int i1) {
		String levelName = ((Button)this.buttons.get(i1)).msg;
		this.minecraft.loadLevel(levelName, 0);  
		this.minecraft.setScreen((Screen)null);
		this.minecraft.grabMouse();
	}
	
	public final void render(int i1, int i2) {
		fillGradient(0, 0, this.width, this.height, 1610941696, -1607454624);
		this.drawCenteredString(this.title, this.width / 2, 40, 0xFFFFFF);
		if(!this.loaded) {
			this.drawCenteredString(this.status, this.width / 2, this.height / 2 - 4, 0xFFFFFF);
		}

		super.render(i1, i2);
	}
}