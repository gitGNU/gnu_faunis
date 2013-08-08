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
package common.graphics;

import java.io.Serializable;

import common.enums.CharacterClass;
import common.enums.Direction;
import common.movement.Path;


/** Contains the whole information about how to correctly draw a player in its current
 * state. Is transmitted in serialised form. */
public class GraphicalPlayerStatus implements Serializable {
	private static final long serialVersionUID = 1L;
	public String name;
	public CharacterClass type;
	public int x;
	public int y;
	public Direction direction;
	public Path path = null;
	public String currentAnimation; // "revert" animations may also be stored here
	
	public boolean hasPath() {
		return (path != null);
	}
	
	public boolean hasAnimation() {
		return (currentAnimation != null);
	}
}
