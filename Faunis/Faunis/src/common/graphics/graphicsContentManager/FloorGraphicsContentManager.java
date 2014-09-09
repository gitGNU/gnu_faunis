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
import java.awt.Point;
import java.io.IOException;

import common.graphics.osseous.Bone;
import common.graphics.osseous.FrameAndOffsets;
import common.graphics.osseous.NotFoundException;
import common.graphics.osseous.path.ClearPath;
import clientSide.ClientSettings;
import clientSide.graphics.FloorGraphics;


public class FloorGraphicsContentManager extends OsseousManager<FloorGraphics> {

	public FloorGraphicsContentManager(GraphicsContentManager parent) {
		super(parent);
	}

	@Override
	public String getGraphicsPath() {
		return ((ClientSettings) parent.settings).floorGraphicsPath();
	}

	@Override
	public void draw(
		FloorGraphics drawable, int x, int y, Graphics drawOnto
	) throws IOException, NotFoundException {
		Bone bone = (Bone) this.resolve(new ClearPath(drawable.name()));
		FrameAndOffsets frameAndOffsets = bone.frameAndOffsets(0);

		if (frameAndOffsets.hasOffsetID(0)) {
			frameAndOffsets.draw(
				bone.defaultScale(), drawOnto, new Point(x, y), 0
			);
		} else {
			frameAndOffsets.draw(
				bone.defaultScale(), drawOnto, new Point(x, y)
			);
		}
	}

}
