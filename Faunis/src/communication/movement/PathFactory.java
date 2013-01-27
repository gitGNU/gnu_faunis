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

import communication.Map;

public class PathFactory {
	public static Path createAirlinePath(int fromX, int fromY, int toX, int toY) {
		Path result = new Path();
		int currentX = fromX;
		int currentY = fromY;
		while (currentX != toX || currentY != toY) {
			int deltaX = toX-currentX;
			int deltaY = toY-currentY;
			if (Math.abs(deltaX) > Math.abs(deltaY)) {
				currentX += Math.signum(deltaX);
				Point point = new Point(currentX, currentY);
				result.push(point);
			} else {
				currentY += Math.signum(deltaY);
				Point point = new Point(currentX, currentY);
				result.push(point);
			}
		}
		assert(!result.isEmpty());
		System.out.println("Path=" + result.toString());
		return result;
	}
	
	/*
	public static Path createShortestPath(Map map, int fromX, int fromY, int toX, int toY) {
		// TODO
	}
	*/
}
