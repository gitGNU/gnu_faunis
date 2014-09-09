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
import java.util.TimerTask;
import common.enums.Direction;
import common.modules.ModuleOwner;

public abstract class MovingTask<MOVEABLE extends Moveable, MODULE_OWNER extends ModuleOwner> extends TimerTask {
	protected Object runningMutexKey;
	protected boolean stopRunning;
	protected Mover<MOVEABLE, MODULE_OWNER> parent;
	protected MOVEABLE moveable;
	public MovingTask(Mover<MOVEABLE, MODULE_OWNER> parent, MOVEABLE moveable) {
		this.parent = parent;
		this.moveable = moveable;
		this.runningMutexKey = new Object();
		synchronized(runningMutexKey) {
			this.stopRunning = false;
		}
	}

	@Override
	public void run() {
		// move moveable to target and
		// unregister at parent when target reached
		/* PROBLEM: On the client side, I want to call
		 tryStopAnimation() in the unregisterMover() method.
		 But that forces me to first synchronise on the locked
		 resources there before I synchronise on moveable.
		*/
		synchronized(runningMutexKey) {
			if (stopRunning) {
				return;
			}
			if (isMovementFinished()) {
				parent.stopAndUnregister();
			} else {
				move();
			}
		}
	}

	protected abstract boolean isMovementFinished();
	protected abstract void move();

	public void stop() {
		synchronized(runningMutexKey) {
			parent.timer.cancel();
			this.stopRunning = true;
			moveable.resetPath();
		}
	}

	public static Direction deltaToDirection(int deltaX, int deltaY) {
		if (Math.abs(deltaX) >= Math.abs(deltaY)) {
			if (deltaX < 0) {
				return Direction.left;
			} else if (deltaX > 0) {
				return Direction.right;
			} else {
				return null;
			}
		} else {
			if (deltaY < 0) {
				return Direction.up;
			} else if (deltaY > 0) {
				return Direction.down;
			} else {
				return null;
			}
		}
	}

	public static Point directionToDelta(Direction direction) {
		if (direction == null) {
			return new Point(0,0);
		}
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
