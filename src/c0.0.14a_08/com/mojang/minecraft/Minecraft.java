package com.mojang.minecraft;

import com.mojang.minecraft.character.Zombie;
import com.mojang.minecraft.gui.Font;
import com.mojang.minecraft.gui.PauseScreen;
import com.mojang.minecraft.gui.Screen;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.LevelIO;
import com.mojang.minecraft.level.levelgen.LevelGen;
import com.mojang.minecraft.level.tile.Tile;
import com.mojang.minecraft.particle.Particle;
import com.mojang.minecraft.particle.ParticleEngine;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.player.MovementInputFromOptions;
import com.mojang.minecraft.player.Player;
import com.mojang.minecraft.renderer.Chunk;
import com.mojang.minecraft.renderer.DirtyChunkSorter;
import com.mojang.minecraft.renderer.Frustum;
import com.mojang.minecraft.renderer.LevelRenderer;
import com.mojang.minecraft.renderer.Tesselator;
import com.mojang.minecraft.renderer.Textures;

import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.JOptionPane;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public final class Minecraft implements Runnable {
	private boolean fullscreen = false;
	public int width;
	public int height;
	private FloatBuffer fogColor0 = BufferUtils.createFloatBuffer(4);
	private FloatBuffer fogColor1 = BufferUtils.createFloatBuffer(4);
	private Timer timer = new Timer(20.0F);
	private Level level;
	private LevelRenderer levelRenderer;
	private Player player;
	private int paintTexture = 1;
	private ParticleEngine particleEngine;
	public User user = null;
	public String minecraftUri;
	private Canvas parent;
	public boolean appletMode = false;
	public volatile boolean pause = false;
	private Cursor emptyCursor;
	private int yMouseAxis = 1;
	private Textures textures;
	public Font font;
	private int editMode = 0;
	private Screen screen = null;
	private LevelIO levelIo = new LevelIO(this);
	private LevelGen levelGen = new LevelGen(this);
	private int ticksRan = 0;
	public String loadMapUser = null;
	public int loadMapID = 0;
	private Robot robot;
	private static final int[] creativeTiles = new int[]{Tile.rock.id, Tile.dirt.id, Tile.stoneBrick.id, Tile.wood.id, Tile.bush.id, Tile.log.id, Tile.leaf.id, Tile.sand.id, Tile.gravel.id};
	private float fogColorRed = 0.5F;
	private float fogColorGreen = 0.8F;
	private float fogColorBlue = 1.0F;
	private volatile boolean running = false;
	private String fpsString = "";
	private boolean mouseGrabbed = false;
	private int prevFrameTime = 0;
	private float renderDistance = 0.0F;
	private IntBuffer viewportBuffer = BufferUtils.createIntBuffer(16);
	private IntBuffer selectBuffer = BufferUtils.createIntBuffer(2000);
	private HitResult hitResult = null;
	private volatile int unusedInt1 = 0;
	private volatile int unusedInt2 = 0;
	private FloatBuffer lb = BufferUtils.createFloatBuffer(16);
	private String title = "";
	private String text = "";

	public Minecraft(Canvas canvas1, int i2, int i3, boolean z4) {
		this.parent = canvas1;
		this.width = i2;
		this.height = i3;
		this.fullscreen = false;
		this.textures = new Textures();
		if(canvas1 != null) {
			try {
				this.robot = new Robot();
				return;
			} catch (AWTException aWTException5) {
				aWTException5.printStackTrace();
			}
		}

	}

	public final void setScreen(Screen screen1) {
		if(this.screen != null) {
			this.screen.closeScreen();
		}

		this.screen = screen1;
		if(screen1 != null) {
			int i2 = this.width * 240 / this.height;
			int i3 = this.height * 240 / this.height;
			screen1.init(this, i2, i3);
		}

	}

	private static void checkGlError(String string0) {
		int i1;
		if((i1 = GL11.glGetError()) != 0) {
			String string2 = GLU.gluErrorString(i1);
			System.out.println("########## GL ERROR ##########");
			System.out.println("@ " + string0);
			System.out.println(i1 + ": " + string2);
			System.exit(0);
		}

	}

	public final void destroy() {
		Minecraft minecraft2;
		if(!(minecraft2 = this).appletMode) {
			try {
				LevelIO.save(minecraft2.level, new FileOutputStream(new File("level.dat")));
			} catch (Exception exception1) {
				exception1.printStackTrace();
			}
		}

		Mouse.destroy();
		Keyboard.destroy();
		Display.destroy();
	}

	public final void run() {
		this.running = true;

		try {
			Minecraft minecraft4 = this;
			this.fogColor0.put(new float[]{this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F});
			this.fogColor0.flip();
			this.fogColor1.put(new float[]{(float)14 / 255.0F, (float)11 / 255.0F, (float)10 / 255.0F, 1.0F});
			this.fogColor1.flip();
			if(this.parent != null) {
				Display.setParent(this.parent);
			} else if(this.fullscreen) {
				Display.setFullscreen(true);
				this.width = Display.getDisplayMode().getWidth();
				this.height = Display.getDisplayMode().getHeight();
			} else {
				Display.setDisplayMode(new DisplayMode(this.width, this.height));
			}

			Display.setTitle("Minecraft 0.0.14a_08");

			try {
				Display.create();
			} catch (LWJGLException lWJGLException23) {
				lWJGLException23.printStackTrace();

				try {
					Thread.sleep(1000L);
				} catch (InterruptedException interruptedException22) {
				}

				Display.create();
			}

			Keyboard.create();
			Mouse.create();

			try {
				Controllers.create();
			} catch (Exception exception21) {
				exception21.printStackTrace();
			}

			checkGlError("Pre startup");
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			GL11.glClearDepth(1.0D);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDepthFunc(GL11.GL_LEQUAL);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
			GL11.glCullFace(GL11.GL_BACK);
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			checkGlError("Startup");
			this.font = new Font("/default.gif", this.textures);
			IntBuffer intBuffer8;
			(intBuffer8 = BufferUtils.createIntBuffer(256)).clear().limit(256);
			GL11.glViewport(0, 0, this.width, this.height);
			boolean z9 = false;

			try {
				if(minecraft4.loadMapUser != null) {
					z9 = minecraft4.loadLevel(minecraft4.loadMapUser, minecraft4.loadMapID);
				} else if(!minecraft4.appletMode) {
					Level level10 = null;
					if(!(z9 = (level10 = minecraft4.levelIo.load(new FileInputStream(new File("level.dat")))) != null)) {
						z9 = (level10 = minecraft4.levelIo.loadLegacy(new FileInputStream(new File("level.dat")))) != null;
					}

					minecraft4.setLevel(level10);
				}
			} catch (Exception exception20) {
				exception20.printStackTrace();
				z9 = false;
			}

			if(!z9) {
				this.generateLevel(1);
			}

			this.levelRenderer = new LevelRenderer(this.textures);
			this.particleEngine = new ParticleEngine(this.level, this.textures);
			this.player = new Player(this.level, new MovementInputFromOptions());
			this.player.resetPos();
			if(this.level != null) {
				this.levelRenderer.setLevel(this.level);
			}

			if(this.appletMode) {
				try {
					minecraft4.emptyCursor = new Cursor(16, 16, 0, 0, 1, intBuffer8, (IntBuffer)null);
				} catch (LWJGLException lWJGLException19) {
					lWJGLException19.printStackTrace();
				}
			}

			checkGlError("Post startup");
		} catch (Exception exception26) {
			exception26.printStackTrace();
			JOptionPane.showMessageDialog((Component)null, exception26.toString(), "Failed to start Minecraft", 0);
			return;
		}

		long j1 = System.currentTimeMillis();
		int i3 = 0;

		try {
			while(this.running) {
				if(this.pause) {
					Thread.sleep(100L);
				} else {
					if(this.parent == null && Display.isCloseRequested()) {
						this.running = false;
					}

					Timer timer27 = this.timer;
					long j7;
					long j29 = (j7 = System.nanoTime()) - timer27.lastTime;
					timer27.lastTime = j7;
					if(j29 < 0L) {
						j29 = 0L;
					}

					if(j29 > 1000000000L) {
						j29 = 1000000000L;
					}

					timer27.fps += (float)j29 * timer27.timeScale * timer27.ticksPerSecond / 1.0E9F;
					timer27.ticks = (int)timer27.fps;
					if(timer27.ticks > 100) {
						timer27.ticks = 100;
					}

					timer27.fps -= (float)timer27.ticks;
					timer27.a = timer27.fps;

					for(int i28 = 0; i28 < this.timer.ticks; ++i28) {
						++this.ticksRan;
						this.tick();
					}

					checkGlError("Pre render");
					this.render(this.timer.a);
					checkGlError("Post render");
					++i3;

					while(System.currentTimeMillis() >= j1 + 1000L) {
						this.fpsString = i3 + " fps, " + Chunk.updates + " chunk updates";
						Chunk.updates = 0;
						j1 += 1000L;
						i3 = 0;
					}
				}
			}

			return;
		} catch (Exception exception24) {
			exception24.printStackTrace();
		} finally {
			this.destroy();
		}

	}

	public final void stop() {
		this.running = false;
	}

	public final void grabMouse() {
		if(!this.mouseGrabbed) {
			this.mouseGrabbed = true;
			if(this.appletMode) {
				try {
					Mouse.setNativeCursor(this.emptyCursor);
					Mouse.setCursorPosition(this.width / 2, this.height / 2);
				} catch (LWJGLException lWJGLException2) {
					lWJGLException2.printStackTrace();
				}
			} else {
				Mouse.setGrabbed(true);
			}

			this.setScreen((Screen)null);
		}
	}

	private void releaseMouse() {
		if(this.mouseGrabbed) {
			this.player.releaseAllKeys();
			this.mouseGrabbed = false;
			if(this.appletMode) {
				try {
					Mouse.setNativeCursor((Cursor)null);
				} catch (LWJGLException lWJGLException2) {
					lWJGLException2.printStackTrace();
				}
			} else {
				Mouse.setGrabbed(false);
			}

			this.setScreen(new PauseScreen());
		}
	}

	private void clickMouse() {
		if(this.hitResult != null) {
			Tile tile1 = Tile.tiles[this.level.getTile(this.hitResult.x, this.hitResult.y, this.hitResult.z)];
			if(this.editMode == 0) {
				boolean z7 = this.level.setTile(this.hitResult.x, this.hitResult.y, this.hitResult.z, 0);
				if(tile1 != null && z7) {
					tile1.destroy(this.level, this.hitResult.x, this.hitResult.y, this.hitResult.z, this.particleEngine);
				}

			} else {
				int i2 = this.hitResult.x;
				int i6 = this.hitResult.y;
				int i3 = this.hitResult.z;
				if(this.hitResult.f == 0) {
					--i6;
				}

				if(this.hitResult.f == 1) {
					++i6;
				}

				if(this.hitResult.f == 2) {
					--i3;
				}

				if(this.hitResult.f == 3) {
					++i3;
				}

				if(this.hitResult.f == 4) {
					--i2;
				}

				if(this.hitResult.f == 5) {
					++i2;
				}

				Tile tile4;
				AABB aABB8;
				if(((tile4 = Tile.tiles[this.level.getTile(i2, i6, i3)]) == null || tile4 == Tile.water || tile4 == Tile.calmWater || tile4 == Tile.lava || tile4 == Tile.calmLava) && ((aABB8 = Tile.tiles[this.paintTexture].getAABB(i2, i6, i3)) == null || (this.player.bb.intersects(aABB8) ? false : this.level.isFree(aABB8)))) {
					this.level.setTile(i2, i6, i3, this.paintTexture);
					Tile.tiles[this.paintTexture].onBlockAdded(this.level, i2, i6, i3);
				}

			}
		}
	}

	private void tick() {
		int i2;
		LevelRenderer levelRenderer6;
		if(this.screen != null) {
			this.prevFrameTime = this.ticksRan + 10000;
		} else {
			while(true) {
				int i1;
				while(Mouse.next()) {
					int i3;
					Minecraft minecraft5;
					if((i1 = Mouse.getEventDWheel()) != 0) {
						i2 = i1;
						minecraft5 = this;
						if(i1 > 0) {
							i2 = 1;
						}

						if(i2 < 0) {
							i2 = -1;
						}

						i3 = 0;

						for(int i4 = 0; i4 < creativeTiles.length; ++i4) {
							if(creativeTiles[i4] == minecraft5.paintTexture) {
								i3 = i4;
							}
						}

						for(i3 += i2; i3 < 0; i3 += creativeTiles.length) {
						}

						while(i3 >= creativeTiles.length) {
							i3 -= creativeTiles.length;
						}

						minecraft5.paintTexture = creativeTiles[i3];
					}

					if(!this.mouseGrabbed && Mouse.getEventButtonState()) {
						this.grabMouse();
					} else {
						if(Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
							this.clickMouse();
							this.prevFrameTime = this.ticksRan;
						}

						if(Mouse.getEventButton() == 1 && Mouse.getEventButtonState()) {
							this.editMode = (this.editMode + 1) % 2;
						}

						if(Mouse.getEventButton() == 2 && Mouse.getEventButtonState()) {
							minecraft5 = this;
							if(this.hitResult != null) {
								if((i2 = this.level.getTile(this.hitResult.x, this.hitResult.y, this.hitResult.z)) == Tile.grass.id) {
									i2 = Tile.dirt.id;
								}

								for(i3 = 0; i3 < creativeTiles.length; ++i3) {
									if(i2 == creativeTiles[i3]) {
										minecraft5.paintTexture = creativeTiles[i3];
									}
								}
							}
						}
					}
				}

				while(Keyboard.next()) {
					this.player.setKey(Keyboard.getEventKey(), Keyboard.getEventKeyState());
					if(Keyboard.getEventKeyState()) {
						if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
							this.releaseMouse();
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_R) {
							this.player.resetPos();
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
							this.level.setSpawnPos((int)this.player.x, (int)this.player.y, (int)this.player.z, this.player.yRot);
							this.player.resetPos();
						}

						for(i1 = 0; i1 < 9; ++i1) {
							if(Keyboard.getEventKey() == i1 + Keyboard.KEY_1) {
								this.paintTexture = creativeTiles[i1];
							}
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_Y) {
							this.yMouseAxis = -this.yMouseAxis;
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_G && this.level.entities.size() < 256) {
							this.level.entities.add(new Zombie(this.level, this.player.x, this.player.y, this.player.z));
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_F) {
							levelRenderer6 = this.levelRenderer;
							this.levelRenderer.drawDistance = (levelRenderer6.drawDistance + 1) % 4;
						}
					}
				}

				if(Mouse.isButtonDown(0) && (float)(this.ticksRan - this.prevFrameTime) >= this.timer.ticksPerSecond / 4.0F && this.mouseGrabbed) {
					this.clickMouse();
					this.prevFrameTime = this.ticksRan;
				}
				break;
			}
		}

		if(this.screen != null) {
			this.screen.updateEvents();
			if(this.screen != null) {
				this.screen.tick();
			}
		}

		levelRenderer6 = this.levelRenderer;
		++this.levelRenderer.cloudTickCounter;
		this.level.tick();
		ParticleEngine particleEngine7 = this.particleEngine;

		for(i2 = 0; i2 < particleEngine7.particles.size(); ++i2) {
			Particle particle8;
			(particle8 = (Particle)particleEngine7.particles.get(i2)).tick();
			if(particle8.removed) {
				particleEngine7.particles.remove(i2--);
			}
		}

		this.player.tick();
	}

	private void orientCamera(float f1) {
		GL11.glTranslatef(0.0F, 0.0F, -0.3F);
		GL11.glRotatef(this.player.xRot - this.player.xRotI * (1.0F - f1), 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(this.player.yRot - this.player.yRotI * (1.0F - f1), 0.0F, 1.0F, 0.0F);
		float f2 = this.player.xo + (this.player.x - this.player.xo) * f1;
		float f3 = this.player.yo + (this.player.y - this.player.yo) * f1;
		float f4 = this.player.zo + (this.player.z - this.player.zo) * f1;
		GL11.glTranslatef(-f2, -f3, -f4);
	}

	private void render(float f1) {
		if(!Display.isActive()) {
			this.releaseMouse();
		}

		int i2;
		int i3;
		int i4;
		int i5;
		if(this.mouseGrabbed) {
			i2 = 0;
			i3 = 0;
			if(this.appletMode) {
				if(this.parent != null) {
					Point point16;
					i4 = (point16 = this.parent.getLocationOnScreen()).x + this.width / 2;
					i5 = point16.y + this.height / 2;
					Point point6;
					i2 = (point6 = MouseInfo.getPointerInfo().getLocation()).x - i4;
					i3 = -(point6.y - i5);
					this.robot.mouseMove(i4, i5);
				} else {
					Mouse.setCursorPosition(this.width / 2, this.height / 2);
				}
			} else {
				i2 = Mouse.getDX();
				i3 = Mouse.getDY();
			}

			this.player.turn((float)i2, (float)(i3 * this.yMouseAxis));
		}

		GL11.glViewport(0, 0, this.width, this.height);
		checkGlError("Set viewport");
		Minecraft minecraft17 = this;
		this.selectBuffer.clear();
		GL11.glSelectBuffer(this.selectBuffer);
		GL11.glRenderMode(GL11.GL_SELECT);
		int i10002 = this.width / 2;
		int i10 = this.height / 2;
		int i9 = i10002;
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		this.viewportBuffer.clear();
		GL11.glGetInteger(GL11.GL_VIEWPORT, this.viewportBuffer);
		this.viewportBuffer.flip();
		this.viewportBuffer.limit(16);
		GLU.gluPickMatrix((float)i9, (float)i10, 5.0F, 5.0F, this.viewportBuffer);
		GLU.gluPerspective(70.0F, (float)this.width / (float)this.height, 0.05F, 1024.0F);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		this.orientCamera(f1);
		LevelRenderer levelRenderer10000 = this.levelRenderer;
		Player player10001 = this.player;
		Frustum frustum30 = Frustum.getFrustum();
		Player player19 = player10001;
		LevelRenderer levelRenderer20 = levelRenderer10000;
		Tesselator tesselator32 = Tesselator.instance;
		float f11 = 2.5F;
		AABB aABB12;
		i5 = (int)(aABB12 = player19.bb.grow(f11, f11, f11)).x0;
		int i24 = (int)(aABB12.x1 + 1.0F);
		int i7 = (int)aABB12.y0;
		int i8 = (int)(aABB12.y1 + 1.0F);
		int i33 = (int)aABB12.z0;
		int i34 = (int)(aABB12.z1 + 1.0F);
		GL11.glInitNames();
		GL11.glPushName(0);
		GL11.glPushName(0);

		for(i5 = i5; i5 < i24; ++i5) {
			GL11.glLoadName(i5);
			GL11.glPushName(0);

			for(int i13 = i7; i13 < i8; ++i13) {
				GL11.glLoadName(i13);
				GL11.glPushName(0);

				for(int i14 = i33; i14 < i34; ++i14) {
					Tile tile15;
					if((tile15 = Tile.tiles[levelRenderer20.level.getTile(i5, i13, i14)]) != null && tile15.mayPick() && frustum30.isVisible(Tile.getTileAABB(i5, i13, i14))) {
						GL11.glLoadName(i14);
						GL11.glPushName(0);

						for(int i36 = 0; i36 < 6; ++i36) {
							if(Tile.cullFace(levelRenderer20.level, i5, i13, i14, i36)) {
								GL11.glLoadName(i36);
								tesselator32.begin();
								Tile.renderFaceNoTexture(player19, tesselator32, i5, i13, i14, i36);
								tesselator32.end();
							}
						}

						GL11.glPopName();
					}
				}

				GL11.glPopName();
			}

			GL11.glPopName();
		}

		GL11.glPopName();
		GL11.glPopName();
		i5 = GL11.glRenderMode(GL11.GL_RENDER);
		this.selectBuffer.flip();
		this.selectBuffer.limit(this.selectBuffer.capacity());
		int[] i25 = new int[10];
		HitResult hitResult26 = null;

		for(i8 = 0; i8 < i5; ++i8) {
			i3 = minecraft17.selectBuffer.get();
			minecraft17.selectBuffer.get();
			minecraft17.selectBuffer.get();

			for(i4 = 0; i4 < i3; ++i4) {
				i25[i4] = minecraft17.selectBuffer.get();
			}

			minecraft17.hitResult = new HitResult(i25[0], i25[1], i25[2], i25[3], i25[4]);
			if(hitResult26 != null) {
				i10 = minecraft17.editMode;
				player19 = minecraft17.player;
				HitResult hitResult37 = minecraft17.hitResult;
				f11 = minecraft17.hitResult.distanceTo(player19, i10);
				float f35 = hitResult26.distanceTo(player19, i10);
				if(f11 >= f35) {
					continue;
				}
			}

			hitResult26 = minecraft17.hitResult;
		}

		minecraft17.hitResult = hitResult26;
		checkGlError("Picked");
		this.fogColorRed = 0.92F;
		this.fogColorGreen = 0.98F;
		this.fogColorBlue = 1.0F;
		GL11.glClearColor(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 0.0F);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		this.renderDistance = (float)(1024 >> (this.levelRenderer.drawDistance << 1));
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(70.0F, (float)this.width / (float)this.height, 0.05F, this.renderDistance);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		this.orientCamera(f1);
		checkGlError("Set up camera");
		GL11.glEnable(GL11.GL_CULL_FACE);
		Frustum frustum22 = Frustum.getFrustum();
		Frustum frustum23 = frustum22;
		LevelRenderer levelRenderer18 = this.levelRenderer;

		for(i5 = 0; i5 < levelRenderer18.sortedChunks.length; ++i5) {
			levelRenderer18.sortedChunks[i5].isInFrustum(frustum23);
		}

		player19 = this.player;
		levelRenderer18 = this.levelRenderer;
		TreeSet treeSet27;
		(treeSet27 = new TreeSet(new DirtyChunkSorter(player19))).addAll(levelRenderer18.dirtyChunks);
		i24 = 4;
		Iterator iterator28 = treeSet27.iterator();

		while(iterator28.hasNext()) {
			Chunk chunk29;
			(chunk29 = (Chunk)iterator28.next()).rebuild();
			levelRenderer18.dirtyChunks.remove(chunk29);
			--i24;
			if(i24 == 0) {
				break;
			}
		}

		checkGlError("Update chunks");
		boolean z21 = this.level.isSolid(this.player.x, this.player.y, this.player.z, 0.1F);
		this.setupFog(0);
		GL11.glEnable(GL11.GL_FOG);
		this.levelRenderer.render(this.player, 0);
		if(z21) {
			i4 = (int)this.player.x;
			i5 = (int)this.player.y;
			i24 = (int)this.player.z;

			for(i2 = i4 - 1; i2 <= i4 + 1; ++i2) {
				for(i7 = i5 - 1; i7 <= i5 + 1; ++i7) {
					for(i8 = i24 - 1; i8 <= i24 + 1; ++i8) {
						this.levelRenderer.render(i2, i7, i8);
					}
				}
			}
		}

		checkGlError("Rendered level");
		this.levelRenderer.renderEntities(frustum22, f1);
		checkGlError("Rendered entities");
		this.particleEngine.render(this.player, f1);
		checkGlError("Rendered particles");
		levelRenderer18 = this.levelRenderer;
		GL11.glCallList(this.levelRenderer.surroundLists);
		GL11.glDisable(GL11.GL_LIGHTING);
		this.setupFog(-1);
		this.levelRenderer.renderClouds(f1);
		this.setupFog(1);
		GL11.glEnable(GL11.GL_LIGHTING);
		if(this.hitResult != null) {
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			this.levelRenderer.renderHit(this.player, this.hitResult, this.editMode, this.paintTexture);
			LevelRenderer.renderHitOutline(this.hitResult, this.editMode);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_LIGHTING);
		}

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		this.setupFog(0);
		levelRenderer18 = this.levelRenderer;
		GL11.glCallList(this.levelRenderer.surroundLists + 1);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glColorMask(false, false, false, false);
		this.levelRenderer.render(this.player, 1);
		GL11.glColorMask(true, true, true, true);
		this.levelRenderer.render(this.player, 1);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		if(this.hitResult != null) {
			GL11.glDepthFunc(GL11.GL_LESS);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			this.levelRenderer.renderHit(this.player, this.hitResult, this.editMode, this.paintTexture);
			LevelRenderer.renderHitOutline(this.hitResult, this.editMode);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glDepthFunc(GL11.GL_LEQUAL);
		}

		i4 = this.width * 240 / this.height;
		i5 = this.height * 240 / this.height;
		i24 = Mouse.getX() * i4 / this.width;
		i7 = i5 - Mouse.getY() * i5 / this.height - 1;
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, (double)i4, (double)i5, 0.0D, 100.0D, 300.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0F, 0.0F, -200.0F);
		checkGlError("GUI: Init");
		GL11.glPushMatrix();
		GL11.glTranslatef((float)(i4 - 16), 16.0F, -50.0F);
		Tesselator tesselator31 = Tesselator.instance;
		GL11.glScalef(16.0F, 16.0F, 16.0F);
		GL11.glRotatef(-30.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
		GL11.glTranslatef(-1.5F, 0.5F, 0.5F);
		GL11.glScalef(-1.0F, -1.0F, -1.0F);
		i3 = this.textures.loadTexture("/terrain.png", 9728);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, i3);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		tesselator31.begin();
		Tile.tiles[this.paintTexture].render(tesselator31, this.level, 0, -2, 0, 0);
		tesselator31.end();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glPopMatrix();
		checkGlError("GUI: Draw selected");
		this.font.drawShadow("0.0.14a_08", 2, 2, 0xFFFFFF);
		this.font.drawShadow(this.fpsString, 2, 12, 0xFFFFFF);
		checkGlError("GUI: Draw text");
		i4 /= 2;
		i3 = i5 / 2;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		tesselator31.begin();
		tesselator31.vertex((float)(i4 + 1), (float)(i3 - 4), 0.0F);
		tesselator31.vertex((float)i4, (float)(i3 - 4), 0.0F);
		tesselator31.vertex((float)i4, (float)(i3 + 5), 0.0F);
		tesselator31.vertex((float)(i4 + 1), (float)(i3 + 5), 0.0F);
		tesselator31.vertex((float)(i4 + 5), (float)i3, 0.0F);
		tesselator31.vertex((float)(i4 - 4), (float)i3, 0.0F);
		tesselator31.vertex((float)(i4 - 4), (float)(i3 + 1), 0.0F);
		tesselator31.vertex((float)(i4 + 5), (float)(i3 + 1), 0.0F);
		tesselator31.end();
		checkGlError("GUI: Draw crosshair");
		if(this.screen != null) {
			this.screen.render(i24, i7);
		}

		checkGlError("Rendered gui");
		Display.update();
	}

	private void setupFog(int i1) {
		if(i1 == -1) {
			GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
			GL11.glFogf(GL11.GL_FOG_START, 0.0F);
			GL11.glFogf(GL11.GL_FOG_END, this.renderDistance);
			GL11.glFog(GL11.GL_FOG_COLOR, this.getBuffer(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F));
			GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.getBuffer(1.0F, 1.0F, 1.0F, 1.0F));
		} else {
			Tile tile2;
			if((tile2 = Tile.tiles[this.level.getTile((int)this.player.x, (int)(this.player.y + 0.12F), (int)this.player.z)]) != null && tile2.getLiquidType() == 1) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
				GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F);
				GL11.glFog(GL11.GL_FOG_COLOR, this.getBuffer(0.02F, 0.02F, 0.2F, 1.0F));
				GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.getBuffer(0.3F, 0.3F, 0.7F, 1.0F));
			} else if(tile2 != null && tile2.getLiquidType() == 2) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
				GL11.glFogf(GL11.GL_FOG_DENSITY, 2.0F);
				GL11.glFog(GL11.GL_FOG_COLOR, this.getBuffer(0.6F, 0.1F, 0.0F, 1.0F));
				GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.getBuffer(0.4F, 0.3F, 0.3F, 1.0F));
			} else if(i1 == 0) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
				GL11.glFogf(GL11.GL_FOG_START, 0.0F);
				GL11.glFogf(GL11.GL_FOG_END, this.renderDistance);
				GL11.glFog(GL11.GL_FOG_COLOR, this.getBuffer(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F));
				GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.getBuffer(1.0F, 1.0F, 1.0F, 1.0F));
			} else if(i1 == 1) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
				GL11.glFogf(GL11.GL_FOG_DENSITY, 0.01F);
				GL11.glFog(GL11.GL_FOG_COLOR, this.fogColor1);
				float f3 = 0.6F;
				GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.getBuffer(f3, f3, f3, 1.0F));
			}

			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT);
			GL11.glEnable(GL11.GL_LIGHTING);
		}
	}

	private FloatBuffer getBuffer(float f1, float f2, float f3, float f4) {
		this.lb.clear();
		this.lb.put(f1).put(f2).put(f3).put(1.0F);
		this.lb.flip();
		return this.lb;
	}

	public final void beginLevelLoading(String string1) {
		this.title = string1;
		int i3 = this.width * 240 / this.height;
		int i2 = this.height * 240 / this.height;
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, (double)i3, (double)i2, 0.0D, 100.0D, 300.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0F, 0.0F, -200.0F);
	}

	public final void levelLoadUpdate(String string1) {
		this.text = string1;
		this.setLoadingProgress(-1);
	}

	public final void setLoadingProgress(int i1) {
		int i2 = this.width * 240 / this.height;
		int i3 = this.height * 240 / this.height;
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		Tesselator tesselator4 = Tesselator.instance;
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		int i5 = this.textures.loadTexture("/dirt.png", 9728);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, i5);
		float f8 = 32.0F;
		tesselator4.begin();
		tesselator4.color(4210752);
		tesselator4.vertexUV(0.0F, (float)i3, 0.0F, 0.0F, (float)i3 / f8);
		tesselator4.vertexUV((float)i2, (float)i3, 0.0F, (float)i2 / f8, (float)i3 / f8);
		tesselator4.vertexUV((float)i2, 0.0F, 0.0F, (float)i2 / f8, 0.0F);
		tesselator4.vertexUV(0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
		tesselator4.end();
		if(i1 >= 0) {
			i5 = i2 / 2 - 50;
			int i6 = i3 / 2 + 16;
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			tesselator4.begin();
			tesselator4.color(8421504);
			tesselator4.vertex((float)i5, (float)i6, 0.0F);
			tesselator4.vertex((float)i5, (float)(i6 + 2), 0.0F);
			tesselator4.vertex((float)(i5 + 100), (float)(i6 + 2), 0.0F);
			tesselator4.vertex((float)(i5 + 100), (float)i6, 0.0F);
			tesselator4.color(8454016);
			tesselator4.vertex((float)i5, (float)i6, 0.0F);
			tesselator4.vertex((float)i5, (float)(i6 + 2), 0.0F);
			tesselator4.vertex((float)(i5 + i1), (float)(i6 + 2), 0.0F);
			tesselator4.vertex((float)(i5 + i1), (float)i6, 0.0F);
			tesselator4.end();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		this.font.drawShadow(this.title, (i2 - this.font.width(this.title)) / 2, i3 / 2 - 4 - 16, 0xFFFFFF);
		this.font.drawShadow(this.text, (i2 - this.font.width(this.text)) / 2, i3 / 2 - 4 + 8, 0xFFFFFF);
		Display.update();

		try {
			Thread.yield();
		} catch (Exception exception7) {
		}
	}

	public final void generateLevel(int i1) {
		String string2 = this.user != null ? this.user.name : "anonymous";
		this.setLevel(this.levelGen.generateLevel(string2, 128 << i1, 128 << i1, 64));
	}

	public final boolean saveLevel(int i1, String string2) {
		return this.levelIo.save(this.level, this.minecraftUri, this.user.name, this.user.sessionId, string2, i1);
	}

	public final boolean loadLevel(String string1, int i2) {
		Level level3;
		if((level3 = this.levelIo.load(this.minecraftUri, string1, i2)) == null) {
			return false;
		} else {
			this.setLevel(level3);
			return true;
		}
	}

	private void setLevel(Level level1) {
		this.level = level1;
		if(this.levelRenderer != null) {
			this.levelRenderer.setLevel(level1);
		}

		if(this.particleEngine != null) {
			ParticleEngine particleEngine10001 = this.particleEngine;
			this.particleEngine.particles.clear();
		}

		if(this.player != null) {
			this.player.setLevel(level1);
			this.player.resetPos();
		}

		System.gc();
	}
}
