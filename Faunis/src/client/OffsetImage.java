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
package client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;


public class OffsetImage {
	private BufferedImage image;
	private Point offset;
	
	public OffsetImage(String filePath) {
		try {
			File graphicFile = new File(filePath);
			image = ImageIO.read(graphicFile);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Couldn't load picture!");
		}
		offset = determineOffset();
	}
	
	public Point determineOffset() {
		Color decoOffsetColour = ColourBoneTranslator.getInstance().getDecoOffsetColour();
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				Color readColour = new Color(image.getRGB(x, y), true);
				if (readColour.equals(decoOffsetColour)) {
					return new Point(x, y);
				}
			}
		}
		throw new RuntimeException("Could not find decoration offset!");
	}
	
	public void draw(Graphics drawOnto, int originX, int originY) {
		drawOnto.drawImage(image, originX - offset.x, originY - offset.y, null);
	}
}
