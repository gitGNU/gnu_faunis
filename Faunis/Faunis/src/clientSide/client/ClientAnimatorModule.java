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

import clientSide.animation.AnimationData;
import clientSide.animation.Animator;
import clientSide.player.PlayerGraphics;
import common.enums.AniEndType;
import common.modules.timerModule.AnimatorModule;

public class ClientAnimatorModule extends AnimatorModule<PlayerGraphics, Client, String> {
	
	public ClientAnimatorModule(Map<PlayerGraphics, Animator<PlayerGraphics>> map) {
		super(map);
	}

	@Override
	public void unregistered(PlayerGraphics forTimeModifiable) {
		// Nothing to do here
	}

	@Override
	public void started(PlayerGraphics timeModifiable) {
		// Nothing to do here
	}

	@Override
	public Animator<PlayerGraphics> createCompleteModTimer(PlayerGraphics playerGraphics, String animation) {
		if (!parent.hasGraphicalOutput())
			return null;
		AnimationData animationData = parent.graphicsContentManager.getAnimationData(
				playerGraphics.getType(), animation);
		AniEndType endType = animationData.endType;
		int maxFrameIndex = animationData.numberOfFrames-1;
		// TODO: Get animation interval
		Animator<PlayerGraphics> animator = new Animator<PlayerGraphics>(this, playerGraphics, 100, endType, maxFrameIndex);
		return animator;
	}

}
