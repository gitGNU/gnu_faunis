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
package common.graphics;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import clientSide.animation.AnimationData;
import clientSide.graphics.Bone;

import common.Logger;
import common.archivist.GraphicsContentArchivist;
import common.enums.AniCompoType;
import common.enums.BodyPart;
import common.enums.CharacterClass;
import common.enums.Direction;

public class GraphicsClassData {
	public final GraphicsContentArchivist archivist;
	public final CharacterClass type;
	public final String playerGraphicsPath;
	public final String imageEnding;
	public final HashMap<String, // animation
	HashMap<BodyPart,
	HashMap<Direction,
	Bone>>> bones;
	public final HashMap<String, AnimationData> availableAnimationsAndData;
	public final List<String> availableFaces;
	public AniCompoType compositionType;
	public float scale = 1;
	
	public GraphicsClassData(GraphicsContentArchivist archivist, CharacterClass type, String playerGraphicsPath,
							 String fileEnding) {
		this.archivist = archivist;
		this.type = type;
		this.playerGraphicsPath = playerGraphicsPath;
		this.imageEnding = fileEnding;
		this.bones = new HashMap<String, HashMap<BodyPart,HashMap<Direction,Bone>>>();
		this.availableAnimationsAndData = new HashMap<String, AnimationData>();
		this.availableFaces = new ArrayList<String>();
	}
	
	public Set<String> getAvailableAnimations() {
		return availableAnimationsAndData.keySet();
	}
	
	public AnimationData getAnimationData(String animation) {
		return availableAnimationsAndData.get(animation);
	}
	
	public List<String> getAvailableFaces() {
		return availableFaces;
	}
	
	public AniCompoType getCompositionType() {
		return compositionType;
	}
	
	public float getScale() {
		return scale;
	}
	
	public void loadAvailableAnimations(boolean countFrames) {
		availableAnimationsAndData.putAll(archivist.loadAvailableAnimations(type, compositionType, countFrames));
	}
	
	public void loadBasicSettings() {
		Properties settings = archivist.loadBasicSettings(type);
		if (settings != null) {
			this.scale = Float.parseFloat(settings.getProperty("scale", "1"));
		}
	}

	public void loadAvailableFaces() {
		availableFaces.addAll(archivist.loadAvailableFaces(type));
	}

	public void loadCompositionType() {
		compositionType = archivist.loadCompositionType(type);
	}
		
	public void loadBones(GraphicsContentManager parent) {
		AniCompoType compoType = compositionType;
		for (String animation : getAvailableAnimations()) {
			AnimationData animationData = getAnimationData(animation);
			int countFrames = animationData.numberOfFrames;
			String prefixString = playerGraphicsPath+type+"/"+animation+"/";
			for (BodyPart bodyPart : BodyPart.values()) {
				if (compoType == AniCompoType.COMPACT
						&& bodyPart != BodyPart.compact)
					// if this character class is compact, only allow iteration
					// over the "compact" BodyPart
					continue;
				if (compoType != AniCompoType.COMPACT
						&& bodyPart == BodyPart.compact)
					// if this character class is limbed, do not allow iteration
					// over the "compact" BodyPart
					continue;
				for (Direction direction : Direction.values()) {
					String prefixString2;
					if (bodyPart != BodyPart.compact)
						prefixString2 = prefixString+bodyPart+"/"+direction;
					else
						prefixString2 = prefixString+direction;

					if (countFrames == 0) {
						// load non-animation
						Logger.log("load "+prefixString2);
						try {
							Bone bone = new Bone(parent, prefixString2, imageEnding);
							addBone(animation, bodyPart, direction, bone);
						} catch (FileNotFoundException e) {
							Logger.log("Couldn't load "+prefixString2);
							continue;
						}
					} else if (countFrames > 0) {
						// load animation with multiple frames
						Logger.log("load "+prefixString2+"*"+imageEnding);
						try {
							Bone bone = new Bone(parent, prefixString2, imageEnding,
								countFrames-1);
							addBone(animation, bodyPart, direction, bone);
						} catch (FileNotFoundException e) {
							Logger.log("couldn't load "+prefixString2+"*");
							continue;
						}
					} else {
						Logger.log("WARNING: Animation "+animation+" of "+type+" hasn't any pictures!");
					}
				}
			}
		}
	}
	
	private void addBone(String animation, BodyPart part, Direction dir, Bone bone) {
		HashMap<BodyPart, HashMap<Direction, Bone>> map2 = bones.get(animation);
		if (map2 == null) {
			map2 = new HashMap<BodyPart, HashMap<Direction,Bone>>();
			bones.put(animation, map2);
		}
		HashMap<Direction, Bone> map3 = map2.get(part);
		if (map3 == null) {
			map3 = new HashMap<Direction, Bone>();
			map2.put(part, map3);
		}
		assert(!map3.containsKey(dir));
		map3.put(dir, bone);
	}
	
	public Bone getBone(String animation, BodyPart part, Direction dir) {
		HashMap<BodyPart, HashMap<Direction, Bone>> map2 = bones.get(animation);
		if (map2 == null) return null;
		HashMap<Direction, Bone> map3 = map2.get(part);
		if (map3 == null) return null;
		return map3.get(dir);
	}


}
