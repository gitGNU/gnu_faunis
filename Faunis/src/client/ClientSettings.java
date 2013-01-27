/* Copyright 2012, 2013 Simon Ley alias "skarute"
 * 
 * This file is part of Faunis.
 * 
 * Faunis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Faunis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General
 * Public License along with Faunis. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package client;

import java.awt.Point;
import java.io.File;

public class ClientSettings {
	private String classPath; // paths are declared in constructor below;
	private String clientDataPath; // NOTE: All paths must end in "/"
	private String gameGraphicsPath;
	private String playerGraphicsPath;
	private String decoGraphicsPath;
	private String imageFileEnding = ".png";	// must begin with a fullstop!
	private int fieldWidth = 20;
	private int fieldHeight = 14;
	private int deltaLevelAmplitude = 2;
	private String host = null; // the hostname, or null for loopback
	private int port = 1024;	// the port through which to connect to the server
	private int frameRate = 20;	// frames per second
	
	public ClientSettings() {
		try {
			classPath = getClass().getProtectionDomain().
					getCodeSource().getLocation().toURI().getPath();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		File classPathFile = new File(classPath);
		String parentPath = classPathFile.getParent()+"/";
		clientDataPath = parentPath+"clientData/";
		gameGraphicsPath = clientDataPath+"graphics/";
		playerGraphicsPath = gameGraphicsPath+"playerGraphics/";
		decoGraphicsPath = gameGraphicsPath+"decoGraphics/";
	}
	
	public String decoGraphicsPath() {
		return decoGraphicsPath;
	}
	
	public String playerGraphicsPath() {
		return playerGraphicsPath;
	}
	
	public String imageFileEnding() {
		return imageFileEnding;
	}
	
	public int fieldWidth() {
		return fieldWidth;
	}
	
	public int fieldHeight() {
		return fieldHeight;
	}
	
	public int deltaLevelAmplitude() {
		return deltaLevelAmplitude;
	}
	
	public int numberOfDeltaLevelStates() {
		return 2*deltaLevelAmplitude+1;
	}
	
	public String host() {
		return host;
	}
	
	public int port() {
		return port;
	}
	
	public int frameRate() {
		return frameRate;
	}
	
	public int delayBetweenFrames() {
		return 1000 / frameRate;
	}
	
	public String checkPaths() {
		File playerGraphicsDir = new File(playerGraphicsPath);
		if (! (playerGraphicsDir.exists() && playerGraphicsDir.isDirectory())) {
			String error = "Player graphics directory "+playerGraphicsPath+" doesn't exist!";
			System.out.println("WARNING: "+error);
			return error;
		}
		return null;
	}
	
	public Point pixelToMapField(Point pixel) {
		return new Point(pixel.x / fieldWidth, pixel.y / fieldHeight);
	}
	
	public Point mapFieldToLeftUpperPixel(Point field) {
		return new Point(field.x * fieldWidth, field.y * fieldHeight);
	}
	
	public Point mapFieldToCenterPixel(Point field) {
		return new Point(fieldWidth/2 + field.x*fieldWidth,
							fieldHeight/2 + field.y*fieldHeight);
	}
}
