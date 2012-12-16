/* Copyright 2012 Simon Ley alias "skarute"
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
package communication;

import java.io.Serializable;
import java.util.HashMap;

public class MapInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	public String mapName;
	public HashMap<String, GraphicalPlayerStatus> players;
	
	public MapInfo(String mapName, HashMap<String, GraphicalPlayerStatus> players) {
		this.mapName = mapName;
		this.players = players;
	}
}
