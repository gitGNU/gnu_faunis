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
package serverSide;

import java.io.File;

import common.Logger;

public class ServerSettings {
	private String classPath; // paths are declared in constructor below;
	private String serverDataPath; // NOTE: All paths must end in "/"
	private String clientDataPath;
	private String accountPath;
	private String graphicsPath;
	private String decoGraphicsPath;
	private String playerGraphicsPath;
	private String mapPath;
	private String imageFileEnding = ".png";
	private String starterRegion = "greenFields";
	private int maxPlayersPerAccount = 3;
	private int receptionPort = 1024;
	private String serverSourceAt = "http://savannah.nongnu.org";
	
	public ServerSettings() {
		try {
			classPath = getClass().getProtectionDomain().
					getCodeSource().getLocation().toURI().getPath();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		File classPathFile = new File(classPath);
		String parentPath = classPathFile.getParent()+"/";
		serverDataPath = parentPath+"serverData/";
		clientDataPath = parentPath+"clientData/";
		accountPath = serverDataPath+"accounts/";
		graphicsPath = clientDataPath+"graphics/";
		playerGraphicsPath = graphicsPath+"playerGraphics/";
		decoGraphicsPath = graphicsPath+"decoGraphics/";
		mapPath = serverDataPath+"maps/";
	}
	
	public String accountPath() {
		return accountPath;
	}
	
	public String starterRegion() {
		return starterRegion;
	}
	
	public String imageFileEnding() {
		return imageFileEnding;
	}
	
	public String playerGraphicsPath() {
		return playerGraphicsPath;
	}
	
	public String decoGraphicsPath() {
		return decoGraphicsPath;
	}
	
	public String graphicsPath() {
		return graphicsPath;
	}
	
	public String mapPath() {
		return mapPath;
	}
	
	public int maxPlayersPerAccount() {
		return maxPlayersPerAccount;
	}
	
	public String serverSourceAt() {
		return serverSourceAt;
	}
	
	public int receptionPort() {
		return receptionPort;
	}
	
	public void checkPaths() {
		File accountDir = new File(accountPath);
		if (! (accountDir.exists() && accountDir.isDirectory()))
			Logger.log("WARNING: Account directory "
								+accountPath+"doesn't exist!");
		File playerGraphicsDir = new File(accountPath);
		if (! (playerGraphicsDir.exists() && playerGraphicsDir.isDirectory()))
			Logger.log("WARNING: Player graphics directory "
								+playerGraphicsPath+" doesn't exist!");
		File mapDir = new File(mapPath);
		if (! (mapDir.exists() && mapDir.isDirectory()))
			Logger.log("WARNING: Map directory "
								+mapPath+" doesn't exist!");
	}
}
