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
package serverSide.mapmanToButlerOrders;

import common.graphics.GraphicalPlayerStatus;
import serverSide.mapManager.MapManager;

/** NOTE: The butler has to check if the source matches his registered
 * active map manager, or else it may be that a former MapManager still sends
 * events which don't belong to the new map anymore! */
public class MBAddCharOrder extends MBOrder {
	private final String playerName;
	private final GraphicalPlayerStatus graphStatus;
	
	public MBAddCharOrder(MapManager source, String playerName, GraphicalPlayerStatus graphStatus) {
		super(source);
		this.playerName = playerName;
		this.graphStatus = graphStatus;
	}
	
	
	public String getPlayerName() {
		return playerName;
	}
	
	public GraphicalPlayerStatus getGraphStatus() {
		return graphStatus;
	}
}
