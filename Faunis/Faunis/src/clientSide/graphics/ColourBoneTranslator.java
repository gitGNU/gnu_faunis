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
package clientSide.graphics;

import java.awt.Color;
import java.util.HashMap;

import common.enums.BodyPart;



public final class ColourBoneTranslator {
	private HashMap<Color, BodyPart> colourToBone;
	private static ColourBoneTranslator instance;
	
	public static ColourBoneTranslator getInstance() {
		if (instance == null)
			instance = new ColourBoneTranslator();
		return instance;
	}
	
	private ColourBoneTranslator() {
		colourToBone = new HashMap<Color, BodyPart>();
		colourToBone.put(Color.MAGENTA, BodyPart.head);
		colourToBone.put(Color.ORANGE, BodyPart.body);
		colourToBone.put(Color.YELLOW, BodyPart.tail);
		colourToBone.put(Color.GREEN, BodyPart.rightArm);
		colourToBone.put(Color.CYAN, BodyPart.leftArm);
		colourToBone.put(Color.RED, BodyPart.rightLeg);
		colourToBone.put(Color.BLUE, BodyPart.leftLeg);
	}
	
	
	public BodyPart translate(Color pixelColour) {
		return colourToBone.get(pixelColour);
	}
}
