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

import java.util.Timer;

public class Mover {
	protected MoverManager parent;
	protected Moveable moveable;
	protected Timer timer;
	protected MovingTask movingTask;
	protected long interval;
	
	/** PLEASE NOTE: You'll also have to create a MovingTask for this Mover
	 * and assign it by calling Mover.setMovingTask()! Afterwards, don't forget
	 * that this Mover isn't started yet. */
	public Mover(MoverManager parent, Moveable moveable,
			long delay) {
		this.parent = parent;
		this.moveable = moveable;
		this.interval = delay;
		this.timer = new Timer();
	}
	
	public void setMovingTask(MovingTask movingTask) {
		this.movingTask = movingTask;
	}
	
	/** Starts the MovingTask. Has to be explicitely called,
	 * as that isn't done automatically.<br/>
	 * HINT: Doesn't register this Mover at the parent, because that must
	 * already happen while creating the Mover. */
	public void start() {
		this.timer.scheduleAtFixedRate(movingTask, 0, interval);
	}
	
	public Moveable getMoveable() {
		return moveable;
	}
	
	public void stop() {
		this.movingTask.stop();
	}
	
	public void stopAndUnregister() {
		this.stop();
		parent.unregisterMover(this.moveable);
	}
}
