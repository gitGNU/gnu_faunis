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
package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import communication.GraphicalDecoStatus;
import communication.Link;
import communication.Map;
import communication.enums.CoordinateType;


public class MapContentManager {
	private HashMap<String, Map> maps;
	
	/** Please call loadAllMaps() afterwards. */
	public MapContentManager() {
		maps = new HashMap<String, Map>();
	}
	
	public HashMap<String, Map> getMaps() {
		return maps;
	}
	
	public Map getMap(String mapName) {
		return maps.get(mapName);
	}
	
	public Set<String> getMapNames() {
		return maps.keySet();
	}
	
	/** Must be called after the MapContentManager is constructed. */
	public void loadAllMaps(String mapPath) {
		File mapDirectory = new File(mapPath);
		File[] mapSubdirs = mapDirectory.listFiles(MainServer.directoryFilter);
		for (File mapSubdir : mapSubdirs) {
			String mapName = mapSubdir.getName();
			// read map info
			File settingsFile = new File(mapPath+mapName+"/settings.txt");
			String line;
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(
												new FileInputStream(settingsFile)));
				line = reader.readLine();
			} catch (Exception e) {
				System.out.println("Could read settings file for map: "+mapName
						+", reason: "+e.getMessage());
				continue;
			}
			String[] arguments = line.split(",");
			if (arguments.length != 2) {
				System.out.println("Error reading map: "+mapName
						+", wrong number of arguments!");
				continue;
			}
			for (int i = 0; i < arguments.length; i++)
				arguments[i] = arguments[i].trim();
			int mapWidth, mapHeight;
			try {
				mapWidth = Integer.parseInt(arguments[0]);
				mapHeight = Integer.parseInt(arguments[1]);
			} catch (NumberFormatException e) {
				System.out.println("Error reading map: "+mapName
						+", could not parse arguments!");
				continue;
			}
			ArrayList<GraphicalDecoStatus> decoInfos = null;
			File decoFile = new File(mapPath+mapName+"/deco.txt");
			if (decoFile.exists()) {
				decoInfos = loadDecoFile(mapWidth, mapHeight, decoFile);
			}
			ArrayList<Link> links = null;
			File linkFile = new File(mapPath+mapName+"/links.txt");
			if (linkFile.exists()) {
				links = loadLinkFile(mapName, mapWidth, mapHeight, linkFile);
			}
			Map map = new Map(mapName, mapWidth, mapHeight, decoInfos, links);
			assert(!maps.containsKey(mapName));
			maps.put(mapName, map);
		}
	}
	
	
	
	private void loadMapCharsAndTranslationMap(int mapWidth, int mapHeight,
												File file, char[][] mapChars,
										HashMap<Character, String> translationMap) {
		assert(mapChars.length == mapHeight && mapChars[0].length == mapWidth);
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
										new FileInputStream(file)));
			int y = 0;
			while(true) {
				char[] row = reader.readLine().toCharArray();
				if (row.length == 1 && row[0] == '*') {
					break;
				} else if (row.length == 0) {
					continue;
				} else {
					mapChars[y] = row;
				}
				y++;
			}
			// now to the translation phase:
			String translation = reader.readLine(); 
			while (translation != null) {
				String[] splitted = translation.split("=");
				for (int i = 0; i < splitted.length; i++)
					splitted[i] = splitted[i].trim();
				if (splitted.length != 2) {
					System.out.println("loadMapCharsAndTranslationMap():"
							+" Warning, translation line has unexpected number"
							+" of split parts");
					continue;
				} else if (splitted[0].length() != 1) {
					System.out.println("loadMapCharsAndTranslationMap():"
							+" Warning, "+splitted[0]+" is not a char");
					continue;
				} else {
					translationMap.put(splitted[0].charAt(0), splitted[1]);
				}
				translation = reader.readLine();
			}
		} catch (Exception e) {
			System.out.println("Couldn't read map file! Reason: "+e.getMessage());
			return;
		}
	}
	
	
	
	private ArrayList<GraphicalDecoStatus> loadDecoFile(int mapWidth, int mapHeight,
															File decoFile) {
		char[][] mapChars = new char[mapHeight][mapWidth];
		HashMap<Character, String> translationMap = new HashMap<Character, String>();
		loadMapCharsAndTranslationMap(mapWidth, mapHeight, decoFile,
				mapChars, translationMap);
		ArrayList<GraphicalDecoStatus> decoInfos = new ArrayList<GraphicalDecoStatus>();
		// Now translate and fill the result:
		for (int y = 0; y < mapHeight; y++) {
			for (int x = 0; x < mapWidth; x++) {
				String decoName = translationMap.get(mapChars[y][x]);
				if (decoName != null) {
					GraphicalDecoStatus decoStatus = new GraphicalDecoStatus();
					decoStatus.x = x;
					decoStatus.y = y;
					decoStatus.name = decoName;
					decoInfos.add(decoStatus);
				}
			}
		}
		return decoInfos;
	}
	
	
	
	private ArrayList<Link> loadLinkFile(String sourceMap, 
			int mapWidth, int mapHeight, File linkFile) {
		char[][] mapChars = new char[mapHeight][mapWidth];
		HashMap<Character, String> translationMap = new HashMap<Character, String>();
		loadMapCharsAndTranslationMap(mapWidth, mapHeight, linkFile,
				mapChars, translationMap);
		ArrayList<Link> linkList = new ArrayList<Link>();
		// Now translate and fill the result:
		for (int y = 0; y < mapHeight; y++) {
			for (int x = 0; x < mapWidth; x++) {
				String linkData = translationMap.get(mapChars[y][x]);
				if (linkData != null) {
					String[] splitted = linkData.split(",");
					if (splitted.length != 3) {
						System.out.println("loadLinkFile(): Warning, unexpected number"
								+ " of split parts");
						continue;
					}
					for (int i = 0; i < splitted.length; i++)
						splitted[i] = splitted[i].trim();
					int sourceX = x;
					int sourceY = y;
					String targetMap = splitted[0];
					int[] targetCoordinate = new int[]{0, 0};
					CoordinateType[] targetType = new CoordinateType[2];
					boolean parseErrorOccurred = false;
					for (int i = 0; i < 2; i++) {
						if (splitted[i+1].equals("*")) {
							targetType[i] = CoordinateType.UNCHANGED;
						} else if (splitted[i].equals("+") || splitted[i].equals("-")) {
							targetType[i] = CoordinateType.RELATIVE;
							if (splitted[i].equals("+"))
								splitted[i] = splitted[i].substring(1);
							try {
								targetCoordinate[i] = Integer.parseInt(splitted[i+1]);
							} catch (NumberFormatException e) {
								System.out.println("loadLinkFile(): Warning, could not parse"
										+" translation line arguments");
								parseErrorOccurred = true;
								break;
							}
						} else {
							targetType[i] = CoordinateType.ABSOLUTE;
							try {
								targetCoordinate[i] = Integer.parseInt(splitted[i+1]);
							} catch (NumberFormatException e) {
								System.out.println("loadLinkFile(): Warning, could not parse"
										+" translation line arguments");
								parseErrorOccurred = true;
								break;
							}
						}
					}
					if (parseErrorOccurred)
						continue;
					Link link = new Link(sourceMap, sourceX, sourceY, targetMap,
							targetCoordinate[0], targetCoordinate[1],
							targetType[0], targetType[1]);
					linkList.add(link);
				}
			}
		}
		return linkList;
	}
}
