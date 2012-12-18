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
package client;

import java.awt.Graphics;
import java.awt.Point;
import java.util.List;

import communication.GraphicalPlayerStatus;
import communication.GraphicsContentManager;
import communication.enums.AniCompoType;
import communication.enums.BodyPart;
import communication.enums.CharacterClass;
import communication.enums.Direction;
import communication.movement.Moveable;
import communication.movement.MovingTask;
import communication.movement.Path;


/** Represents the graphics of a player. It can be completely created just from
 * the player's GraphicalPlayerStatus and the client's GraphicsContentManager. 
 * Any additional fields here don't have to be stored at / distributed over
 * the server and can be filled of each client during construction of playerGraphics. */
public class PlayerGraphics implements Moveable, Animateable {
	private GraphicalPlayerStatus info;
	private Client parent;
	private int deltaLevel = 0; // between -1 and +1
	private int frame = 0;
	
	
	public PlayerGraphics(GraphicalPlayerStatus info, Client parent) {
		this.info = info;
		this.parent = parent;
	}

	private Bone getBone(BodyPart part) {
		String animation;
		if (hasPath())
			animation = "walk";
		else if (info.currentEmote != null)
			animation = info.currentEmote;
		else
			animation = "stand";
		return parent.getGraphicsContentManager().
				getBone(info.type, animation, part, info.direction);
	}
	
	public void draw(Graphics drawOnto, int x, int y, int frameIndex) {
		// Decide whether we have a limbed or compact type
		GraphicsContentManager graphicsContentManager = parent.getGraphicsContentManager();
		AniCompoType compoType = 
				graphicsContentManager.getCompositionType(info.type);
		List<BodyPart> orderList;
		if (compoType == AniCompoType.compact) {
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
			System.out.println("Couldn't find bone for "+currentPart+"!");
		}
		Point offset = currentBone.getConnectionOffset(currentPart, frameIndex);
		if (offset == null) {
			System.out.println("Couldn't find offset for "+currentPart+", frame "+frameIndex+"!");
		}
		assert(offset != null);

		
		for (BodyPart toDraw : orderList) {
			for (BodyPart part : currentBone.getConnectionOffsets(frameIndex)) {
				if (part != sourcePart && part == toDraw) {
					Point partOffset = currentBone.getConnectionOffset(part, frameIndex);
					drawRecursive(part, currentPart, originX-(offset.x-partOffset.x), 
										originY-(offset.y-partOffset.y),
										drawOnto, orderList, frameIndex);
				}
				if (toDraw == currentPart) {
					currentBone.draw(drawOnto, originX-offset.x, originY-offset.y, frameIndex);
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
		info.path = path;
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
	public void resetPath() {
		info.path = null;
		this.deltaLevel = 0;
		System.out.println("Reset path.");
	}
	
	@Override
	public void resetEmote() {
		info.currentEmote = null;
		this.frame = 0;
		System.out.println("Set frame to 0.");
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
			System.out.println("ERROR: setDirection(null)");
		assert(direction != null);
		info.direction = direction;
	}
	
	@Override
	public String getEmote() {
		return info.currentEmote;
	}
	
	@Override
	public void setEmote(String emote) {
		info.currentEmote = emote;
	}
	
	@Override
	public boolean hasEmote() {
		return info.hasEmote();
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
	 * If given flag adaptDirection is set, sets the direction accordingly. */
	@Override
	public void moveAbsolute(int x, int y, boolean adaptDirection) {
		if (adaptDirection) {
			Direction newDirection = MovingTask.deltaToDirection(
									x-info.x, y-info.y);
			if (newDirection != null)
				info.direction = newDirection;
		}
		info.x = x;
		info.y = y;
	}
}
