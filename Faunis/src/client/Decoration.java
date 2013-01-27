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
package client;

import java.awt.Graphics;

import communication.GraphicalDecoStatus;

public class Decoration implements Sprite {
	private Client parent;
	private GraphicalDecoStatus info;
	private ZOrderManager zOrderManager;
	
	public Decoration(Client parent, ZOrderManager manager, GraphicalDecoStatus info) {
		this.parent = parent;
		this.info = info;
		this.zOrderManager = manager;
	}
	
	@Override
	public void draw(Graphics drawOnto, int x, int y) {
		parent.getGraphicsContentManager().getDecoImage(info.name)
			.draw(drawOnto, x, y);
	}
	
	@Override
	public int getX() {
		return info.x;
	}
	@Override
	public int getY() {
		return info.y;
	}
	@Override
	public void setX(int x) {
		info.x = x;
	}
	/** locks zOrderedSprites */
	@Override
	public void setY(int y) {
		if (zOrderManager != null) {
			// Problem here: there is a slight time window when we
			// change the zOrderedSprites key and the actual y value
			// in which the values don't correspond.
			// -> Whenever we draw conclusions from a sprite's z value to
			// its key in zOrderedSprites, we have to lock zOrderedSprites
			// firsthand before reading the z value.
			Object zOrderedSprites = zOrderManager.getSynchroStuffForMovement();
			synchronized(zOrderedSprites) {
				zOrderManager.notifyZOrderChange(this, info.y, y);
				info.y = y;
			}
		} else {
			throw new RuntimeException("Missing zOrderManager!");
		}
	}
	@Override
	public float getZOrder() {
		return info.y;
	}
	@Override
	public void setZOrderManager(ZOrderManager listener) {
		zOrderManager = listener;
	}
}
