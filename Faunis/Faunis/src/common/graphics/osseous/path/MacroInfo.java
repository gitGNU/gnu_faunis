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
package common.graphics.osseous.path;

import common.enums.Mood;


/**
 * Contains information needed to resolve some macros, for example
 * currentFrame, currentMood etc..
 */
public class MacroInfo {
	private int currentFrame;
	private Mood currentMood;
	
	/** Any parameter may be nulled and will be set to a default value if you do so. */
	public MacroInfo(Integer currentFrame, Mood currentMood) {
		if (currentFrame != null) {
			this.currentFrame = currentFrame;
		} else {
			this.currentFrame = 0;
		}
		if (currentMood != null) {
			this.currentMood = currentMood;
		} else {
			this.currentMood = Mood.normal;
		}
	}
	
	public int currentFrame() {
		return currentFrame;
	}
	
	public Mood currentMood() {
		return currentMood;
	}
}
