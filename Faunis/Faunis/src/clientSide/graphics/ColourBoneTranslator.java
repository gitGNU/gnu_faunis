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
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import common.enums.BodyPart;
import common.graphics.osseous.OffsetPoint;



public final class ColourBoneTranslator {
	private final HashMap<Color, Integer> colourToId;
	private final HashMap<Integer, BodyPart> idToBodyPart;
	private static ColourBoneTranslator instance;

	public static ColourBoneTranslator getInstance() {
		if (instance == null) {
			instance = new ColourBoneTranslator();
		}
		return instance;
	}

	private ColourBoneTranslator() {
		colourToId = new HashMap<Color, Integer>();
		// These are the colours of KolourPaint's default palette (KolourPaint 4.13.1)
		colourToId.put(new Color(0, 0, 0), 0);
		colourToId.put(new Color(255, 255, 255), 1);
		colourToId.put(new Color(128, 128, 128), 2);
		colourToId.put(new Color(192, 192, 192), 3);
		colourToId.put(new Color(255, 0, 0), 4);
		colourToId.put(new Color(128, 0, 0), 5);
		colourToId.put(new Color(255, 128, 0), 6);
		colourToId.put(new Color(128, 64, 0), 7);
		colourToId.put(new Color(255, 255, 0), 8);
		colourToId.put(new Color(128, 128, 0), 9);
		idToBodyPart = new HashMap<Integer, BodyPart>();
		idToBodyPart.put(0, BodyPart.body);
		idToBodyPart.put(1, BodyPart.head);
		idToBodyPart.put(2, BodyPart.leftArm);
		idToBodyPart.put(3, BodyPart.rightArm);
		idToBodyPart.put(4, BodyPart.leftLeg);
		idToBodyPart.put(5, BodyPart.rightLeg);
		idToBodyPart.put(6, BodyPart.tail);
	}

	public Integer colourToId(Color pixelColour) {
		return colourToId.get(pixelColour);
	}

	public BodyPart idToBodyPart(int id) {
		return idToBodyPart.get(id);
	}

	public Integer bodyPartToId(BodyPart bodyPart) {
		for (Entry<Integer, BodyPart> entry : idToBodyPart.entrySet()) {
			if (entry.getValue() == bodyPart) {
				return entry.getKey();
			}
		}
		return null;
	}

	public Color idToColour(int id) {
		for (Entry<Color, Integer> entry : colourToId.entrySet()) {
			if (entry.getValue().equals(id)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public Map<Integer, OffsetPoint> readOffsets(BufferedImage image) {
		Map<Integer, OffsetPoint> result = new HashMap<Integer, OffsetPoint>();
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				Color readColour = new Color(image.getRGB(x, y), true);
				Integer id = colourToId(readColour);
				if (id != null) {
					OffsetPoint alreadyRead = result.get(id);
					if (alreadyRead != null) {
						if (alreadyRead.y == y && alreadyRead.x == x-1) {
							result.put(id, new OffsetPoint(x - 0.5f, y));
						} else {
							throw new RuntimeException(
								"Mask contains id=" + id + " more than once!"
							);
						}
					} else {
						result.put(id, new OffsetPoint(x, y));
					}
				}
			}
		}
		return result;
	}
}
