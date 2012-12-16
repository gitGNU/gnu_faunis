/* Copyright 2012 Simon Ley alias "skarute"
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
import java.util.TimerTask;

import communication.enums.Direction;

public abstract class MovingTask extends TimerTask {
	protected Object runningMutexKey;
	protected boolean stopRunning;
	protected Mover parent;
	protected Moveable moveable;
	public MovingTask(Mover parent, Moveable moveable) {
		this.parent = parent;
		this.moveable = moveable;
		this.runningMutexKey = new Object();
		this.stopRunning = false;
	}
	
	/** Locks moveable, runningMutexKey or moving-list, animation-list, moveable */
	@Override
	public void run() {
		// move moveable to target and 
		// unregister at parent when target reached
		/* PROBLEM: On the client side, I want to call
		 tryStopAnimation() in the unregisterMover() method.
		 But that forces me to first synchronise on the locked
		 resources there before I synchronise on moveable.
		*/
		synchronized(moveable) {
			if (!this.isMovementFinished()) {
				this.move();
				return;
			}
		}
		// else:
		Object[] lists = parent.parent.getSynchroStuffForMoverStop();
		assert(lists != null);
		if (lists.length == 1) {
			synchronized(lists[0]) {
				synchronized(moveable) {
					System.out.println("Synchronized on movingPlayerGraphics,"
							+" moveable.");
					if (this.isMovementFinished())
						parent.stopAndUnregister();
				}
			}
		} else if (lists.length == 2) {
			synchronized(lists[0]) {
				synchronized(lists[1]) {
					System.out.println("Synchronized on movingPlayerGraphics,"
							+" animatedPlayerGraphics, moveable.");
					synchronized(moveable) {
						if (this.isMovementFinished())
							parent.stopAndUnregister();
					}
				}
			}
		} else {
			throw new RuntimeException("MovingTask: Lists array is of strange size!");
		}

	}
	
	protected abstract boolean isMovementFinished();
	protected abstract void move();
	
	public void stop() {
		parent.timer.cancel();
		synchronized(runningMutexKey) {
			this.stopRunning = true;
		}
		synchronized(moveable) {
			moveable.resetPath();
		}
	}
	
	public static Direction deltaToDirection(int deltaX, int deltaY) {
		if (Math.abs(deltaX) >= Math.abs(deltaY)) {
			if (deltaX < 0)
				return Direction.left;
			else if (deltaX > 0)
				return Direction.right;
			else
				return null;
		} else {
			if (deltaY < 0)
				return Direction.up;
			else if (deltaY > 0)
				return Direction.down;
			else
				return null;
		}
	}
	
	public static Point directionToDelta(Direction direction) {
		if (direction == null)
			return new Point(0,0);
		switch(direction) {
		case left:
			return new Point(-1,0);
		case right:
			return new Point(1,0);
		case up:
			return new Point(0,-1);
		case down:
			return new Point(0,1);
		}
		throw new RuntimeException("directionToDelta: Unknown direction!");
	}
}
