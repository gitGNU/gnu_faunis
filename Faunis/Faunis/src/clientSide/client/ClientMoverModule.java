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
import common.modules.timerModule.MoverModule;
import common.movement.Mover;
import common.movement.MovingTask;
import common.movement.SoftMovingTask;

public class ClientMoverModule extends MoverModule<ClientPlayer, Client, Void> {

	public ClientMoverModule(Map<ClientPlayer, Mover<ClientPlayer, Client>> map) {
		super(map);
	}

	@Override
	public void unregistered(ClientPlayer forPlayer) {
		parent.animatorModule.tryStop(forPlayer);
	}

	@Override
	public void started(ClientPlayer player) {
		parent.animatorModule.tryStart(player, "walk");
	}

	@Override
	public Mover<ClientPlayer, Client> createCompleteModTimer(ClientPlayer player, Void argument) {
		if (!player.hasPath() || player.getPath().isEmpty()) {
			return null;
		}
		int numDeltaLevels = parent.clientSettings.numberOfDeltaLevelStates();
		Mover<ClientPlayer, Client> mover = new Mover<ClientPlayer, Client>(this, player, 500/numDeltaLevels);//TODO: time unit
		MovingTask<ClientPlayer, Client> movingTask = new SoftMovingTask(mover, player);
		mover.setMovingTask(movingTask);
		return mover;
	}

}
