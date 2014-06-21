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
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import common.enums.BodyPart;
import common.graphics.GraphicsContentManager;
import common.graphics.ResolvedImageResult;



public class Bone {
	/** image file paths are currently only used for debugging */
	private ArrayList<String> imagePaths;
	private HashMap<Float, ArrayList<BufferedImage>> scaledImageLists;
	private HashMap<Float, ArrayList<HashMap<BodyPart, Point>>> scaledConnectionOffsets;
	private static ColourBoneTranslator translator = ColourBoneTranslator.getInstance();
	private boolean isAnimation;
	private final GraphicsContentManager parent;
	
	public Bone(GraphicsContentManager parent, String filePathWithoutEnding) throws FileNotFoundException {
		this.parent = parent;
		imagePaths = new ArrayList<String>();
		scaledImageLists = new HashMap<Float, ArrayList<BufferedImage>>();
		scaledImageLists.put(new Float(1), new ArrayList<BufferedImage>());
		readFrame(filePathWithoutEnding);
		isAnimation = false;
		scaledConnectionOffsets = new HashMap<Float, ArrayList<HashMap<BodyPart, Point>>>();
		readConnectionOffsets();
	}
	public Bone(GraphicsContentManager parent, String animationPrefix, int maxFrameIndex) throws FileNotFoundException {
		this.parent = parent;
		imagePaths = new ArrayList<String>();
		scaledImageLists = new HashMap<Float, ArrayList<BufferedImage>>();
		scaledImageLists.put(new Float(1), new ArrayList<BufferedImage>());
		for (int frameIndex = 0; frameIndex <= maxFrameIndex; frameIndex++) {
			String filePathWithoutEnding = animationPrefix+frameIndex;
			readFrame(filePathWithoutEnding);
		}
		isAnimation = true;
		scaledConnectionOffsets = new HashMap<Float, ArrayList<HashMap<BodyPart, Point>>>();
		readConnectionOffsets();
	}
	
	public boolean isAnimation() {
		return isAnimation;
	}
	
	private void readFrame(String filePathWithoutEnding) throws FileNotFoundException {
		ResolvedImageResult result = parent.getArchivist().resolveImage(filePathWithoutEnding);
		imagePaths.add(result.getPath());
		ArrayList<BufferedImage> unscaledImageLists = scaledImageLists.get(1.0f);
		unscaledImageLists.add(result.getImage());
	}
	

	
	private void readConnectionOffsets() {
		ArrayList<HashMap<BodyPart, Point>> originalConnectionOffsets = new ArrayList<HashMap<BodyPart, Point>>();
		ArrayList<BufferedImage> originalImages = scaledImageLists.get(1.0f);
		for (int frameIndex = 0; frameIndex < originalImages.size(); frameIndex++) {
			BufferedImage image = originalImages.get(frameIndex);
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
			originalConnectionOffsets.add(offsets);
		}
		scaledConnectionOffsets.put(new Float(1), originalConnectionOffsets);
	}
	
	public Point getConnectionOffset(BodyPart part, int frameIndex, float scale) {
		if (scale != 1.0f && !scaledConnectionOffsets.containsKey(scale))
			createConnectionOffsetScale(scale);
		HashMap<BodyPart, Point> x = null;
		try {
			x = scaledConnectionOffsets.get(scale).get(frameIndex);
		} catch(IndexOutOfBoundsException e) {
			return null;
		}
		if (x == null)
			return null;
		if (!x.containsKey(part)) {
			System.err.println("Bone: Could not get connection offset of frameIndex "+frameIndex+", BodyPart "+part+"!");
			System.err.println("Bone image paths are: "+imagePaths);
			throw new RuntimeException();
		}
		return scaledConnectionOffsets.get(scale).get(frameIndex).get(part);
	}
	
	private void createConnectionOffsetScale(float scale) {
		assert(scale != 1.0f);
		assert(!scaledConnectionOffsets.containsKey(scale));
		ArrayList<HashMap<BodyPart, Point>> unscaledConnectionOffsets = scaledConnectionOffsets.get(1.0f);
		ArrayList<HashMap<BodyPart, Point>> connectionOffsets = new ArrayList<HashMap<BodyPart,Point>>();
		scaledConnectionOffsets.put(scale, connectionOffsets);

		for (int frameIndex = 0; frameIndex < countFrames(); frameIndex++) {
			HashMap<BodyPart, Point> unscaledBodyPartMap = unscaledConnectionOffsets.get(frameIndex);
			HashMap<BodyPart, Point> scaledBodyPartMap = new HashMap<BodyPart, Point>();
			connectionOffsets.add(scaledBodyPartMap);
			for (BodyPart bodyPart : unscaledBodyPartMap.keySet()) {
				Point unscaledPoint = unscaledBodyPartMap.get(bodyPart);
				Point scaledPoint = new Point(Math.round(unscaledPoint.x*scale),
											  Math.round(unscaledPoint.y*scale));
				scaledBodyPartMap.put(bodyPart, scaledPoint);
			}
		}
	}
	
	private void createImageScale(float scale) {
		assert(scale != 1.0f);
		assert(!scaledImageLists.containsKey(scale));
		ArrayList<BufferedImage> unscaledImageLists = scaledImageLists.get(1.0f);
		ArrayList<BufferedImage> newImageLists = new ArrayList<BufferedImage>();
		scaledImageLists.put(scale, newImageLists);
		
		for (BufferedImage unscaledImage : unscaledImageLists) {
			BufferedImage scaledImage = ImageScaler.downscale(unscaledImage, scale);
			newImageLists.add(scaledImage);
		}
	}
	
	public int countFrames() {
		return scaledImageLists.get(1.0f).size();
	}
	
	public Set<BodyPart> getConnectionOffsets(int frameIndex, float scale) {
		return scaledConnectionOffsets.get(scale).get(frameIndex).keySet();
	}
	
	public BufferedImage getImage(int frameIndex, float scale) {
		if (!scaledImageLists.containsKey(scale))
			createImageScale(scale);
		return scaledImageLists.get(scale).get(frameIndex);
	}
	
	public void draw(Graphics drawOnto, int x, int y, int frameIndex, float scale) {
		BufferedImage toDraw = getImage(frameIndex, scale);
		drawOnto.drawImage(toDraw, x, y, null);
	}
	public void draw(Graphics drawOnto, int x, int y, float scale) {
		draw(drawOnto, x, y, 0, scale);
	}
}
