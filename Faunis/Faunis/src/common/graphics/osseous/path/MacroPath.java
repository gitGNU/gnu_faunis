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

import java.util.List;

import common.graphics.osseous.Bone;
import common.graphics.osseous.Osseous;


/** A path which may contain macros (placeholders, like currentFrame or mood f. ex.) */
public class MacroPath extends Path {
	
	public MacroPath(List<String> path) {
		super(path);
	}

	public MacroPath(String... elements) {
		super(elements);
	}
	
	@Override
	public MacroPath copy() {
		return new MacroPath(path);
	}

	/** 
	 * Returns a new ClearPath instance from this MacroPath by replacing macros / placeholders,
	 * using given MacroInfo. If MacroInfo is null, will insert default values.
	 * @param macroInfo May be null.
	 */
	public ClearPath replaceMacros(Osseous currentOsseous, MacroInfo macroInfo) throws MacroException {
		ClearPath result = new ClearPath();
		for (String element : this) {
			if (
				element.startsWith(PathMacros.ancestorName)
				&& element.length() > PathMacros.ancestorName.length()
			) {
				String levelsUpString = element.substring(PathMacros.ancestorName.length());
				int levelsUp;
				try {
					levelsUp = Integer.parseInt(
						levelsUpString
					);
				} catch(NumberFormatException e) {
					throw new MacroException(
						PathMacros.ancestorName
						+ ": Invalid specification of levels up given: " + levelsUpString
					);
				}
				if (levelsUp < 0) {
					throw new MacroException(
							PathMacros.ancestorName
							+ ": Got a negative level specification: " + levelsUp
						);
				}
				// now go up levelsUp times and insert the parent's name here
				Osseous upperOsseous = currentOsseous;
				for (int i=1; i<=levelsUp; i++) {
					upperOsseous = upperOsseous.parent();
					if (upperOsseous == null) {
						throw new MacroException(
							PathMacros.ancestorName + ": Was told to refer to parent "
							+ levelsUp + " levels above, but there was no parent at level " + i
						);
					}
				}
				result.add(upperOsseous.name());
			} else if (element.equals(PathMacros.currentMood)) {
				result.add(macroInfo.currentMood().toString());
			} else if (
				element.startsWith(PathMacros.currentFrame)
			) {
				if (!(currentOsseous instanceof Bone)) {
					throw new MacroException(
						PathMacros.currentFrame + ": The current element '"
						+ currentOsseous + "' in path '" + this + "' must address a Bone!"
					);
				}
				Bone currentBone = (Bone) currentOsseous;
				// relative frame index specified
				int frameIndex = macroInfo.currentFrame();
				if (element.length() > PathMacros.currentFrame.length() + 1) {
					// frameIndex is relative to current index by a
					// difference indicated by f.ex. "+1", "-2" etc.
					// => calculate frameIndex modulo number of frames
					char operator = element.charAt(PathMacros.currentFrame.length());
					int difference = Integer.parseInt(
						element.substring(PathMacros.currentFrame.length() + 1)
					);
					if (operator == '+') {
						frameIndex += difference;
					} else if (operator == '-') {
						frameIndex -= difference;
					} else {
						throw new IllegalArgumentException(
							"Invalid operator after PathMacros.currentFrame: " + element
						);
					}
					frameIndex = frameIndex % currentBone.numberOfFrames();
					// frameIndex may still be negative as Java doesn't use the mathematical modulo
					if (frameIndex < 0) { frameIndex += currentBone.numberOfFrames(); }
				}
				result.add(String.valueOf(frameIndex));
			} else {
				result.add(element);
			}
		}
		return result;
	}
}
