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
package clientSide.graphics;

import java.awt.Graphics;

/** Represents any placeable object that is drawable. */
public interface Drawable {
	/** Draws this object with its main origin at x,y (for characters:
	 * the 'body' reference point between the feet) onto the given Graphics */
	void draw(Graphics drawOnto, int x, int y);
	float getScale();
	int getX();
	int getY();
	void setX(int x);
	void setY(int y);
	float getZOrder();
	
	void setZOrderManager(ZOrderManager listener);
}