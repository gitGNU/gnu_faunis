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
package communication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import client.Bone;
import client.Client;
import client.AnimationData;

import communication.enums.AniCompoType;
import communication.enums.AniEndType;
import communication.enums.BodyPart;
import communication.enums.CharacterClass;
import communication.enums.Direction;


public class GraphicsContentManager {
	private String graphicsPath;
	private String fileEnding;
	private HashMap<CharacterClass,
			HashMap<String, // animation
			HashMap<BodyPart,
			HashMap<Direction,
			Bone>>>> bones;
	private HashMap<Direction, List<BodyPart>> drawingOrders;
	private List<BodyPart> compactDrawingOrder;
	private HashMap<CharacterClass, HashMap<String, AnimationData>> availableAnimationsAndData;
	private HashMap<CharacterClass, List<String>> availableFaces;
	private HashMap<CharacterClass, AniCompoType> compositionTypes;
	
	/** Don't forget to call loadResourcesForClient() / loadResourcesForServer()
	 * afterwards! */
	public GraphicsContentManager(String graphicsPath, String fileEnding) {
		this.graphicsPath = graphicsPath;
		this.fileEnding = fileEnding;
	}
	
	/** Must be called by client after calling the content manager's constructor,
	 * will initialise all the fields that the client needs. */
	public void loadResourcesForClient() {
		compactDrawingOrder = new ArrayList<BodyPart>();
		compactDrawingOrder.add(BodyPart.compact);
		bones = new HashMap<CharacterClass, HashMap<String,HashMap<BodyPart,HashMap<Direction,Bone>>>>();
		drawingOrders = new HashMap<Direction, List<BodyPart>>();
		loadDrawingOrders();
		compositionTypes = new HashMap<CharacterClass, AniCompoType>();
		loadCompositionTypes();
		availableAnimationsAndData = new HashMap<CharacterClass, HashMap<String, AnimationData>>();
		loadAvailableAnimations(true);
		availableFaces = new HashMap<CharacterClass, List<String>>();
		loadAvailableFaces();
		loadImages();
	}

	/** Must be called by server after calling the content manager's constructor,
	 * will initialise all the fields that the server needs. */
	public void loadResourcesForServer() {
		availableAnimationsAndData = new HashMap<CharacterClass, HashMap<String, AnimationData>>();
		loadAvailableAnimations(false);	
		availableFaces = new HashMap<CharacterClass, List<String>>();
		loadAvailableFaces();
	}
	
	public HashMap<Direction, List<BodyPart>> getDrawingOrders() {
		return this.drawingOrders;
	}
	
	public List<BodyPart> getDrawingOrder(Direction direction) {
		return this.drawingOrders.get(direction);
	}
	
	public List<BodyPart> getCompactDrawingOrder() {
		return compactDrawingOrder;
	}
	
	public Set<String> getAvailableAnimations(CharacterClass type) {
		return availableAnimationsAndData.get(type).keySet();
	}
	
	public AnimationData getAnimationData(CharacterClass type, String animation) {
		return availableAnimationsAndData.get(type).get(animation);
	}
	
	public List<String> getAvailableFaces(CharacterClass type) {
		return availableFaces.get(type);
	}
	
	public AniCompoType getCompositionType(CharacterClass type) {
		return compositionTypes.get(type);
	}
	
	/** Determines which animations are available for which character class.
	 * If given flag countFrames is set, stores the number of frames available for
	 * an animation (Takes body/down as reference, though all limbs/directions must have
	 * the same number). <br/>
	 * Note that if frames should be counted, this method will require compositionTypes
	 * to be initialised. */
	private void loadAvailableAnimations(boolean countFrames) {
		for (CharacterClass type : CharacterClass.values()) {
			File typeDirectory = new File(graphicsPath+type.toString());
			File[] subdirs = typeDirectory.listFiles(Client.directoryFilter);
			HashMap<String, AnimationData> animationAndData = new HashMap<String, AnimationData>();
			if (subdirs != null) {
				for (File subdir : subdirs) {
					String animation = subdir.getName();
					if (!animation.equals("faces")) {
						int numFrames = 0;
						if (countFrames) {
							String prefix;
							if (compositionTypes.get(type) == AniCompoType.limbed)
								prefix = graphicsPath+type+"/"
													+animation+"/body/down";
							else
								prefix = graphicsPath+type+"/"
													+animation+"/down";
							numFrames = countAvailableFrames(prefix);
							System.out.println("Animation "+animation+" of "+type+" has "+numFrames+" frames.");
						}
						AniEndType endType = determineEndType(type, animation);
						AnimationData animationData = new AnimationData(numFrames, endType);
						animationAndData.put(animation, animationData);
					}
				}
			}
			availableAnimationsAndData.put(type, animationAndData);
		}
	}
	
	private void loadAvailableFaces() {
		for (CharacterClass type : CharacterClass.values()) {
			File faceDirectory = new File(graphicsPath+type.toString()+"/faces");
			File[] subdirs = faceDirectory.listFiles(Client.directoryFilter);
			if (subdirs == null) continue;
			List<String> faceList = new ArrayList<String>();
			for (File subdir : subdirs) {
				String subdirName = subdir.getName();
				faceList.add(subdirName);
			}
			availableFaces.put(type, faceList);
		}
	}
	
	private void loadCompositionTypes() {
		for (CharacterClass type : CharacterClass.values()) {
			File bodyDirectory = new File(graphicsPath+type.toString()+"/stand/body");
			if (bodyDirectory.isDirectory() && bodyDirectory.exists()) {
				System.out.println(type+" has limbed graphics.");
				compositionTypes.put(type, AniCompoType.limbed);
			} else {
				System.out.println(type+" has compact graphics.");
				compositionTypes.put(type, AniCompoType.compact);
			}
		}
	}
	
	/** determines in which order body parts have to be drawn */
	private void loadDrawingOrders() {
		List<BodyPart> leftList = Arrays.asList(
				  new BodyPart[] {BodyPart.rightArm,
				  BodyPart.rightLeg, BodyPart.body, BodyPart.head,
				  BodyPart.leftLeg, BodyPart.leftArm}
				);
		drawingOrders.put(Direction.left, leftList);
		
		List<BodyPart> rightList = Arrays.asList(
				  new BodyPart[] {BodyPart.leftArm,
				  BodyPart.leftLeg, BodyPart.body, BodyPart.head,
				  BodyPart.rightLeg, BodyPart.rightArm}
				);
		drawingOrders.put(Direction.right, rightList);
				
		List<BodyPart> downList = Arrays.asList(
				  new BodyPart[] {BodyPart.body,
				  BodyPart.leftLeg, BodyPart.rightLeg, BodyPart.leftArm,
				  BodyPart.rightArm, BodyPart.head}
				);
		drawingOrders.put(Direction.down, downList);
				
		List<BodyPart> upList = Arrays.asList(
				  new BodyPart[] {BodyPart.body,
				  BodyPart.leftLeg, BodyPart.rightLeg, BodyPart.leftArm,
				  BodyPart.rightArm, BodyPart.head}
				);
		drawingOrders.put(Direction.up, upList);
	}
	
	private void loadImages() {
		for (CharacterClass type : CharacterClass.values()) {
			AniCompoType compoType = compositionTypes.get(type);
			for (String animation : getAvailableAnimations(type)) {
				AnimationData animationData = getAnimationData(type, animation);
				int countFrames = animationData.numberOfFrames;
				String prefixString = graphicsPath+type+"/"+animation+"/";
				for (BodyPart part : BodyPart.values()) {
					if (compoType == AniCompoType.compact
							&& part != BodyPart.compact)
						continue;
					if (compoType != AniCompoType.compact
							&& part == BodyPart.compact)
						continue;
					for (Direction dir:Direction.values()) {
						String prefixString2;
						if (part != BodyPart.compact)
							prefixString2 = prefixString+part+"/"+dir;
						else
							prefixString2 = prefixString+dir;
						
						if (countFrames == 0) {
							// load non-animation
							String address = prefixString2+fileEnding;
							if (!new File(address).exists()) {
								System.out.println("couldn't load "+address);
								continue;
							}
							System.out.println("load "+address);
							Bone bone = new Bone(address);
							addBone(type, animation, part, dir, bone);
						} else if (countFrames > 0) {
							// load animation with multiple frames
							String toCheck = prefixString2+"0"+fileEnding;
							if (!new File(toCheck).exists()) {
								System.out.println("couldn't load "+toCheck);
								continue;
							}
							System.out.println("load "+prefixString2+"*"+fileEnding);
							Bone bone = new Bone(prefixString2, fileEnding,
									countFrames-1);
							addBone(type, animation, part, dir, bone);
						} else {
							System.out.println("WARNING: Animation "+animation+" of "+type+" hasn't any pictures!");
						}
					}
				}
			}
		}
	}
	
	/** Detects if there are multiple numbered picture files (returns their number)
	 * or only a single un-numbered file (returns 0). If neither is found,
	 * returns -1. */
	private int countAvailableFrames(String prefixString) {
		File noAnim = new File(prefixString+fileEnding);
		if (noAnim.exists()) return 0;
		int counter = 0;
		while (new File(prefixString+counter+fileEnding).exists()) {
			counter++;
		}
		if (counter == 0) {
			System.out.println("WARNING: Couldn't find neither single picture nor animation frames!");
			return -1;
		}
		return counter;
	}
	
	private AniEndType determineEndType(CharacterClass type, String animation) {
		File settingsFile = new File(graphicsPath+type+"/"+animation
										+"/aniSettings.txt");
		assert(settingsFile.exists());
		BufferedReader reader;
		String endTypeString;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(settingsFile)));
			endTypeString = reader.readLine();
			reader.close();
		} catch(IOException e) {
			System.out.println("Couldn't read settings for animation "+animation+" of "+type+"!");
			return null;
		}
		System.out.println("Animation "+animation+" of "+type+" should be of AniEndType \""+endTypeString+"\".");
		return AniEndType.valueOf(endTypeString);
	}
	
	private void addBone(CharacterClass type, String animation, BodyPart part, Direction dir, Bone bone) {
		HashMap<String, HashMap<BodyPart, HashMap<Direction, Bone>>> map = bones.get(type);
		if (map == null) {
			map = new HashMap<String, HashMap<BodyPart,HashMap<Direction,Bone>>>();
			bones.put(type, map);
		}
		HashMap<BodyPart, HashMap<Direction, Bone>> map2 = map.get(animation);
		if (map2 == null) {
			map2 = new HashMap<BodyPart, HashMap<Direction,Bone>>();
			map.put(animation, map2);
		}
		HashMap<Direction, Bone> map3 = map2.get(part);
		if (map3 == null) {
			map3 = new HashMap<Direction, Bone>();
			map2.put(part, map3);
		}
		assert(!map3.containsKey(dir));
		map3.put(dir, bone);
	}
	
	public Bone getBone(CharacterClass type, String animation, BodyPart part, Direction dir) {
		HashMap<String, HashMap<BodyPart, HashMap<Direction, Bone>>> map = bones.get(type);
		if (map == null) return null;
		HashMap<BodyPart, HashMap<Direction, Bone>> map2 = map.get(animation);
		if (map2 == null) return null;
		HashMap<Direction, Bone> map3 = map2.get(part);
		if (map3 == null) return null;
		return map3.get(dir);
	}
}
