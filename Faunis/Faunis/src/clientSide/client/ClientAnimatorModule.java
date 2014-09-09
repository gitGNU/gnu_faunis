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

import java.io.IOException;
import java.util.Map;

import clientSide.animation.Animator;
import clientSide.player.ClientPlayer;
import common.enums.AniEndType;
import common.graphics.osseous.BoneCollection;
import common.graphics.osseous.NotFoundException;
import common.graphics.osseous.path.ClearPath;
import common.modules.timerModule.AnimatorModule;

public class ClientAnimatorModule extends AnimatorModule<ClientPlayer, Client, String> {

	public ClientAnimatorModule(Map<ClientPlayer, Animator<ClientPlayer>> map) {
		super(map);
	}

	@Override
	public void unregistered(ClientPlayer forTimeModifiable) {
		// Nothing to do here
	}

	@Override
	public void started(ClientPlayer timeModifiable) {
		// Nothing to do here
	}

	@Override
	public Animator<ClientPlayer> createCompleteModTimer(ClientPlayer player, String animation) {
		if (!parent.hasGraphicalOutput()) {
			return null;
		}
		BoneCollection collection;
		try {
			collection = (BoneCollection) parent.graphicsContentManager.playerGraphicsContentManager(
			).resolve(new ClearPath(player.getType().toString(), animation));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (NotFoundException e) {
			throw new RuntimeException(e);
		}

		AniEndType endType = AniEndType.valueOf(collection.getBequeathedProperty("endType"));
		int numberOfFrames = Integer.parseInt(collection.getBequeathedProperty("numberOfFrames"));
		int millisecsPerFrame = Integer.parseInt(collection.getBequeathedProperty("millisecsPerFrame"));
		Animator<ClientPlayer> animator = new Animator<ClientPlayer>(
			this, player, millisecsPerFrame, endType, numberOfFrames - 1
		);
		return animator;
	}

}
