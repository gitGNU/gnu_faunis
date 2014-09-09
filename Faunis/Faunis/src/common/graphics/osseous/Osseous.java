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

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import common.graphics.graphicsContentManager.OsseousManager;
import common.graphics.osseous.path.ClearPath;
import common.graphics.osseous.path.MacroInfo;


public abstract class Osseous {
	protected final OsseousManager<?> manager;
	protected BoneCollection parent;
	protected String name;
	protected boolean loaded;
	/**
	 * meta should be created and filled in this.load() with all properties from the
	 * properties file that remain / have no special meaning defined in the devdoc.
	 */
	protected Map<String, String> meta;


	public Osseous(OsseousManager<?> manager, BoneCollection parent, String name) {
		if (manager == null) {
			throw new NullPointerException("Osseous manager must not be null!");
		}
		this.manager = manager;
		this.parent = parent;
		this.name = name;
		this.loaded = false;
	}

	
	public String name() {
		return name;
	}
	
	
	public BoneCollection parent() {
		return parent;
	}

	
	/** Returns this osseous' path as a string. */
	protected String path() {
		String result;
		if (parent == null) {
			result = "";
		} else {
			result = parent.path();
		}
		if (name != null && !name.equals("")) {
			result += "/" + name;
		}
		return result;
	}

	
	/** Returns the absolute file system path. */
	protected String filePath() {
		String result = manager.getGraphicsPath();
		if (result.endsWith("/")) {
			result = result.substring(0, result.length()-1);
		}
		result += path();
		return result;
	}

	
	public Osseous resolve(ClearPath namePath) throws IOException, NotFoundException {
		return resolve(namePath, null);
	}

	
	/**
	 * Resolves a given path.
	 * @param macroInfo May be null, will be used if paths with macros have
	 * to be resolved during resolution
	 */
	public abstract Osseous resolve(
			ClearPath namePath, MacroInfo macroInfo
	) throws IOException, NotFoundException;


	protected abstract Properties loadPropertiesFile() throws IOException;


	/** Needs this.load() to be called first. */
	public String getMeta(String key) {
		return meta.get(key);
	}


	/**
	 * @param macroInfo May be null 
	 */
	protected abstract void load(
		MacroInfo macroInfo
	) throws IOException, NotFoundException;


	/**
	 * @param macroInfo May be null 
	 */
	protected Osseous returnOrLoad(
		MacroInfo macroInfo
	) throws IOException, NotFoundException {
		if (!loaded) {
			load(macroInfo);
			loaded = true;
		}
		return this;
	}


	@Override
	public String toString() {
		return path();
	}
}
