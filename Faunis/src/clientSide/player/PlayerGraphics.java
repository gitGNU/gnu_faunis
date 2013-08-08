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
package clientSide.player;

import java.awt.Graphics;
import java.awt.Point;
import java.util.List;

import clientSide.animation.Animateable;
import clientSide.client.Client;
import clientSide.graphics.Bone;
import clientSide.graphics.Drawable;
import clientSide.graphics.ZOrderManager;

import common.Logger;
import common.enums.AniCompoType;
import common.enums.BodyPart;
import common.enums.CharacterClass;
import common.enums.Direction;
import common.graphics.GraphicalPlayerStatus;
import common.graphics.GraphicsContentManager;
import common.movement.Moveable;
import common.movement.MovingTask;
import common.movement.Path;


/** Represents the graphics of a player. It can be completely created just from
 * the player's GraphicalPlayerStatus and the client's GraphicsContentManager.
 * Any additional fields here don't have to be stored at / distributed over
 * the server and can be filled by each client during construction of playerGraphics. */
public class PlayerGraphics implements Moveable, Animateable, Drawable {
	private GraphicalPlayerStatus info;
	private Client parent;
	private int deltaLevel = 0; // between -1 and +1
	private int frame = 0;
	private float scale;
	private ZOrderManager zOrderManager;
	
	public PlayerGraphics(GraphicalPlayerStatus info, Client parent, ZOrderManager manager) {
		this.info = info;
		this.parent = parent;
		this.zOrderManager = manager;
		if (parent.hasGraphicalOutput())
			this.scale = parent.getGraphicsContentManager().getScale(info.type);
	}

	private Bone getBone(BodyPart part) {
		String animation;
		if (hasPath())
			animation = "walk";
		else if (info.currentAnimation != null)
			animation = info.currentAnimation;
		else
			animation = "stand";
		return parent.getGraphicsContentManager().
				getBone(info.type, animation, part, info.direction);
	}
	
	@Override
	public void draw(Graphics drawOnto, int x, int y) {
		int frameIndex = this.frame;
		// Decide whether we have a limbed or compact type
		GraphicsContentManager graphicsContentManager = parent.getGraphicsContentManager();
		AniCompoType compoType =
				graphicsContentManager.getCompositionType(info.type);
		List<BodyPart> orderList;
		if (compoType == AniCompoType.COMPACT) {
			orderList = graphicsContentManager.getCompactDrawingOrder();
		} else {
		 orderList = graphicsContentManager.
				 getDrawingOrders().get(this.getDirection());
		}
		assert(orderList != null);
		drawRecursive(BodyPart.body, null, x, y, drawOnto,
				orderList, frameIndex);
	}
	
	private void drawRecursive(BodyPart currentPart, BodyPart sourcePart, int originX, int originY,
	 Graphics drawOnto, List<BodyPart> orderList, int frameIndex) {
		// we need sourcePart only for preventing going backwards recursively
		Bone currentBone = getBone(currentPart);
		if (currentBone == null) {
			Logger.log("Couldn't find bone for "+currentPart+"!");
		}
		assert(currentBone != null);
		Point offset = currentBone.getConnectionOffset(currentPart, frameIndex, getScale());
		if (offset == null) {
			Logger.log("Couldn't find offset for "+currentPart+", frame "+frameIndex+"!");
		}
		assert(offset != null);

		
		for (BodyPart toDraw : orderList) {
			for (BodyPart part : currentBone.getConnectionOffsets(frameIndex, getScale())) {
				if (part != sourcePart && part == toDraw) {
					Point partOffset = currentBone.getConnectionOffset(part, frameIndex, getScale());
					drawRecursive(part, currentPart, originX-(offset.x-partOffset.x),
										originY-(offset.y-partOffset.y),
										drawOnto, orderList, frameIndex);
				}
				if (toDraw == currentPart) {
					currentBone.draw(drawOnto, originX-offset.x, originY-offset.y, frameIndex, getScale());
				}
			}
		}
	}
	
	public CharacterClass getType() {
		return info.type;
	}

	@Override
	public Path getPath() {
		return info.path;
	}

	@Override
	public void setPath(Path path) {
		assert(path != null);
		info.path = path;
	}
	
	@Override
	public float getScale() {
		return this.scale;
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
	/** locks zOrderedDrawables */
	@Override
	public void setY(int y) {
		if (zOrderManager != null) {
			// Problem here: there is a slight time window when we
			// change the zOrderedDrawables key and the actual y value
			// in which the values don't correspond.
			// -> Whenever we draw conclusions from a Drawable's z value to
			// its key in zOrderedDrawables, we have to lock zOrderedDrawables
			// firsthand before reading the z value.
			Object zOrderedDrawables = zOrderManager.getZOrderedDrawables();
			synchronized(zOrderedDrawables) {
				zOrderManager.notifyZOrderChange(this, info.y, y);
				info.y = y;
			}
		} else {
			throw new RuntimeException("Missing zOrderManager!");
		}
	}
	
	@Override
	public float getZOrder() {
		int deltaLevelAmplitude = parent.getClientSettings().deltaLevelAmplitude();
		return info.y + (this.deltaLevel / (float)deltaLevelAmplitude);
	}
	
	@Override
	public void setZOrderManager(ZOrderManager listener) {
		zOrderManager = listener;
	}

	@Override
	public void resetPath() {
		info.path = null;
		this.deltaLevel = 0;
		Logger.log("Reset path.");
	}
	
	@Override
	public void resetAnimation() {
		info.currentAnimation = null;
		this.frame = 0;
		Logger.log("Set frame to 0.");
	}
	
	public int getDeltaLevel() {
		return this.deltaLevel;
	}
	
	public void setDeltaLevel(int deltaLevel) {
		this.deltaLevel = deltaLevel;
	}
	
	@Override
	public boolean hasPath() {
		return info.hasPath();
	}
	
	public String getName() {
		return info.name;
	}
	
	public Direction getDirection() {
		return info.direction;
	}
	
	public void setDirection(Direction direction) {
		if (direction == null)
			Logger.log("ERROR: setDirection(null)");
		assert(direction != null);
		info.direction = direction;
	}
	
	@Override
	public String getAnimation() {
		return info.currentAnimation;
	}
	
	@Override
	public void setAnimation(String animation) {
		info.currentAnimation = animation;
	}
	
	@Override
	public boolean hasAnimation() {
		return info.hasAnimation();
	}
	
	@Override
	public int getFrame() {
		return frame;
	}
	
	@Override
	public void setFrame(int frame) {
		this.frame = frame;
	}

	/** Directly sets the coordinates of this playerGraphics to the given ones.
	 * If given flag adaptDirection is set, sets the direction accordingly.
	*/
	@Override
	public void moveAbsolute(int x, int y, boolean adaptDirection) {
		if (adaptDirection) {
			Direction newDirection = MovingTask.deltaToDirection(
									x-info.x, y-info.y);
			if (newDirection != null)
				info.direction = newDirection;
		}
		setX(x);
		setY(y);
	}
}
