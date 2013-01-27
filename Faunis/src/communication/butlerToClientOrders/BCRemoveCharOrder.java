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
package communication.butlerToClientOrders;

import server.mapmanToButlerOrders.MBRemoveCharOrder;

/** The butler tells the client that the character defined by this order
 * has left the map and should no longer be displayed. */
public class BCRemoveCharOrder extends BCOrder {
	private static final long serialVersionUID = 1L;
	private String playerName;

	public BCRemoveCharOrder(MBRemoveCharOrder order) {
		this.playerName = order.getPlayerName();
	}
	
	public String getPlayerName() {
		return playerName;
	}
}
