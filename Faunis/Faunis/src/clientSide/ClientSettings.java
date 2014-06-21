/* Copyright 2012 - 2014 Simon Ley alias "skarute"
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
import java.util.Properties;

import common.Logger;
import common.Settings;

public class ClientSettings extends Settings {
	private String decoGraphicsPath;
	private String floorGraphicsPath;
	private String guiGraphicsPath;
	
	private int fieldWidth = 40;
	private int fieldHeight = 28;
	private Color gridColor = new Color(200, 200, 200, 90);
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
		super();
		decoGraphicsPath = graphicsPath+"decoGraphics/";
		floorGraphicsPath = graphicsPath+"floorGraphics/";
		guiGraphicsPath = graphicsPath+"guiGraphics/";
	}
	
	public String decoGraphicsPath() {
		return decoGraphicsPath;
	}
	
	public String floorGraphicsPath() {
		return floorGraphicsPath;
	}
	
	public String guiGraphicsPath() {
		return guiGraphicsPath;
	}
	
	public int fieldWidth() {
		return fieldWidth;
	}
	
	public int fieldHeight() {
		return fieldHeight;
	}
	
	public Color gridColor() {
		return gridColor;
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
	
	@Override
	public void checkPaths() {
		super.checkPaths();
		String[] paths = new String[] {
			decoGraphicsPath, floorGraphicsPath, guiGraphicsPath
		};
		for (String path : paths) {
			if (!isPathAccessible(path)) {
				Logger.log("WARNING: directory "+path+" is not accessible!");
			}
		}
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
