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
package clientSide.client;

import java.util.Map;

import clientSide.player.PlayerGraphics;
import common.modules.objectModule.ObjectModule;

public class ClientPlayerModule extends ObjectModule<String, PlayerGraphics, Client, Void, Void> {
	
	public ClientPlayerModule(Map<String, PlayerGraphics> map) {
		super(map);
	}

	@Override
	public void added(String name, PlayerGraphics playerGraphics, Void unused) {
		assert(! parent.zOrderedDrawables.contains(playerGraphics,
                playerGraphics.getZOrder()));
		parent.zOrderedDrawables.add(playerGraphics, playerGraphics.getZOrder());
		// If the new playerGraphics is moving, start movement:
		if (playerGraphics.hasPath()) {
			parent.moverModule.tryStart(playerGraphics, null);
		} else if (playerGraphics.hasAnimation()) {
			parent.animatorModule.tryStart(playerGraphics, playerGraphics.getAnimation());
		}
	}

	@Override
	public void beforeRemove(String name, PlayerGraphics playerGraphics, Void unused) {
		// Stop possible earlier animation and movement:
		parent.moverModule.tryStop(playerGraphics);
		parent.animatorModule.tryStop(playerGraphics);
		assert(parent.zOrderedDrawables.contains(playerGraphics,
                playerGraphics.getZOrder()));
		parent.zOrderedDrawables.remove(playerGraphics, playerGraphics.getZOrder());
	}

	@Override
	public Object[] getSynchroStuffForModification() {
		return new Object[] {parent.currentPlayerGraphics,
							 parent.movingPlayerGraphics,
							 parent.animatedPlayerGraphics,
							 parent.zOrderedDrawables};
	}

}
