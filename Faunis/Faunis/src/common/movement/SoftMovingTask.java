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

import clientSide.ClientSettings;
import clientSide.client.Client;
import clientSide.player.ClientPlayer;

import common.enums.Direction;

/** For the client side: Moves the player along the path, but between adjusting
 * the coordinates, special values (deltaLevel) are also set, such that the
 * animation looks softer. Therefore it needs more (frequent) calls of move()
 * than RoughMovingTask, and the coordinates are not changed with every call.
*/
public class SoftMovingTask extends MovingTask<ClientPlayer, Client> {

	public SoftMovingTask(Mover<ClientPlayer, Client> parent, ClientPlayer moveable) {
		super(parent, moveable);
	}

	@Override
	protected boolean isMovementFinished() {
		return (moveable.getPath().isEmpty());
	}

	@Override
	protected void move() {
		synchronized(runningMutexKey) {
			if (stopRunning) {
				return;
			}
			ClientPlayer player = moveable;
			assert(player != null);
			ClientSettings settings = parent.parent.parent().getClientSettings();
			int deltaLevelAmplitude = settings.deltaLevelAmplitude();
			Path path = player.getPath();
			assert(path != null);
			int deltaLevel = player.getDeltaLevel();
			Point nextPoint = path.top();
			//Logger.log("deltaLevel="+deltaLevel+", nextPoint="+nextPoint);
			assert(!(deltaLevel == 0 && nextPoint == null));
			// We know where to go to,
			// and we also know the deltaLevel
			deltaLevel++;

			if (deltaLevel == 0) {
				player.setDeltaLevel(deltaLevel);
				// stop movement if there's no further waypoint
				if (nextPoint == null) {
					// stop movement
//						player.resetPath();
					return;
				}
			} else if (deltaLevel == 1) {
				// adapt direction to next waypoint
				Direction newDirection = MovingTask.deltaToDirection(
						nextPoint.x-player.getX(), nextPoint.y-player.getY());
				if (newDirection != null) {
					player.setDirection(newDirection);
				}
				player.setDeltaLevel(deltaLevel);
			} else if (deltaLevel > deltaLevelAmplitude) {
				// move over to the next field and remove waypoint from path
				deltaLevel = -deltaLevelAmplitude;
				player.setDeltaLevel(deltaLevel);
				assert(nextPoint != null);
				player.moveAbsolute(nextPoint.x, nextPoint.y, false);
				path.pop();
			} else {
				player.setDeltaLevel(deltaLevel);
			}
		}
	}

}
