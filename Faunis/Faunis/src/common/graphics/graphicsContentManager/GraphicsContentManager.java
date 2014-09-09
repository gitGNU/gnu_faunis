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
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import clientSide.graphics.Decoration;
import clientSide.graphics.Drawable;
import clientSide.graphics.FloorGraphics;
import clientSide.graphics.GuiGraphics;
import clientSide.player.ClientPlayer;
import common.Settings;
import common.enums.CharacterClass;
import common.graphics.osseous.BoneCollection;
import common.graphics.osseous.NotFoundException;
import common.graphics.osseous.path.ClearPath;


public class GraphicsContentManager {
	protected Settings settings;
	protected PlayerGraphicsContentManager playerGraphicsContentManager;
	protected DecoGraphicsContentManager decoGraphicsContentManager;
	protected GuiGraphicsContentManager guiGraphicsContentManager;
	protected FloorGraphicsContentManager floorGraphicsContentManager;

	/** Don't forget to call loadResourcesForClient() / loadResourcesForServer()
	 * afterwards! */
	public GraphicsContentManager(Settings settings) {
		this.settings = settings;
	}

	public Settings settings() {
		return settings;
	}

	public PlayerGraphicsContentManager playerGraphicsContentManager() {
		return playerGraphicsContentManager;
	}

	public DecoGraphicsContentManager decoGraphicsContentManager() {
		return decoGraphicsContentManager;
	}

	public GuiGraphicsContentManager guiGraphicsContentManager() {
		return guiGraphicsContentManager;
	}

	public FloorGraphicsContentManager floorGraphicsContentManager() {
		return floorGraphicsContentManager;
	}

	/** Must be called by client after calling the content manager's constructor,
	 * will initialise all the fields that the client needs. */
	public void loadResourcesForClient() {
		this.playerGraphicsContentManager = new PlayerGraphicsContentManager(this);
		this.decoGraphicsContentManager = new DecoGraphicsContentManager(this);
		this.guiGraphicsContentManager = new GuiGraphicsContentManager(this);
		this.floorGraphicsContentManager = new FloorGraphicsContentManager(this);
	}

	/** Must be called by server after calling the content manager's constructor,
	 * will initialise all the fields that the server needs. */
	public void loadResourcesForServer() {
		this.playerGraphicsContentManager = new PlayerGraphicsContentManager(this);
	}

	public Set<String> getAvailableAnimations(
		CharacterClass type
	) throws IOException, NotFoundException {
		BoneCollection collection = (BoneCollection) playerGraphicsContentManager.resolve(
			new ClearPath(type.toString())
		);
		Set<String> result = collection.getElementNames();
		result.removeAll(settings.specialClassDirs());
		return result;
	}

	public Set<String> getAvailableMoods(CharacterClass type) {
		BoneCollection collection;
		try {
			collection = (BoneCollection) playerGraphicsContentManager.resolve(
				new ClearPath(type.toString(), "moods")
			);
		} catch(IOException e) {
			return new HashSet<String>();
		} catch(NotFoundException e) {
			return new HashSet<String>();
		}
		return collection.getElementNames();
	}

	public void draw(
		Drawable drawable, int x, int y, Graphics drawOnto
	) throws IOException, NotFoundException {
		if (drawable instanceof ClientPlayer) {
			playerGraphicsContentManager.draw((ClientPlayer) drawable, x, y, drawOnto);
		} else if (drawable instanceof Decoration) {
			decoGraphicsContentManager.draw((Decoration) drawable, x, y, drawOnto);
		} else if (drawable instanceof GuiGraphics) {
			guiGraphicsContentManager.draw((GuiGraphics) drawable, x, y, drawOnto);
		} else if (drawable instanceof FloorGraphics) {
			floorGraphicsContentManager.draw((FloorGraphics) drawable, x, y, drawOnto);
		} else {
			throw new RuntimeException(
				"I don't know how to draw unknown type "+drawable.getClass()+"!"
			);
		}
	}
}
