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
package common;

import java.io.File;

public class Settings {
	protected String classPath; // paths are declared in constructor below;
	protected String clientDataPath; // NOTE: All paths must end in "/"
	protected String graphicsPath;
	protected String playerGraphicsPath;
	protected String imageFileEnding = ".png";	// must begin with a fullstop!
	
	public Settings() {
		try {
			classPath = getClass().getProtectionDomain().
					getCodeSource().getLocation().toURI().getPath();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		File classPathFile = new File(classPath);
		String parentPath = classPathFile.getParent()+"/"; // assuming that the binaries are in a "bin" dir.
		clientDataPath = parentPath+"clientData/";
		graphicsPath = clientDataPath+"graphics/";
		playerGraphicsPath = graphicsPath+"playerGraphics/";
	}
	
	protected boolean isPathAccessible(String path) {
		File file = new File(path);
		return (file.exists() && file.canRead() && file.isDirectory());
	}
	public void checkPaths() {
		String[] paths = new String[] {
			classPath, clientDataPath, graphicsPath, playerGraphicsPath
		};
		for (String path : paths) {
			if (!isPathAccessible(path)) {
				Logger.log("WARNING: directory "+path+" is not accessible!");
			}
		}
	}
	
	
	public String clientDataPath() {
		return clientDataPath;
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
}
