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
package common.graphics.osseous;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import common.graphics.OffsetsScaler;
import common.graphics.osseous.path.ClearPath;
import common.graphics.osseous.path.MacroInfo;
import common.graphics.osseous.path.MacroPath;
import clientSide.graphics.ColourBoneTranslator;
import clientSide.graphics.ImageScaler;


public class FrameAndOffsets {
	private Map<Float, BufferedImage> frame;
	private Map<Float, Map<Integer, OffsetPoint>> offsets;
	private boolean mirrored;
	private boolean loaded;

	public FrameAndOffsets() {
	}

	public static String[] getInheritableProperties() {
		return new String[] {
			"redirectFrame", "mirrorHorizontally", "replaceOffsets"
		};
	}
	
	public FrameAndOffsets returnOrLoad(Bone parent, MacroInfo macroInfo) throws IOException, NotFoundException {
		if (!loaded) {
			load(parent, macroInfo);
			loaded = true;
		}
		return this;
	}

	public boolean loaded() {
		return loaded;
	}

	public void loaded(boolean value) {
		loaded = value;
	}

	public boolean mirrored() {
		return mirrored;
	}

	protected Properties loadPropertiesFile(String pathWithoutEnding) throws IOException {
		Properties properties = new Properties();
		File propertiesFile = new File(pathWithoutEnding+".properties");
		properties.load(new FileInputStream(propertiesFile));
		return properties;
	}

	/**
	 * Returns a copy of this where everything except for this.frame is copied.
	 * We want to copy as little as possible such that memory is saved, especially
	 * because of the many scaled instances we create. However, this is not always
	 * possible because we want to mirror horizontally and we want to replace
	 * offset ids. Therefore, we won't copy this.frame. If we need a horizontally
	 * mirrored image, we will handle it during drawing. Offsets however will be
	 * stored as mirrored.
	 */
	public FrameAndOffsets copyExceptForFrame(boolean loadedValue) {
		return copyExceptForFrame(new FrameAndOffsets(), loadedValue);
	}

	public FrameAndOffsets copyExceptForFrame(FrameAndOffsets copyTo, boolean loadedValue) {
		copyTo.frame = frame;
		copyTo.mirrored = mirrored;
		copyTo.loaded = loadedValue;
		Map<Float, Map<Integer, OffsetPoint>> copiedOffsets = new HashMap<Float, Map<Integer,OffsetPoint>>();
		for (Entry<Float, Map<Integer, OffsetPoint>> entry : offsets.entrySet()) {
			copiedOffsets.put(entry.getKey(), new HashMap<Integer, OffsetPoint>(entry.getValue()));
		}
		copyTo.offsets = copiedOffsets;
		return copyTo;
	}

	private void createFrameScaleFactor(float scaleFactor) {
		frame.put(
			scaleFactor, ImageScaler.scale(
				frame.get(1.0f), scaleFactor
			)
		);
	}
	private void createOffsetsScaleFactor(float scaleFactor) {
		offsets.put(
			scaleFactor, OffsetsScaler.scale(
				offsets.get(1.0f), scaleFactor
			)
		);
	}

	public BufferedImage image(float scaleFactor) {
		if (!frame.containsKey(scaleFactor)) {
			createFrameScaleFactor(scaleFactor);
		}
		return frame.get(scaleFactor);
	}

	public boolean hasOffsetID(int offsetID) {
		return offsets.get(1.0f).containsKey(offsetID);
	}

	public Map<Integer, OffsetPoint> offsets(float scaleFactor) {
		if (!offsets.containsKey(scaleFactor)) {
			createOffsetsScaleFactor(scaleFactor);
		}
		return offsets.get(scaleFactor);
	}

	/** mutating; Mirrors the offsets horizontally */
	public void mirrorOffsets() {
		this.mirrored = !this.mirrored;
		for (Float scale : offsets.keySet()) {
			Map<Integer, OffsetPoint> scaledOffsets = offsets.get(scale);
			int imageWidth = Math.round(scale * frame.get(1.0f).getWidth());
			for (Integer key : scaledOffsets.keySet()) {
				OffsetPoint originalPoint = scaledOffsets.get(key);
				OffsetPoint mirroredPoint = new OffsetPoint(
					imageWidth - originalPoint.x - 1, originalPoint.y
				);
				scaledOffsets.put(key, mirroredPoint);
			}
		}
	}

	/** mutating; Applies the list of offset ID replacements sequentially */
	public void replaceOffsets(List<OffsetReplacement> replacements) {
		for (Float scale : offsets.keySet()) {
			Map<Integer, OffsetPoint> scaledOffsets = offsets.get(scale);
			for (OffsetReplacement replacement : replacements) {
				if (replacement.oldId() != replacement.newId()) {
					if (replacement.swap()) {
						OffsetPoint oldPoint = scaledOffsets.get(replacement.oldId());
						OffsetPoint newPoint = scaledOffsets.get(replacement.newId());
						if (oldPoint != null) {
							scaledOffsets.put(replacement.newId(), oldPoint);
						} else {
							scaledOffsets.remove(replacement.newId());
						}
						if (newPoint != null) {
							scaledOffsets.put(replacement.oldId(), newPoint);
						} else {
							scaledOffsets.remove(replacement.oldId());
						}
					} else {
						OffsetPoint point = scaledOffsets.get(replacement.oldId());
						if (point != null) {
							scaledOffsets.put(replacement.newId(), point);
							scaledOffsets.remove(replacement.oldId());
						}
					}
				}
			}
		}
	}

	/**
	 * Loads this NewFrameAndOffsets instance, either by loading and
	 * creating it from hard disk data, or by copying from another
	 * instance. Its frame number should be stored in given macroInfo.
	 * @throws NotFoundException
	 */
	private void load(Bone parent, MacroInfo macroInfo) throws IOException, NotFoundException {
		this.frame = new HashMap<Float, BufferedImage>();
		this.offsets = new HashMap<Float, Map<Integer, OffsetPoint>>();
		Map<String, String> bequeathToFrames = parent.bequeathToFrames();
		String redirect = bequeathToFrames.get("redirectFrame");
		boolean shouldMirror = false;
		List<OffsetReplacement> replacementList = null;
		if (bequeathToFrames.containsKey("mirrorHorizontally")) {
			shouldMirror = Boolean.parseBoolean(bequeathToFrames.get("mirrorHorizontally"));
		}
		if (bequeathToFrames.containsKey("replaceOffsets")) {
			replacementList = parseReplaceOffsets(bequeathToFrames.get("replaceOffsets"));
		}

		// first, try to load redirection from properties file if existent:
		Properties properties = new Properties();
		
		if (macroInfo == null) {
			macroInfo = new MacroInfo(null, null);
		}
		String frameFilePathWithoutEnding;
		if (parent.isAnimation()) {
			frameFilePathWithoutEnding = parent.filePath() + "/image" + macroInfo.currentFrame();
		} else {
			frameFilePathWithoutEnding = parent.filePath() + "/image";
		}
		boolean couldRead = true;
		try {
			properties = loadPropertiesFile(frameFilePathWithoutEnding);
		} catch (IOException e) {
			couldRead = false;
		}
		if (couldRead) {
			redirect = properties.getProperty("redirectFrame");
			shouldMirror = Boolean.parseBoolean(properties.getProperty("mirrorHorizontally"));
			if (properties.containsKey("replaceOffsets")) {
				replacementList = parseReplaceOffsets(properties.getProperty("replaceOffsets"));
			}
		}
		if (redirect != null) {
			// else resolve from redirection and copy from there
			FrameAndOffsets resolved = resolveFrameAndOffset(
				parent, new MacroPath(redirect), macroInfo
			);
			resolved.copyExceptForFrame(this, false);
		} else {
			// else load frame and offset from real graphics files
			String fileEnding = parent.manager.getFileEnding();
			File frameFile = new File(frameFilePathWithoutEnding+fileEnding);
			File offsetFile = new File(frameFilePathWithoutEnding+".mask"+fileEnding);
			BufferedImage _frame = loadImage(frameFile);
			this.frame.put(1.0f, _frame);
			BufferedImage offsetImage = null;
			try {
				offsetImage = loadImage(offsetFile);
			} catch(IOException e) {
			}
			if (offsetImage != null) {
				Map<Integer, OffsetPoint> _offsets = ColourBoneTranslator.getInstance().readOffsets(
					offsetImage
				);
				this.offsets.put(1.0f, _offsets);
			} else {
				this.offsets.put(1.0f, new HashMap<Integer, OffsetPoint>());
			}
		}
		if (shouldMirror) {
			this.mirrorOffsets();
		}
		if (replacementList != null) {
			this.replaceOffsets(replacementList);
		}
	}

	/**
	 * Called by load() if redirectFrame property is set.
	 * Returns the NewFrameAndOffsets from given path redirecting to another frame.<br>
	 * Syntax of path is: collection1/collection2/.../bone/frameNumber<br>
	 * Does not create a copy. Calls NewBone.resolve() and optionally
	 * load() on NewOsseous instances.
	 * @throws NotFoundException
	 */
	protected static FrameAndOffsets resolveFrameAndOffset(
			Bone resolveFrom, MacroPath _redirect, MacroInfo macroInfo
	) throws IOException, NotFoundException {
		ClearPath path = _redirect.replaceMacros(resolveFrom, macroInfo);
		// split off the trailing frame number from path before resolving
		String frameIndexString = path.popLast();
		// resolve the bone containing the frame:
		Bone bone = (Bone) resolveFrom.resolve(path);
		int frameIndex;
		try {
			frameIndex = Integer.parseInt(frameIndexString);
		} catch(NumberFormatException e) {
			throw new NotFoundException("Invalid frame number: " + frameIndexString);
		}
		// now return the requested frame and offsets from that bone:
		return bone.frameAndOffsets(frameIndex);
	}

	private static BufferedImage loadImage(File imageFile) throws IOException {
		BufferedImage rawImage;
		try {
			rawImage = ImageIO.read(imageFile);
		} catch(IOException e) {
			System.out.println("Could not read "+imageFile.getCanonicalPath());
			throw e;
		}
		GraphicsConfiguration graphicsConfiguration = new JFrame().getGraphicsConfiguration();
		BufferedImage image = graphicsConfiguration.createCompatibleImage(
			rawImage.getWidth(), rawImage.getHeight(), Transparency.TRANSLUCENT
		);
		Graphics graphics = image.getGraphics();
		graphics.drawImage(rawImage, 0, 0, null);
		graphics.dispose();
		return image;
	}

	/**
	 * Draws this frame.
	 * @param scale the scale factor which should be used
	 * @param drawOnto the Graphics onto which this frame should be drawn
	 * @param position the coordinates of where the
	 * left upper corner of this frame should be drawn
	 */
	public void draw(float scale, Graphics drawOnto, Point position) {
		BufferedImage image = this.image(scale);
		if (!mirrored) {
			drawOnto.drawImage(image, position.x, position.y, null);
		} else {
			int width = image.getWidth();
			int height = image.getHeight();
			drawOnto.drawImage(image, position.x+width, position.y, -width, height, null);
		}
	}

	/**
	 * Draws this frame relative to one of its offsets.
	 * @param scale the scale factor which should be used
	 * @param drawOnto the Graphics onto which this frame should be drawn
	 * @param position the coordinates of where this frame should be drawn,
	 * relative to its {@code offsetID}
	 * @param offsetID the offset whose coordinates are denoted by parameter {@code position}
	 * @throws NoSuchOffsetException
	 */
	public void draw(float scale, Graphics drawOnto, Point position, int offsetID) {
		OffsetPoint offset = this.offsets(scale).get(offsetID);
		if (offset == null) {
			throw new NoSuchOffsetException("No such offset: "+offsetID);
		}
		Point leftUpperCorner = new OffsetPoint(position.x - offset.x, position.y - offset.y).roundToInt();
		draw(scale, drawOnto, leftUpperCorner);
	}

	/**
	 * Parses the replaceOffsets property and returns it as
	 * [x, y, z, ...] where x is the first executed substitution,
	 * y is the second etc.. A substitution is a pair (a, b) and
	 * means that offset ID "a" is replaced by ID "b".
	 */
	public static List<OffsetReplacement> parseReplaceOffsets(String property) {
		property = property.replace(" ", "");
		String[] strings = property.split(",");
		List<OffsetReplacement> result = new ArrayList<OffsetReplacement>();
		for (String currentPair : strings) {
			OffsetReplacement replacement;
			if (currentPair.contains("//")) {
				// swapping
				String[] split = currentPair.split("//");
				assert(split.length == 2);
				replacement = new OffsetReplacement(
					Integer.parseInt(split[0]), Integer.parseInt(split[1]), true
				);
			} else if (currentPair.contains("/")) {
				// replacement
				String[] split = currentPair.split("/");
				assert(split.length == 2);
				replacement = new OffsetReplacement(
					Integer.parseInt(split[0]), Integer.parseInt(split[1]), false
				);
			} else {
				throw new RuntimeException("Invalid replaceOffsets entry: " + currentPair);
			}
			result.add(replacement);
		}
		return result;
	}
}
