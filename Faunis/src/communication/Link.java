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
package communication;

import java.io.Serializable;

import communication.enums.CoordinateType;
import communication.movement.Moveable;

public class Link implements Serializable{
	private static final long serialVersionUID = 1L;
	private String sourceMap;
	private int sourceX;
	private int sourceY;
	private int targetX;
	private int targetY;
	private CoordinateType targetXType;
	private CoordinateType targetYType;
	private String targetMap;
	
	public Link(String sourceMap, int sourceX, int sourceY, String targetMap,
				int targetX, int targetY,
				CoordinateType targetXType, CoordinateType targetYType) {
		this.sourceMap = sourceMap;
		this.sourceX = sourceX;
		this.sourceY = sourceY;
		this.targetMap = targetMap;
		this.targetX = targetX;
		this.targetY = targetY;
		this.targetXType = targetXType;
		this.targetYType = targetYType;
	}
	
	/** This method doesn't synchronise on moveable, please do that before. */
	public void move(Moveable moveable) {
		int[] coordinate = new int[2];
		coordinate[0] = moveable.getX();
		coordinate[1] = moveable.getY();
		switch(targetXType) {
			case ABSOLUTE:
				coordinate[0] = targetX;
				break;
			case RELATIVE:
				coordinate[0] += targetX;
				break;
		}
		switch(targetYType) {
			case ABSOLUTE:
				coordinate[1] = targetY;
				break;
			case RELATIVE:
				coordinate[1] += targetY;
				break;
		}
		moveable.moveAbsolute(coordinate[0], coordinate[1], false);
	}
	
	public String getSourceMap() {
		return sourceMap;
	}
	
	public int getSourceX() {
		return sourceX;
	}
	
	public int getSourceY() {
		return sourceY;
	}
	
	public String getTargetMap() {
		return targetMap;
	}
	
	public int getTargetX() {
		return targetX;
	}
	
	public int getTargetY() {
		return targetY;
	}
	
	public CoordinateType getTargetXType() {
		return targetXType;
	}
	
	public CoordinateType getTargetYType() {
		return targetYType;
	}
}
