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
package common.graphics.graphicsContentManager;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import clientSide.graphics.Drawable;
import common.graphics.osseous.Bone;
import common.graphics.osseous.BoneCollection;
import common.graphics.osseous.Osseous;
import common.graphics.osseous.NotFoundException;
import common.graphics.osseous.path.ClearPath;
import common.graphics.osseous.path.MacroInfo;


public abstract class OsseousManager<T extends Drawable> {

	protected GraphicsContentManager parent;
	private BoneCollection root;


	public OsseousManager(GraphicsContentManager parent) {
		this.parent = parent;
		this.root = new BoneCollection(this, null, null);
	}

	public Osseous resolve(ClearPath namePath) throws IOException, NotFoundException {
		return root.resolve(namePath);
	}
	
	public Osseous resolve(ClearPath namePath, MacroInfo macroInfo) throws IOException, NotFoundException {
		return root.resolve(namePath, macroInfo);
	}

	public abstract String getGraphicsPath();

	public String getFileEnding() {
		return parent.settings.imageFileEnding();
	}

	public abstract void draw(
		T drawable, int x, int y, Graphics drawOnto
	) throws IOException, NotFoundException;
	
	/**
	 * An abbreviation to retrieve the image in its default scale under a given path
	 * @throws NotFoundException
	 * @throws IOException
	 */
	public BufferedImage image(ClearPath path) throws IOException, NotFoundException {
		Bone bone = (Bone) this.resolve(path);
		return bone.frameAndOffsets(0).image(bone.defaultScale());
	}
}
