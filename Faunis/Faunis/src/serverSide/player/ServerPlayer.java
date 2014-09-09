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
package serverSide.player;

import java.io.Serializable;
import java.util.Arrays;

import serverSide.inventory.Inventory;
import common.Logger;
import common.enums.CharacterClass;
import common.enums.Direction;
import common.enums.Mood;
import common.graphics.PlayerData;
import common.movement.Moveable;
import common.movement.MovingTask;
import common.movement.Path;


/** Represents a playable character on the server side. */
public class ServerPlayer implements Serializable, Moveable {
	private static final long serialVersionUID = 1L;
	private String name;	// unique player name
	private CharacterClass type;
	private Mood mood;
	private String currentMapName;
	private String accountName;
	private int x;
	private int y;
	private Direction direction;
	private transient Path path;
	private String currentAnimation; // do not store animations of end type "revert" here!
	private Inventory inventory;


	public ServerPlayer(String name, CharacterClass type, String currentMapName, String accountName) {
		this.name = name;
		this.type = type;
		this.currentMapName = currentMapName;
		this.accountName = accountName;
		this.direction = Direction.down;
		this.mood = Mood.normal;
		this.x = 5;
		this.y = 5;
		this.path = null;
		this.currentAnimation = null;
		this.inventory = new Inventory();
	}


	/** IMPORTANT: result has to be independent from the player,
	 *  that means no shared references etc.! */
	public PlayerData getPlayerData() {
		PlayerData graphStatus = new PlayerData();
		graphStatus.name = this.name;
		graphStatus.type = this.type;
		graphStatus.mood = this.mood;
		graphStatus.direction = this.direction;
		graphStatus.path = (this.path == null)? null : this.path.copy();
		graphStatus.currentAnimation = this.currentAnimation; // immutable :o]
		graphStatus.x = this.x;
		graphStatus.y = this.y;
		// DEBUG: Just to test the new accessoires feature:
		graphStatus.accessoires = Arrays.asList("jeans", "sweater");
		return graphStatus;
	}

	// getters and setters:

	public String getMapName() {
		return currentMapName;
	}
	public CharacterClass getType() {
		return type;
	}
	public Mood getMood() {
		return mood;
	}
	public void setMood(Mood mood) {
		this.mood = mood;
	}
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public String getName(){
		return this.name;
	}
	@Override
	public int getX() {
		return x;
	}
	@Override
	public int getY() {
		return y;
	}
	@Override
	public Path getPath() {
		return path;
	}
	@Override
	public void setPath(Path path) {
		assert(path != null);
		this.path = path;
	}
	@Override
	public void resetPath() {
		Logger.log("Reset path.");
		this.path = null;
	}
	@Override
	public boolean hasPath() {
		return (path != null);
	}
	public String getCurrentAnimation() {
		return currentAnimation;
	}
	public void setCurrentAnimation(String animation) {
		currentAnimation = animation;
	}
	public void resetAnimation() {
		this.currentAnimation = null;
	}
	public boolean isAnimating() {
		return (currentAnimation != null);
	}

	@Override
	public void moveAbsolute(int toX, int toY, boolean adaptDirection) {
		if (adaptDirection) {
			Direction newDirection = MovingTask.deltaToDirection(
									toX-this.x, toY-this.y);
			if (newDirection != null) {
				this.direction = newDirection;
			}
		}
		this.x = toX;
		this.y = toY;
	}

	public void setMapName(String mapName) {
		this.currentMapName = mapName;
	}

	public Inventory getInventory() {
		return inventory;
	}


	@Override
	public String toString() {
		return "Player [name=" + name + ", accountName=" + accountName + "]";
	}
}
