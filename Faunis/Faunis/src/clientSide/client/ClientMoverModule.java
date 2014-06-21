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

import clientSide.player.PlayerGraphics;
import common.modules.timerModule.MoverModule;
import common.movement.Mover;
import common.movement.MovingTask;
import common.movement.SoftMovingTask;

public class ClientMoverModule extends MoverModule<PlayerGraphics, Client, Void> {
	
	public ClientMoverModule(Map<PlayerGraphics, Mover<PlayerGraphics, Client>> map) {
		super(map);
	}

	@Override
	public void unregistered(PlayerGraphics forPlayerGraphics) {
		parent.animatorModule.tryStop(forPlayerGraphics);
	}

	@Override
	public void started(PlayerGraphics playerGraphics) {
		parent.animatorModule.tryStart(playerGraphics, "walk");
	}

	@Override
	public Mover<PlayerGraphics, Client> createCompleteModTimer(PlayerGraphics playerGraphics, Void argument) {
		if (!playerGraphics.hasPath() || playerGraphics.getPath().isEmpty())
			return null;
		int numDeltaLevels = parent.clientSettings.numberOfDeltaLevelStates();
		Mover<PlayerGraphics, Client> mover = new Mover<PlayerGraphics, Client>(this, playerGraphics, 500/numDeltaLevels);//TODO: time unit
		MovingTask<PlayerGraphics, Client> movingTask = new SoftMovingTask(mover, playerGraphics);
		mover.setMovingTask(movingTask);
		return mover;
	}

}
