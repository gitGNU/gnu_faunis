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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import common.graphics.graphicsContentManager.OsseousManager;
import common.graphics.osseous.path.ClearPath;
import common.graphics.osseous.path.MacroInfo;
import common.graphics.osseous.path.MacroPath;
import common.graphics.osseous.path.PathMacros;


public class BoneCollection extends Osseous {
	private String runtimeRedirectCollection;
	protected Map<String, Osseous> elements;
	/**
	 * bequeathToBones contains all properties that are passed down to its
	 * elements. Note that it contains all properties of parent.bequeathToBones.
	 */
	protected Map<String, String> bequeathToBones;
	private static FileFilter directoriesFilter;

	{
		directoriesFilter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
	}
	
	public static String[] propertiesForCollection() {
		return new String[] {"runtimeRedirectCollection", "redirectCollection"};
	}
	
	public String runtimeRedirectCollection() {
		return runtimeRedirectCollection;
	}


	public BoneCollection(
			OsseousManager<?> manager, BoneCollection parent, String name
	) {
		super(manager, parent, name);
	}

	/**
	 * Returns the names of this collection's children. Returned set is a copy.
	 * Requires this to be loaded.
	 */
	public Set<String> getElementNames() {
		return new HashSet<String> (elements.keySet());
	}

	/**
	 * Returns whether this collection contains a direct child of given name.
	 * Requires this to be loaded.
	 */
	public boolean contains(String _name) {
		return elements.containsKey(_name);
	}

	/** NOTE: File collection.properties is optional */
	@Override
	protected Properties loadPropertiesFile() throws IOException {
		Properties properties = new Properties();
		File propertiesFile = new File(this.filePath() + "/collection.properties");
		properties.load(new FileInputStream(propertiesFile));
		return properties;
	}

	/** this.load() must be called first. */
	public String getBequeathedProperty(String key) {
		return bequeathToBones.get(key);
	}
	

	@Override
	public Osseous resolve(
			ClearPath namePath, MacroInfo macroInfo
	) throws IOException, NotFoundException {
		if (namePath.size() == 0) {
			// make sure this is loaded
			returnOrLoad(macroInfo);
			if (runtimeRedirectCollection != null) {
				ClearPath resolvedPath = new MacroPath(
					runtimeRedirectCollection
				).replaceMacros(this, macroInfo);
				Osseous runtimeRedirectedOsseous = resolve(resolvedPath, macroInfo);
				BoneCollection runtimeRedirectedCollection;
				try {
					runtimeRedirectedCollection = (BoneCollection) runtimeRedirectedOsseous;
				} catch(ClassCastException e) {
					throw new NotFoundException(
						"runtimeRedirectCollection: " + runtimeRedirectedOsseous
						+ " is not a BoneCollection!", e
					);
				}
				return runtimeRedirectedCollection.resolve(namePath, macroInfo);
			}
			return this;
		} else {
			ClearPath remainingPath = namePath.copy();
			String currentName = remainingPath.popFirst();
			if (currentName.equals(PathMacros.up)) {
				return parent.resolve(remainingPath, null);
			} else {
				// make sure this is loaded
				returnOrLoad(macroInfo);
				if (runtimeRedirectCollection != null) {
					ClearPath resolvedPath = new MacroPath(
						runtimeRedirectCollection
					).replaceMacros(this, macroInfo);
					Osseous runtimeRedirectedOsseous = resolve(resolvedPath, macroInfo);
					BoneCollection runtimeRedirectedCollection;
					try {
						runtimeRedirectedCollection = (BoneCollection) runtimeRedirectedOsseous;
					} catch(ClassCastException e) {
						throw new NotFoundException(
							"runtimeRedirectCollection: " + runtimeRedirectedOsseous
							+ " is not a BoneCollection!", e
						);
					}
					return runtimeRedirectedCollection.resolve(namePath, macroInfo);
				}
				Osseous child = elements.get(currentName);
				if (child == null) {
					throw new NotFoundException(
						"Could not find '" + currentName + "' under '" + path()
						+ "', choices are: " + getElementNames()
					);
				}
				// NOTE: after loading, this.bequeathToBones is a superset of _bequeathToBones,
				// so bequeath that instead
				return child.resolve(remainingPath, macroInfo);
			}
		}
	}

	private void processProperties(
		Map<String, String> inheritedProperties, Properties propertiesFromFile,
		BoneCollection redirectedCollection
	) {
		// Process properties from redirected collection
		if (redirectedCollection != null) {
			// Do not fill bequeathToBones because those properties have already been
			// applied at the target
			// Apply main properties for this
			runtimeRedirectCollection = redirectedCollection.runtimeRedirectCollection;
			// Fill meta properties
			meta.putAll(redirectedCollection.meta);
		}

		// Process inherited properties
		if (inheritedProperties != null) {
			// Fill bequeathToBones
			for (String key : Bone.getInheritableProperties()) {
				if (inheritedProperties.containsKey(key)) {
					bequeathToBones.put(key, inheritedProperties.get(key));
				}
			}
			// Do not apply main properties for this because the parent can only be
			// another collection, thus it would not bequeath any collection-relevant
			// properties but would apply them on itself
			// Do not fill meta properties because those cannot be inherited
		}
		// Process properties from properties file
		if (propertiesFromFile != null) {
			// Fill bequeathToBones
			for (String key : Bone.getInheritableProperties()) {
				String value = propertiesFromFile.getProperty(key);
				if (value != null) {
					bequeathToBones.put(key, value);
				}
			}
			// Apply main properties to this
			String read;
			read = propertiesFromFile.getProperty("runtimeRedirectCollection");
			if (read != null) {
				runtimeRedirectCollection = read;
			}
			// Fill meta properties
			HashSet<String> metaPropertyKeys = new HashSet<String>();
			for (Object key : propertiesFromFile.keySet()) {
				metaPropertyKeys.add((String) key);
			}
			for (String key : BoneCollection.propertiesForCollection()) {
				metaPropertyKeys.remove(key);
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

	/** Loads this collection from its parent if it isn't. */
	public void assertLoad() throws IOException, NotFoundException {
		parent.resolve(new ClearPath(name));
	}

	@Override
	protected void load(
		MacroInfo macroInfo
	) throws IOException, NotFoundException {
		Map<String, String> _bequeathToBones = null;
		if (parent != null) {
			_bequeathToBones = parent.bequeathToBones;
		}
		this.elements = new HashMap<String, Osseous>();
		this.meta = new HashMap<String, String>();
		this.bequeathToBones = new HashMap<String, String>();

		// try to load this collection's properties file (optional)
		Properties propertiesFromFile;
		try {
			propertiesFromFile = loadPropertiesFile();
		} catch (FileNotFoundException e) {
			propertiesFromFile = null;
		}


		String redirectCollection = null;
		if (propertiesFromFile != null) {
			String read;
			read = propertiesFromFile.getProperty("redirectCollection");
			if (read != null) {
				redirectCollection = read;
			}
		}

		if (redirectCollection != null && !redirectCollection.equals("")) {
			// Follow redirection and copy content from there
			ClearPath redirectPath = new MacroPath(redirectCollection).replaceMacros(this, macroInfo);
			Osseous redirected = resolve(redirectPath, macroInfo);
			BoneCollection redirectedCollection = (BoneCollection) redirected;
			processProperties(_bequeathToBones, propertiesFromFile, redirectedCollection);
			RedirectedCollectionCreator creator = new RedirectedCollectionCreator(
				redirectPath, this, redirectedCollection
			);
			creator.create();
		} else {
			processProperties(_bequeathToBones, propertiesFromFile, null);
			File directory = new File(filePath());
			for (File subdirectory : directory.listFiles(BoneCollection.directoriesFilter)) {
				// check for bone.properties file inside subdirectory
				File bonePropertiesFile = new File(subdirectory, "bone.properties");
				if (bonePropertiesFile.exists() && bonePropertiesFile.isFile()) {
					Bone bone = new Bone(manager, this, subdirectory.getName());
					elements.put(subdirectory.getName(), bone);
				} else {
					// DEBUG
					String[] suspiciousNames = new String[]{"up", "left", "right", "down"};
					for (String suspiciousName : suspiciousNames) {
						if (subdirectory.getName().equals(suspiciousName)) {
							System.out.println(
								"WARNING: BoneCollection with typical Bone name "
								+ subdirectory.getName() + " created!"
							);
						}
					}
					// END OF DEBUG
					BoneCollection collection = new BoneCollection(
						manager, this, subdirectory.getName()
					);
					elements.put(subdirectory.getName(), collection);
				}
			}
		}


	}
}
