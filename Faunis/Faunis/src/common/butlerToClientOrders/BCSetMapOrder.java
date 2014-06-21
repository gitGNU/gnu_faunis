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
package common.butlerToClientOrders;

import serverSide.mapmanToButlerOrders.MBMapInfoOrder;

import common.MapInfo;

/** The butler tells the client that the map defined by this order
 * should be shown, including info about all characters on the new map. */
public class BCSetMapOrder extends BCOrder {
	private static final long serialVersionUID = 1L;
	private MapInfo mapInfo;
	private String activePlayerName;
	
	public BCSetMapOrder(MBMapInfoOrder order, String activePlayerName) {
		this.mapInfo = order.getMapInfo();
		this.activePlayerName = activePlayerName;
	}
	
	public MapInfo getMapInfo() {
		return mapInfo;
	}
	
	public String getActivePlayerName() {
		return activePlayerName;
	}
}
