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
package server;

import java.io.Serializable;

import communication.GraphicalPlayerStatus;
import communication.enums.CharacterClass;
import communication.enums.Direction;
import communication.movement.Moveable;
import communication.movement.MovingTask;
import communication.movement.Path;


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
	private Path path;
	private String currentEmote; // do not store emotes of end type "revert" here!
	
	
	public Player(String name, CharacterClass type, String currentMapName, String accountName) {
		this.name = name;
		this.type = type;
		this.currentMapName = currentMapName;
		this.accountName = accountName;
		this.direction = Direction.down;
		this.x = 5;
		this.y = 5;
		this.path = null;
	}
	

	/** IMPORTANT: GraphicalPlayerStatus has to be independent from the player, 
	 *  that means no shared references etc.! */
	public GraphicalPlayerStatus getGraphicalPlayerStatus() {
		// TODO
		GraphicalPlayerStatus graphStatus = new GraphicalPlayerStatus();
		graphStatus.name = this.name;
		graphStatus.type = this.type;
		graphStatus.direction = this.direction;
		graphStatus.path = (this.path == null)? null : this.path.copy();
		graphStatus.currentEmote = this.currentEmote; // immutable :o]
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
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public Path getPath() {
		return path;
	}
	public void setPath(Path path) {
		this.path = path;
	}
	public void resetPath() {
		this.path = null;
	}
	public boolean hasPath() {
		return (path != null);
	}
	public String getCurrentEmote() {
		return currentEmote;
	}
	public void setCurrentEmote(String emote) {
		currentEmote = emote;
	}
	public void resetEmote() {
		this.currentEmote = null;
	}
	public boolean isAnimating() {
		return (currentEmote != null);
	}
	
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
}
