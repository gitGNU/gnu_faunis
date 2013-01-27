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
package server.butlerToMapmanOrders;

import communication.clientToButlerOrders.CBMoveOrder;

import server.Butler;
import server.Player;

public class BMMoveOrder extends BMOrder {
	private Player player;
	private int xTarget;
	private int yTarget;
	
	public BMMoveOrder(Player player, Butler source, CBMoveOrder order) {
		super(source);
		this.player = player;
		this.xTarget = order.getXTarget();
		this.yTarget = order.getYTarget();
	}
	
	public BMMoveOrder(Player player, Butler source, int xTarget, int yTarget) {
		super(source);
		this.player = player;
		this.xTarget = xTarget;
		this.yTarget = yTarget;
	}
	
	public int getXTarget() {
		return xTarget;
	}
	
	public int getYTarget() {
		return yTarget;
	}
	
	public Player getPlayer() {
		return player;
	}
}
