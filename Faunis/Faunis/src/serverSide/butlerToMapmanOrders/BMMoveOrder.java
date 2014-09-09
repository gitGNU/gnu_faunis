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
package serverSide.butlerToMapmanOrders;

import common.clientToButlerOrders.CBMoveOrder;

import serverSide.butler.Butler;
import serverSide.player.ServerPlayer;

public class BMMoveOrder extends BMOrder {
	private ServerPlayer player;
	private int xTarget;
	private int yTarget;

	public BMMoveOrder(ServerPlayer player, Butler source, CBMoveOrder order) {
		super(source);
		this.player = player;
		this.xTarget = order.getXTarget();
		this.yTarget = order.getYTarget();
	}

	public BMMoveOrder(ServerPlayer player, Butler source, int xTarget, int yTarget) {
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

	public ServerPlayer getPlayer() {
		return player;
	}
}
