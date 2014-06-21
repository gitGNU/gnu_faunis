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
package common.graphics;

import java.io.FileNotFoundException;
import java.util.Properties;

import clientSide.graphics.Bone;

import common.Logger;
import common.archivist.GraphicsContentArchivist;

public class DecorationData {
	private GraphicsContentArchivist archivist;
	private String name;
	private Bone bone;
	private float scale = 1;
	private String decoGraphicsPath;
	
	public DecorationData(GraphicsContentArchivist archivist, String name, String decoGraphicsPath) {
		this.archivist = archivist;
		this.name = name;
		this.decoGraphicsPath = decoGraphicsPath;
	}
	
	
	public void loadBasicSettings() {
		Properties settings = archivist.loadBasicDecoSettings(name);
		if (settings != null) {
			this.scale = Float.parseFloat(settings.getProperty("scale", "1"));
		}
	}
	
	public float getScale() {
		return scale;
	}
	
	public Bone getBone() {
		return bone;
	}
	
	public void loadBone(GraphicsContentManager parent) {
		String decoPathWithoutEnding = decoGraphicsPath+name+"/"+name;
		try {
			Bone decorationBone = new Bone(parent, decoPathWithoutEnding);
			bone = decorationBone;
		} catch(FileNotFoundException e) {
			Logger.log("Could not load bone for decoration "+name);
		}
	}
}
