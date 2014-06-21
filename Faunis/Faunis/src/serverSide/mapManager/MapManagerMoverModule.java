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
package serverSide.mapManager;

import java.util.Map;

import serverSide.mapmanToButlerOrders.MBChangeCharOrder;
import serverSide.mapmanToButlerOrders.MBCharAtOtherMapmanOrder;
import serverSide.player.Player;

import common.Link;
import common.graphics.GraphicalPlayerStatus;
import common.modules.timerModule.MoverModule;
import common.movement.Mover;
import common.movement.MovingTask;
import common.movement.RoughMovingTask;

public class MapManagerMoverModule extends MoverModule<Player, MapManager, Void> {
	
	public MapManagerMoverModule(Map<Player, Mover<Player, MapManager>> map) {
		super(map);
	}

	@Override
	public void unregistered(Player forPlayer) {
		// Check if the player has landed on a link to another map
		Link link = parent.getMap().getOutgoingLink(forPlayer.getX(), forPlayer.getY());
		if (link != null) {
			if (link.getTargetMap().equals(parent.getMapName())) {
				// just move the player to the targetField
				link.move(forPlayer);
			} else {
				// the player must be moved to another mapman:
				// -> inform the butler so that he can apply the change
				parent.registeredPlayers.get(forPlayer).put(
					new MBCharAtOtherMapmanOrder(parent, link));
			}
		}
	}

	@Override
	public void started(Player player) {
		String playerName = player.getName();
		GraphicalPlayerStatus status = player.getGraphicalPlayerStatus();
		parent.playerModule.notifyAll(new MBChangeCharOrder(parent, playerName, status));
	}

	@Override
	public Mover<Player, MapManager> createCompleteModTimer(Player player, Void argument) {
		if (!player.hasPath() || player.getPath().isEmpty())
			return null;
		Mover<Player, MapManager> mover = new Mover<Player, MapManager>(this, player, 500);//TODO: time unit
		MovingTask<Player, MapManager> movingTask = new RoughMovingTask(mover, player);
		mover.setMovingTask(movingTask);
		return mover;
	}

}
