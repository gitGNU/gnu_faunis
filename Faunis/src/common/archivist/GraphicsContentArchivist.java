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
package common.archivist;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;

import clientSide.animation.AnimationData;

import common.Logger;
import common.archivist.fileSystemArchivist.DirectoryFilter;
import common.enums.AniCompoType;
import common.enums.AniEndType;
import common.enums.CharacterClass;
import common.graphics.ResolvedImageResult;

public class GraphicsContentArchivist {
	private final String playerGraphicsPath;
	private final String decoGraphicsPath;
	private final String graphicsPath;
	private final String imageEnding;
	public static final FileFilter directoryFilter = new DirectoryFilter();
	
	public GraphicsContentArchivist(String playerGraphicsPath, String decoGraphicsPath, String graphicsPath,
									String imageEnding) {
		this.playerGraphicsPath = playerGraphicsPath;
		this.decoGraphicsPath = decoGraphicsPath;
		this.graphicsPath = graphicsPath;
		this.imageEnding = imageEnding;
	}

	public ResolvedImageResult resolveImage(String filePathWithoutEnding) throws FileNotFoundException {
		String path = filePathWithoutEnding+imageEnding;
		File graphicFile = new File(path);
		if (!graphicFile.exists()) {
			String alternatePath = filePathWithoutEnding+".properties";
			File linkFile = new File(alternatePath);
			if (!linkFile.exists())
				throw new FileNotFoundException();
			Properties properties = new Properties();
			FileInputStream inputStream = null;
			try {
				inputStream = new FileInputStream(linkFile);
				properties.load(inputStream);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
				}
			}
			filePathWithoutEnding = properties.getProperty("source");
			// read path should NOT start with a "/" and be
			// relative to the graphics directory. It should
			// NOT end with the file ending.
			
			if (filePathWithoutEnding == null)
				throw new FileNotFoundException();
			filePathWithoutEnding = graphicsPath + filePathWithoutEnding;
			return resolveImage(filePathWithoutEnding);
		} else {
			try {
				BufferedImage image = ImageIO.read(graphicFile);
				return new ResolvedImageResult(image, path);
			} catch(IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public File[] listDecorationDirs() {
		File decorationDirectory = new File(decoGraphicsPath);
		Logger.log(decoGraphicsPath);
		return decorationDirectory.listFiles(DirectoryFilter.getInstance());
	}
	
	public Properties loadBasicSettings(CharacterClass type) {
		File settingsFile = new File(playerGraphicsPath+type.toString()+"/settings.properties");
		Properties graphicsProperties = new Properties();
		FileInputStream settingsStream = null;
		try {
			settingsStream = new FileInputStream(settingsFile);
			graphicsProperties.load(settingsStream);
		} catch (FileNotFoundException e) {
			System.err.println("Settings file for graphics "+type+" not found!");
			return null;
		} catch(IOException e) {
			e.printStackTrace();
			System.err.println("IOException while reading settings for graphics "+type+"!");
			return null;
		} finally {
			if (settingsStream != null) {
				try {
					settingsStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return graphicsProperties;
	}
	
	public Properties loadBasicDecoSettings(String decoration) {
		File settingsFile = new File(decoGraphicsPath+decoration+"/settings.properties");
		Properties decoProperties = new Properties();
		FileInputStream settingsStream = null;
		try {
			settingsStream = new FileInputStream(settingsFile);
			decoProperties.load(settingsStream);
		} catch (FileNotFoundException e) {
			System.err.println("Settings file for decoration "+decoration+" not found!");
			return null;
		} catch(IOException e) {
			e.printStackTrace();
			System.err.println("IOException while reading settings for decoration "+decoration+"!");
			return null;
		} finally {
			if (settingsStream != null) {
				try {
					settingsStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return decoProperties;
	}
	
	public Map<String, AnimationData> loadAvailableAnimations(CharacterClass type, AniCompoType compositionType,
															  boolean countFrames) {
		HashMap<String, AnimationData> result = new HashMap<String, AnimationData>();
		File typeDirectory = new File(playerGraphicsPath+type.toString());
		if (!typeDirectory.exists()) {
			Logger.log("Graphics path for character class "+type+" doesn't seem to exist.");
			return result;
		}
		File[] subdirs = typeDirectory.listFiles(directoryFilter);
		if (subdirs != null) {
			for (File subdir : subdirs) {
				String animation = subdir.getName();
				if (!animation.equals("faces")) {
					int numFrames = 0;
					if (countFrames) {
						String prefix;
						if (compositionType == AniCompoType.LIMBED)
							prefix = playerGraphicsPath+type+"/"
									+animation+"/body/down";
						else
							prefix = playerGraphicsPath+type+"/"
									+animation+"/down";
						numFrames = countAvailableFrames(prefix);
						Logger.log("Animation "+animation+" of "+type+" has "+numFrames+" frames.");
					}

					// Read further animation settings
					File animationSettingsFile = new File(subdir.getPath()+"/settings.properties");
					Properties animationProperties = new Properties();
					FileInputStream animationSettingsStream;
					try {
						animationSettingsStream = new FileInputStream(animationSettingsFile);
						animationProperties.load(animationSettingsStream);
						animationSettingsStream.close();
					} catch (FileNotFoundException e) {
						System.err.println("Settings file for animation "+animation+" not found!");
						continue;
					} catch(IOException e) {
						e.printStackTrace();
						System.err.println("IOException while reading settings for "+animation+"!");
						continue;
					}
					AniEndType endType = AniEndType.valueOf(animationProperties.getProperty("endType"));
					long millisecsPerFrame = Long.parseLong(animationProperties.getProperty("millisecsPerFrame"));
					AnimationData animationData = new AnimationData(numFrames, endType, millisecsPerFrame);
					result.put(animation, animationData);
				}
			}
		}
		return result;
	}
	
	public List<String> loadAvailableFaces(CharacterClass type) {
		List<String> result = new ArrayList<String>();
		File faceDirectory = new File(playerGraphicsPath+type.toString()+"/faces");
		File[] subdirs = faceDirectory.listFiles(DirectoryFilter.getInstance());
		if (subdirs == null)
			return result;
		for (File subdir : subdirs) {
			String subdirName = subdir.getName();
			result.add(subdirName);
		}
		return result;
	}

	public AniCompoType loadCompositionType(CharacterClass type) {
		File bodyDirectory = new File(playerGraphicsPath+type.toString()+"/stand/body");
		if (bodyDirectory.isDirectory() && bodyDirectory.exists()) {
			Logger.log(type+" has limbed graphics.");
			return AniCompoType.LIMBED;
		} else {
			Logger.log(type+" has compact graphics.");
			return AniCompoType.COMPACT;
		}
	}
	
	/** Detects if there are multiple numbered picture files (returns their number)
	 * or only a single un-numbered file (returns 0). If neither is found,
	 * returns -1. */
	public int countAvailableFrames(String prefixString) {
		File noAnim = new File(prefixString+imageEnding);
		if (noAnim.exists()) return 0;
		int counter = 0;
		while (new File(prefixString+counter+imageEnding).exists()
			   || new File(prefixString+counter+".properties").exists()) {
			counter++;
		}
		if (counter == 0) {
			Logger.log("WARNING: Couldn't find neither single picture nor animation frames!");
			return -1;
		}
		return counter;
	}
}
