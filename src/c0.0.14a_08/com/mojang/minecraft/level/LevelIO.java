package com.mojang.minecraft.level;

import com.mojang.minecraft.Minecraft;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class LevelIO {
	private Minecraft minecraft;

	public LevelIO(Minecraft minecraft1) {
		this.minecraft = minecraft1;
	}

	public final boolean save(Level level1, String string2, String string3, String string4, String string5, int i6) {
		this.minecraft.beginLevelLoading("Saving level");

		try {
			this.minecraft.levelLoadUpdate("Compressing..");
			ByteArrayOutputStream byteArrayOutputStream7 = new ByteArrayOutputStream();
			save(level1, byteArrayOutputStream7);
			byteArrayOutputStream7.close();
			byte[] b11 = byteArrayOutputStream7.toByteArray();
			
			this.minecraft.levelLoadUpdate("Saving..");
			File savesDir = new File("saves");
			if (!savesDir.exists()) {
				savesDir.mkdir();
			}
			
			File levelFile = new File(savesDir, string5 + ".dat");
			FileOutputStream fos = new FileOutputStream(levelFile);
			fos.write(b11);
			fos.close();
			
			return true;
		} catch (Exception exception10) {
			exception10.printStackTrace();
			this.minecraft.levelLoadUpdate("Failed!");

			try {
				Thread.sleep(1000L);
			} catch (InterruptedException interruptedException8) {
				interruptedException8.printStackTrace();
			}

			return false;
		}
	}

	public final Level load(String string1, String string2, int i3) {
		this.minecraft.beginLevelLoading("Loading level");

		try {
			this.minecraft.levelLoadUpdate("Loading..");
			File savesDir = new File("saves");
			if (!savesDir.exists()) {
				savesDir.mkdir();
				return null;
			}
			
			File levelFile = new File(savesDir, string2 + ".dat");
			if (!levelFile.exists()) {
				return null;
			}
			
			FileInputStream fis = new FileInputStream(levelFile);
			Level level = this.load(fis);
			fis.close();
			
			return level;
		} catch (Exception exception6) {
			exception6.printStackTrace();
			this.minecraft.levelLoadUpdate("Failed!");

			try {
				Thread.sleep(3000L);
			} catch (InterruptedException interruptedException4) {
				interruptedException4.printStackTrace();
			}

			return null;
		}
	}

	public final Level load(InputStream inputStream1) {
		this.minecraft.beginLevelLoading("Loading level");
		this.minecraft.levelLoadUpdate("Reading..");

		try {
			DataInputStream dataInputStream10;
			if((dataInputStream10 = new DataInputStream(new GZIPInputStream(inputStream1))).readInt() != 656127880) {
				return null;
			} else {
				byte b12;
				if((b12 = dataInputStream10.readByte()) > 2) {
					return null;
				} else if(b12 <= 1) {
					System.out.println("Version is 1!");
					String string14 = dataInputStream10.readUTF();
					String string15 = dataInputStream10.readUTF();
					long j7 = dataInputStream10.readLong();
					short s3 = dataInputStream10.readShort();
					short s4 = dataInputStream10.readShort();
					short s5 = dataInputStream10.readShort();
					byte[] b6 = new byte[s3 * s4 * s5];
					dataInputStream10.readFully(b6);
					dataInputStream10.close();
					Level level11;
					(level11 = new Level()).setData(s3, s5, s4, b6);
					level11.name = string14;
					level11.creator = string15;
					level11.createTime = j7;
					return level11;
				} else {
					Level level2;
					ObjectInputStream objectInputStream13;
					(level2 = (Level)(objectInputStream13 = new ObjectInputStream(dataInputStream10)).readObject()).initTransient();
					objectInputStream13.close();
					return level2;
				}
			}
		} catch (Exception exception9) {
			exception9.printStackTrace();
			(new StringBuilder()).append("Failed to load level: ").append(exception9.toString()).toString();
			return null;
		}
	}

	public final Level loadLegacy(InputStream inputStream1) {
		this.minecraft.beginLevelLoading("Loading level");
		this.minecraft.levelLoadUpdate("Reading..");

		try {
			DataInputStream dataInputStream5 = new DataInputStream(new GZIPInputStream(inputStream1));
			String string7 = "--";
			String string2 = "unknown";
			byte[] b3 = new byte[256 << 8 << 6];
			dataInputStream5.readFully(b3);
			dataInputStream5.close();
			Level level6;
			(level6 = new Level()).setData(256, 64, 256, b3);
			level6.name = string7;
			level6.creator = string2;
			level6.createTime = 0L;
			return level6;
		} catch (Exception exception4) {
			exception4.printStackTrace();
			(new StringBuilder()).append("Failed to load level: ").append(exception4.toString()).toString();
			return null;
		}
	}

	public static void save(Level level0, OutputStream outputStream1) {
		try {
			DataOutputStream dataOutputStream3;
			(dataOutputStream3 = new DataOutputStream(new GZIPOutputStream(outputStream1))).writeInt(656127880);
			dataOutputStream3.writeByte(2);
			ObjectOutputStream objectOutputStream4;
			(objectOutputStream4 = new ObjectOutputStream(dataOutputStream3)).writeObject(level0);
			objectOutputStream4.close();
		} catch (Exception exception2) {
			exception2.printStackTrace();
		}
	}
}