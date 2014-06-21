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

import serverSide.inventory.Inventory;

import common.Logger;
import common.enums.CharacterClass;
import common.enums.Direction;
import common.graphics.GraphicalPlayerStatus;
import common.movement.Moveable;
import common.movement.MovingTask;
import common.movement.Path;


/** Represents a playable character on the server side. */
public class Player implements Serializable, Moveable {
	private static final long serialVersionUID = 1L;
	private String name;	// unique player name
	private CharacterClass type;
	private String currentMapName;
	private String accountName;
	private int x;
	private int y;
	private Direction direction;
	private transient Path path;
	private String currentAnimation; // do not store animations of end type "revert" here!
	private Inventory inventory;
	
	
	public Player(String name, CharacterClass type, String currentMapName, String accountName) {
		this.name = name;
		this.type = type;
		this.currentMapName = currentMapName;
		this.accountName = accountName;
		this.direction = Direction.down;
		this.x = 5;
		this.y = 5;
		this.path = null;
		this.currentAnimation = null;
		this.inventory = new Inventory();
	}
	

	/** IMPORTANT: GraphicalPlayerStatus has to be independent from the player,
	 *  that means no shared references etc.! */
	public GraphicalPlayerStatus getGraphicalPlayerStatus() {
		GraphicalPlayerStatus graphStatus = new GraphicalPlayerStatus();
		graphStatus.name = this.name;
		graphStatus.type = this.type;
		graphStatus.direction = this.direction;
		graphStatus.path = (this.path == null)? null : this.path.copy();
		graphStatus.currentAnimation = this.currentAnimation; // immutable :o]
		graphStatus.x = this.x;
		graphStatus.y = this.y;
		return graphStatus;
	}
	
	// getters and setters:
	
	public String getMapName() {
		return currentMapName;
	}
	public CharacterClass getType() {
		return type;
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
			if (newDirection != null)
				this.direction = newDirection;
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
