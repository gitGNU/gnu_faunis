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
package clientSide.client;

import java.util.Map;

import clientSide.player.ClientPlayer;
import common.modules.objectModule.ObjectModule;

public class ClientPlayerModule extends ObjectModule<String, ClientPlayer, Client, Void, Void> {

	public ClientPlayerModule(Map<String, ClientPlayer> map) {
		super(map);
	}

	@Override
	public void added(String name, ClientPlayer player, Void unused) {
		assert(! parent.zOrderedDrawables.contains(player,
                player.getZOrder()));
		parent.zOrderedDrawables.add(player, player.getZOrder());
		// If the new player is moving, start movement:
		if (player.hasPath()) {
			parent.moverModule.tryStart(player, null);
		} else if (player.hasAnimation()) {
			parent.animatorModule.tryStart(player, player.getAnimation());
		}
	}

	@Override
	public void beforeRemove(String name, ClientPlayer player, Void unused) {
		// Stop possible earlier animation and movement:
		parent.moverModule.tryStop(player);
		parent.animatorModule.tryStop(player);
		boolean success = false;
		while (!success) {
			success = parent.zOrderedDrawables.remove(player, player.getZOrder());
		}
	}

}
