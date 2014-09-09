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


/** Macros which can be used in properties files under "clientData/graphics/" */
public class PathMacros {
	/** 
	 * Do not confuse with PathMacros.ancestorName, this is completely different!<br />
	 * "up": Go up one level.
	 * "up" is not really a macro, e.g. it has to work even in ClearPaths,
	 * I just did not find a good place where to define it...
	 */
	public static final String up = "..";
	/** Default value: "0" */
	public static final String currentFrame = "*currentFrame";
	/**
	 * Do not confuse with PathMacros.up, this is completely different!<br />
	 * "ancestorName" inserts the name of the current osseous' ancestor n-th levels above,
	 * where n is defined by the suffix integer you have to append to the macro.
	 * n=0 is the current osseous itself, n=1 its direct parent, etc.. The macro will
	 * throw a MacroException if you go up too far / do not append a valid non-negative integer.
	 */
	public static final String ancestorName = "..";
	/** Default value: "normal" */
	public static final String currentMood = "*currentMood";
}
