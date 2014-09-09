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
package clientSide.player;

import java.util.List;

import clientSide.animation.Animateable;
import clientSide.client.Client;
import clientSide.graphics.MapDrawable;
import clientSide.graphics.ZOrderManager;
import common.Logger;
import common.enums.CharacterClass;
import common.enums.Direction;
import common.enums.Mood;
import common.graphics.PlayerData;
import common.movement.Moveable;
import common.movement.MovingTask;
import common.movement.Path;


/** Represents the player on the client side. It can be completely created just
 * from the player's PlayerData and the client's GraphicsContentManager.
 * Put only those additional fields in this class which don't have to be
 * stored at / distributed over the server and can be filled by each client
 * during construction of this. */
public class ClientPlayer implements Moveable, Animateable, MapDrawable {
	private PlayerData info;
	private Client parent;
	private int deltaLevel = 0; // between -1 and +1
	private int frame = 0;
	private ZOrderManager zOrderManager;

	public ClientPlayer(PlayerData info, Client parent, ZOrderManager manager) {
		this.info = info;
		this.parent = parent;
		this.zOrderManager = manager;
	}

	public CharacterClass getType() {
		return info.type;
	}

	public Mood getMood() {
		return info.mood;
	}

	public void setMood(Mood mood) {
		info.mood = mood;
	}

	public List<String> getAccessoires() {
		return info.accessoires;
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
			zOrderManager.notifyZOrderChange(this, info.y, y);
			info.y = y;
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
		if (direction == null) {
			Logger.log("ERROR: setDirection(null)");
		}
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

	/** Directly sets the coordinates of this player to the given ones.
	 * If given flag adaptDirection is set, sets the direction accordingly.
	*/
	@Override
	public void moveAbsolute(int x, int y, boolean adaptDirection) {
		if (adaptDirection) {
			Direction newDirection = MovingTask.deltaToDirection(
									x-info.x, y-info.y);
			if (newDirection != null) {
				info.direction = newDirection;
			}
		}
		setX(x);
		setY(y);
	}

	@Override
	public String toString() {
		return getName();
	}
}
