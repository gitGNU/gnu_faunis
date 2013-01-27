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
package communication.movement;

import java.awt.Point;

import client.Client;
import client.ClientSettings;
import client.PlayerGraphics;

import communication.enums.Direction;

/** For the client side: Moves the player along the path, but between adjusting
 * the coordinates, special values (deltaLevel) are also set, such that the
 * animation looks softer. Therefore it needs more (frequent) calls of move()
 * than RoughMovingTask, and the coordinates are not changed with every call.
*/
public class SoftMovingTask extends MovingTask {

	public SoftMovingTask(Mover parent, Moveable moveable) {
		super(parent, moveable);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean isMovementFinished() {
		return (moveable.getPath().isEmpty());
	}

	@Override
	protected void move() {
		Object movementSynchroStuff = parent.parent.getSynchroStuffForMovement();
		synchronized(movementSynchroStuff) {
			synchronized(moveable) {
				synchronized(runningMutexKey) {
					if (stopRunning)
						return;
					PlayerGraphics playerGraphics = (PlayerGraphics) moveable;
					assert(playerGraphics != null);
					ClientSettings settings = Client.getClientSettings();
					int deltaLevelAmplitude = settings.deltaLevelAmplitude();
					Path path = playerGraphics.getPath();
					assert(path != null);
					int deltaLevel = playerGraphics.getDeltaLevel();
					Point nextPoint = path.top();
					//System.out.println("deltaLevel="+deltaLevel+", nextPoint="+nextPoint);
					assert(!(deltaLevel == 0 && nextPoint == null));
					// We know where to go to,
					// and we also know the deltaLevel
					deltaLevel++;
			
					if (deltaLevel == 0) {
						playerGraphics.setDeltaLevel(deltaLevel);
						// stop movement if there's no further waypoint
						if (nextPoint == null) {
							// stop movement
	//						playerGraphics.resetPath();
							return;
						}
					} else if (deltaLevel == 1) {
						// adapt direction to next waypoint
						Direction newDirection = MovingTask.deltaToDirection(
								nextPoint.x-playerGraphics.getX(), nextPoint.y-playerGraphics.getY());
						if (newDirection != null)
							playerGraphics.setDirection(newDirection);
						playerGraphics.setDeltaLevel(deltaLevel);
					} else if (deltaLevel > deltaLevelAmplitude) {
						// move over to the next field and remove waypoint from path 
						deltaLevel = -deltaLevelAmplitude;
						playerGraphics.setDeltaLevel(deltaLevel);
						assert(nextPoint != null);
						playerGraphics.moveAbsolute(nextPoint.x, nextPoint.y, false);
						path.pop();
					} else {
						playerGraphics.setDeltaLevel(deltaLevel);
					}
				}
			}
		}
	}

}
