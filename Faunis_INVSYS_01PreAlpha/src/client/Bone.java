/* Copyright 2012 Simon Ley alias "skarute"
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.imageio.ImageIO;

import communication.enums.BodyPart;



public class Bone {
	private ArrayList<BufferedImage> images;
	private ArrayList<HashMap<BodyPart, Point>> connectionOffsets;
	private static ColourBoneTranslator translator = ColourBoneTranslator.getInstance();
	private boolean isAnimation;
	
	public Bone(String filePath) {
		images = new ArrayList<BufferedImage>();
		try {
			File graphicFile = new File(filePath);
			BufferedImage image = ImageIO.read(graphicFile);
			images.add(image);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Couldn't load picture!");
		}
		isAnimation = false;
		connectionOffsets = new ArrayList<HashMap<BodyPart, Point>>();
		readConnectionOffsets();
	}
	
	public Bone(String animationPrefix, String fileEnding, int maxFrameIndex) {
		images = new ArrayList<BufferedImage>();
		for (int frameIndex = 0; frameIndex <= maxFrameIndex; frameIndex++) {
			File frameFile = new File(animationPrefix+frameIndex+fileEnding);
			try {
				BufferedImage frame = ImageIO.read(frameFile);
				images.add(frame);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Couldn't load frame!");
			}
			// TODO
		}
		isAnimation = true;
		connectionOffsets = new ArrayList<HashMap<BodyPart, Point>>();
		readConnectionOffsets();
	}
	
	public boolean isAnimation() {
		return isAnimation;
	}
	
	public void readConnectionOffsets() {
		for (int frameIndex = 0; frameIndex < images.size(); frameIndex++) {
			BufferedImage image = images.get(frameIndex);
			HashMap<BodyPart, Point> offsets = new HashMap<BodyPart, Point>();

			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					Color readColour = new Color(image.getRGB(x, y), true);
					BodyPart part = translator.translate(readColour);
					if (part != null) {
						offsets.put(part, new Point(x, y));
					}
				}
			}
			connectionOffsets.add(offsets);
		}
	}
	
	public Point getConnectionOffset(BodyPart part, int frameIndex) {
		assert(connectionOffsets.get(frameIndex).containsKey(part));
		return connectionOffsets.get(frameIndex).get(part);
	}
	
	public Set<BodyPart> getConnectionOffsets(int frameIndex) {
		return connectionOffsets.get(frameIndex).keySet();
	}
	
	public BufferedImage getImage(int frameIndex) {
		return images.get(frameIndex);
	}
	
	public void draw(Graphics drawOnto, int x, int y, int frameIndex) {
		drawOnto.drawImage(this.images.get(frameIndex), x, y, null);
	}
}
