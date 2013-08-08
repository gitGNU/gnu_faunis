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
package common.movement;

import java.awt.Point;
import java.io.Serializable;
import java.util.LinkedList;


/** FIFO-queue of coordinates along which a moveable can be moved. */
public class Path implements Serializable {
	private static final long serialVersionUID = 1L;
	LinkedList<Point> steps;
	
	public Path() {
		steps = new LinkedList<Point>();
	}
	
	/** deep copy */
	public Path copy() {
		Path result = new Path();
		for (Point step : this.steps) {
			result.push((Point)step.clone());
		}
		return result;
	}
	
	public void push(Point step) {
		steps.add(step);
	}
	
	/** Removes the next waypoint from this path and returns it,
	 * or returns null if this path is empty. */
	public Point pop() {
		if (steps.isEmpty())
			return null;
		else
			return steps.pop();
	}
	
	/** Returns the next waypoint or null if path is empty */
	public Point top() {
		return steps.peek();
	}
	
	public boolean isEmpty() {
		return steps.isEmpty();
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (Point step : steps) {
			stringBuilder.append("(");
			stringBuilder.append(step.x);
			stringBuilder.append(",");
			stringBuilder.append(step.y);
			stringBuilder.append(")");
		}
		return stringBuilder.toString();
	}
}
