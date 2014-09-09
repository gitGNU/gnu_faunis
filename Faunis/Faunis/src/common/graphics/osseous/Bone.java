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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import common.enums.AniEndType;
import common.graphics.graphicsContentManager.OsseousManager;
import common.graphics.osseous.path.ClearPath;
import common.graphics.osseous.path.MacroInfo;
import common.graphics.osseous.path.MacroPath;
import common.graphics.osseous.path.PathMacros;


public class Bone extends Osseous {
	private String runtimeRedirectBone;
	private List<FrameAndOffsets> framesAndOffsets;
	private Map<String, String> bequeathToFrames;
	private int numberOfFrames;
	private int millisecsPerFrame;
	private AniEndType endType;
	private float defaultScale;


	public Bone(OsseousManager<?> manager, BoneCollection parent, String name) {
		super(manager, parent, name);
	}

	public static String[] getInheritableProperties() {
		return new String[] {
			"runtimeRedirectBone", "redirectBone", "numberOfFrames", "endType", "millisecsPerFrame",
			"defaultScale",
			"redirectFrame", "mirrorHorizontally", "replaceOffsets"
		};
	}

	public boolean isAnimation() {
		return numberOfFrames != 0;
	}

	public FrameAndOffsets frameAndOffsets(int frameIndex) throws IOException, NotFoundException {
		return frameAndOffsets(new MacroInfo(frameIndex, null));
	}
	
	public FrameAndOffsets frameAndOffsets(
		MacroInfo macroInfo
	) throws IOException, NotFoundException {
		returnOrLoad(macroInfo);
		FrameAndOffsets result = framesAndOffsets.get(macroInfo.currentFrame());
		result.returnOrLoad(this, macroInfo);
		return result;
	}
	
	public Map<String, String> bequeathToFrames() {
		return bequeathToFrames;
	}
	
	public String runtimeRedirectBone() {
		return runtimeRedirectBone;
	}

	public float defaultScale() {
		return defaultScale;
	}

	public AniEndType endType() {
		return endType;
	}

	public int millisecsPerFrame() {
		return millisecsPerFrame;
	}

	/**
	 * NOTE: If numberOfFrames is 0, it does not mean that there are no frames, but
	 * that there is only one single frame which is not animated.
	 */
	public int numberOfFrames() {
		return numberOfFrames;
	}

	@Override
	protected Properties loadPropertiesFile() throws IOException {
		Properties properties = new Properties();
		File propertiesFile = new File(this.filePath() + "/bone.properties");
		properties.load(new FileInputStream(propertiesFile));
		return properties;
	}
	
	@Override
	public Osseous resolve(
			ClearPath namePath, MacroInfo macroInfo
	) throws IOException, NotFoundException {
		if (namePath.size() == 0) {
			// make sure this is loaded
			returnOrLoad(macroInfo);
			if (runtimeRedirectBone != null) {
				ClearPath resolvedPath = new MacroPath(
					runtimeRedirectBone
				).replaceMacros(this, macroInfo);
				Osseous runtimeRedirectedOsseous = resolve(resolvedPath, macroInfo);
				Bone runtimeRedirectedBone;
				try {
					runtimeRedirectedBone = (Bone) runtimeRedirectedOsseous;
				} catch(ClassCastException e) {
					throw new NotFoundException(
						"runtimeRedirectBone: " + runtimeRedirectedOsseous
						+ " is not a Bone!", e
					);
				}
				return runtimeRedirectedBone.resolve(namePath, macroInfo);
			}
			return this;
		} else {
			ClearPath remainingPath = namePath.copy();
			String currentName = remainingPath.popFirst();
			if (currentName.equals(PathMacros.up)) {
				return parent.resolve(remainingPath, null);
			} else {
				throw new NotFoundException(
					"Could not find '" + currentName + "' under '" + path()
					+ "' - this is a Bone, thus an endpoint!"
				);
			}
		}
	}

	/**
	 * Fills bequeathToFrames from properties file and from inherited properties.
	 * Applies inherited properties, properties from this bone's properties file,
	 * and properties from the bone that this bone is optionally redirected to,
	 * to this bone in the correct priority (that is 1. properties file,
	 * 2. inherited properties, 3. redirected properties). Any parameter may be
	 * null if it doesn't exist for this bone.
	 * Furthermore, adds the remaining properties from the properties file as meta
	 * properties to this bone.
	 */
	private void processProperties(
		Map<String, String> inheritedProperties, Properties propertiesFromFile,
		Bone redirectedBone
	) {
		// Process properties from redirected bone
		if (redirectedBone != null) {
			// Do not fill bequeathToFrames because those properties have already been
			// applied at the target
			// Apply main properties for this
			runtimeRedirectBone = redirectedBone.runtimeRedirectBone;
			numberOfFrames = redirectedBone.numberOfFrames;
			millisecsPerFrame = redirectedBone.millisecsPerFrame;
			endType = redirectedBone.endType;
			defaultScale = redirectedBone.defaultScale;
			// Fill meta properties
			meta.putAll(redirectedBone.meta);
		}

		// Process inherited properties
		if (inheritedProperties != null) {
			// Fill bequeathToFrames
			for (String bequeathable : FrameAndOffsets.getInheritableProperties()) {
				if (inheritedProperties.containsKey(bequeathable)) {
					bequeathToFrames.put(bequeathable, inheritedProperties.get(bequeathable));
				}
			}
			// Apply main properties to this
			String read;
			read = inheritedProperties.get("runtimeRedirectBone");
			if (read != null) {
				runtimeRedirectBone = read;
			}
			read = inheritedProperties.get("numberOfFrames");
			if (read != null) {
				numberOfFrames = Integer.parseInt(read);
			}
			read = inheritedProperties.get("millisecsPerFrame");
			if (read != null) {
				millisecsPerFrame = Integer.parseInt(read);
			}
			read = inheritedProperties.get("endType");
			if (read != null) {
				endType = AniEndType.valueOf(read);
			}
			read = inheritedProperties.get("defaultScale");
			if (read != null) {
				defaultScale = Float.parseFloat(read);
			}
			// Do not fill meta properties because those cannot be inherited
		}

		// Process properties from properties file
		if (propertiesFromFile != null) {
			// Fill bequeathToFrames
			for (String bequeathable : FrameAndOffsets.getInheritableProperties()) {
				if (propertiesFromFile.containsKey(bequeathable)) {
					bequeathToFrames.put(bequeathable, propertiesFromFile.getProperty(bequeathable));
				}
			}
			// Apply main properties to this
			String read;
			read = propertiesFromFile.getProperty("runtimeRedirectBone");
			if (read != null) {
				runtimeRedirectBone = read;
			}
			read = propertiesFromFile.getProperty("numberOfFrames");
			if (read != null) {
				numberOfFrames = Integer.parseInt(read);
			}
			read = propertiesFromFile.getProperty("millisecsPerFrame");
			if (read != null) {
				millisecsPerFrame = Integer.parseInt(read);
			}
			read = propertiesFromFile.getProperty("endType");
			if (read != null) {
				endType = AniEndType.valueOf(read);
			}
			read = propertiesFromFile.getProperty("defaultScale");
			if (read != null) {
				defaultScale = Float.parseFloat(read);
			}
			// Fill meta properties
			HashSet<String> metaPropertyKeys = new HashSet<String>();
			for (Object key : propertiesFromFile.keySet()) {
				metaPropertyKeys.add((String) key);
			}
			for (String key : Bone.getInheritableProperties()) {
				metaPropertyKeys.remove(key);
			}
			for (String key : FrameAndOffsets.getInheritableProperties()) {
				metaPropertyKeys.remove(key);
			}
			for (String metaKey : metaPropertyKeys) {
				String metaValue = propertiesFromFile.getProperty(metaKey);
				meta.put(metaKey, metaValue);
			}
		}
	}

	@Override
	protected void load(MacroInfo macroInfo) throws IOException, NotFoundException {
		Map<String, String> _bequeathToBones = null;
		if (parent != null) {
			_bequeathToBones = parent.bequeathToBones;
		}
		this.meta = new HashMap<String, String>();
		this.bequeathToFrames = new HashMap<String, String>();
		this.defaultScale = 1;
		this.framesAndOffsets = new ArrayList<FrameAndOffsets>();

		// load properties
		Properties propertiesFromFile;
		try {
			propertiesFromFile = loadPropertiesFile();
		} catch (FileNotFoundException e) {
			propertiesFromFile = null;
		}

		String read;
		String redirectBone = null;
		if (_bequeathToBones != null) {
			read = _bequeathToBones.get("redirectBone");
			if (read != null) {
				redirectBone = read;
			}
		}

		if (propertiesFromFile != null) {
			read = propertiesFromFile.getProperty("redirectBone");
			if (read != null) {
				redirectBone = read;
			}
		}

		if (redirectBone != null && !redirectBone.equals("")) {
			// Follow redirection and copy content from there
			ClearPath redirectPath = new MacroPath(redirectBone).replaceMacros(this, macroInfo);
			Osseous redirected = resolve(redirectPath, macroInfo);
			Bone redirectedBone = (Bone) redirected;
			processProperties(_bequeathToBones, propertiesFromFile, redirectedBone);
			int framesCount = Math.max(1, redirectedBone.numberOfFrames());
			for(int frameIndex = 0; frameIndex < framesCount; frameIndex++) {
				FrameAndOffsets frameAndOffsets = redirectedBone.frameAndOffsets(frameIndex);
				// copy frames, mark them as already loaded
				this.framesAndOffsets.add(frameAndOffsets.copyExceptForFrame(true));
			}
			// Since we won't call load() on them anymore and thus bequeathToFrames
			// would never be applied, apply it now:
			if (bequeathToFrames.containsKey("mirrorHorizontally")) {
				if (Boolean.parseBoolean(bequeathToFrames.get("mirrorHorizontally"))) {
					for (FrameAndOffsets frameAndOffsets : framesAndOffsets) {
						frameAndOffsets.mirrorOffsets();
					}
				}
			}
			if (bequeathToFrames.containsKey("replaceOffsets")) {
				List<OffsetReplacement> replacementList = FrameAndOffsets.parseReplaceOffsets(
					bequeathToFrames.get("replaceOffsets")
				);
				for (FrameAndOffsets frameAndOffsets : framesAndOffsets) {
					frameAndOffsets.replaceOffsets(replacementList);
				}
			}
		} else {
			processProperties(_bequeathToBones, propertiesFromFile, null);
			// else create some empty frames which will only be loaded when we access them:
			if (isAnimation()) {
				for (int frameIndex = 0; frameIndex < numberOfFrames; frameIndex++) {
					framesAndOffsets.add(new FrameAndOffsets());
				}
			} else {
				framesAndOffsets.add(new FrameAndOffsets());
			}
			// Do not apply bequeathToFrames because that will happen when they are loaded
		}
	}

}
