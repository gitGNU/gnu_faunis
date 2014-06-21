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
package common.movement;

import java.awt.Point;

import serverSide.mapManager.MapManager;
import serverSide.player.Player;

/** For the server side: Moves the Player in whole steps along the path
 *  without any intermediate animation adjustments like in SoftMovingTask.
 *  Therefore, one call of move() = one field onward. */
public class RoughMovingTask extends MovingTask<Player, MapManager> {

	public RoughMovingTask(Mover<Player, MapManager> parent, Player moveable) {
		super(parent, moveable);
	}

	@Override
	protected boolean isMovementFinished() {
		return (moveable.getPath().isEmpty());
	}

	@Override
	protected void move() {
		synchronized(runningMutexKey) {
			if (stopRunning)
				return;
			Path path = moveable.getPath();
			assert(path != null);
			Point nextPoint = path.pop();
			if (nextPoint == null) {
//					moveable.resetPath();
				return;
			}
			moveable.moveAbsolute(nextPoint.x, nextPoint.y, true);
//				if (path.isEmpty()) moveable.resetPath();
		}
	}

}
