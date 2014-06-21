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
package serverSide;

import java.io.File;

import common.Logger;
import common.Settings;

public class ServerSettings extends Settings {
	private String serverDataPath; // NOTE: All paths must end in "/"
	private String accountPath;
	private String mapPath;
	private String starterRegion = "sw-green";
	private int maxPlayersPerAccount = 3;
	private int receptionPort = 1024;
	private String serverSourceAt = "http://savannah.nongnu.org";
	
	public ServerSettings() {
		super();
		File classPathFile = new File(classPath);
		String parentPath = classPathFile.getParent()+"/";
		serverDataPath = parentPath+"serverData/";
		accountPath = serverDataPath+"accounts/";
		mapPath = serverDataPath+"maps/";
	}
	
	public String accountPath() {
		return accountPath;
	}
	
	public String starterRegion() {
		return starterRegion;
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
	
	@Override
	public void checkPaths() {
		super.checkPaths();
		String[] paths = new String[] {accountPath, mapPath};
		for (String path : paths) {
			if (!isPathAccessible(path)) {
				Logger.log("WARNING: directory "+path+" is not accessible!");
			}
		}
	}
}
