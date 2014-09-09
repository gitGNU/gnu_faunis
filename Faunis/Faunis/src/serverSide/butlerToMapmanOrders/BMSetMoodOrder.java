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

import common.enums.Mood;

import serverSide.butler.Butler;
import serverSide.player.ServerPlayer;


public class BMSetMoodOrder extends BMOrder {
	private ServerPlayer player;
	private Mood mood;
	public BMSetMoodOrder(Butler source, ServerPlayer player, Mood mood) {
		super(source);
		this.player = player;
		this.mood = mood;
	}

	public ServerPlayer getPlayer() {
		return player;
	}

	public Mood getMood() {
		return mood;
	}
}
