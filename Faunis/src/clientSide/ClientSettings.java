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
package clientSide;

import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.util.Properties;

import common.Logger;

public class ClientSettings {
	private String classPath; // paths are declared in constructor below;
	private String clientDataPath; // NOTE: All paths must end in "/"
	private String graphicsPath;
	private String playerGraphicsPath;
	private String decoGraphicsPath;
	private String imageFileEnding = ".png";	// must begin with a fullstop!
	private int fieldWidth = 40;
	private int fieldHeight = 28;
	private int deltaLevelAmplitude = 2;
	private String host = null; // the hostname, or null for loopback
	private int port = 1024;	// the port through which to connect to the server
	private int frameRate = 20;	// frames per second
	private Color mapBackgroundColor = new Color(0, 180, 0); // default map background color
	
	private String commandPrefix;
	private String connectCommand;
	private String disconnectCommand;
	private String loginCommand;
	private String logoutCommand;
	private String loadPlayerCommand;
	private String unloadPlayerCommand;
	private String serverSourceCommand;
	private String emoteCommand;
	private String moveCommand;
	private String whisperCommand;
	private String broadcastCommand;
	private String createPlayerCommand;
	private String queryOwnPlayersCommand;
	private String helpCommand;
	
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
		graphicsPath = clientDataPath+"graphics/";
		playerGraphicsPath = graphicsPath+"playerGraphics/";
		decoGraphicsPath = graphicsPath+"decoGraphics/";
	}
	
	public String clientDataPath() {
		return clientDataPath;
	}
	
	public String decoGraphicsPath() {
		return decoGraphicsPath;
	}
	
	public String playerGraphicsPath() {
		return playerGraphicsPath;
	}
	
	public String graphicsPath() {
		return graphicsPath;
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
	
	public Color mapBackgroundColor() {
		return mapBackgroundColor;
	}
	
	public String checkPaths() {
		File playerGraphicsDir = new File(playerGraphicsPath);
		if (! (playerGraphicsDir.exists() && playerGraphicsDir.isDirectory())) {
			String error = "Player graphics directory "+playerGraphicsPath+" doesn't exist!";
			Logger.log("WARNING: "+error);
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
	
	public void loadFromProperties(Properties properties) {
		host = properties.getProperty("host", null);
		if (host != null && host.equals(""))
			host = null;
		port = Integer.parseInt(properties.getProperty("port", "1024"));
		commandPrefix = properties.getProperty("commandPrefix");
		connectCommand = properties.getProperty("connect");
		disconnectCommand = properties.getProperty("disconnect");
		loginCommand = properties.getProperty("login");
		logoutCommand = properties.getProperty("logout");
		loadPlayerCommand = properties.getProperty("loadPlayer");
		unloadPlayerCommand = properties.getProperty("unloadPlayer");
		serverSourceCommand = properties.getProperty("serverSource");
		emoteCommand = properties.getProperty("emote");
		moveCommand = properties.getProperty("move");
		whisperCommand = properties.getProperty("whisper");
		broadcastCommand = properties.getProperty("broadcast");
		createPlayerCommand = properties.getProperty("createPlayer");
		queryOwnPlayersCommand = properties.getProperty("queryOwnPlayers");
		helpCommand = properties.getProperty("help");
		// TODO
	}

	public String commandPrefix() {
		return commandPrefix;
	}
	public String connectCommand() {
		return connectCommand;
	}
	public String disconnectCommand() {
		return disconnectCommand;
	}
	public String loginCommand() {
		return loginCommand;
	}
	public String logoutCommand() {
		return logoutCommand;
	}
	public String loadPlayerCommand() {
		return loadPlayerCommand;
	}
	public String unloadPlayerCommand() {
		return unloadPlayerCommand;
	}
	public String serverSourceCommand() {
		return serverSourceCommand;
	}
	public String emoteCommand() {
		return emoteCommand;
	}
	public String moveCommand() {
		return moveCommand;
	}
	public String whisperCommand() {
		return whisperCommand;
	}
	public String broadcastCommand() {
		return broadcastCommand;
	}
	public String createPlayerCommand() {
		return createPlayerCommand;
	}
	public String queryOwnPlayersCommand() {
		return queryOwnPlayersCommand;
	}
	public String helpCommand() {
		return helpCommand;
	}
}
