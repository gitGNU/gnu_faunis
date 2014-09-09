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
package serverSide.mapManager;

import java.util.Map;

import serverSide.butler.Butler;
import serverSide.butlerToMapmanOrders.BMRegisterOrder;
import serverSide.butlerToMapmanOrders.BMUnregisterOrder;
import serverSide.mapmanToButlerOrders.MBAddCharOrder;
import serverSide.mapmanToButlerOrders.MBChangeCharOrder;
import serverSide.mapmanToButlerOrders.MBMapInfoOrder;
import serverSide.mapmanToButlerOrders.MBOrder;
import serverSide.mapmanToButlerOrders.MBRemoveCharOrder;
import serverSide.player.ServerPlayer;
import common.MapInfo;
import common.enums.Mood;
import common.graphics.PlayerData;
import common.modules.objectModule.ObjectModule;

public class MapManagerPlayerModule extends ObjectModule<ServerPlayer, Butler, MapManager, BMRegisterOrder,
														 BMUnregisterOrder> {

	public MapManagerPlayerModule(Map<ServerPlayer, Butler> map) {
		super(map);
	}

	@Override
	public void added(ServerPlayer player, Butler butler, BMRegisterOrder order) {
		String playerName = player.getName();
		Butler sourceButler = order.getSource();
		assert(!player.hasPath());
		assert(!parent.playerNameToPlayer.containsKey(playerName));
		parent.playerNameToPlayer.put(playerName, player);
		if (order.getAddPlayerMapEntry()) {
			assert(player.getMapName() == null);
			player.setMapName(parent.getMapName());
		}
		// send map information to the new player's butler:
		MapInfo mapInfo = parent.getMapInfo();
		sourceButler.put(new MBMapInfoOrder(parent, mapInfo));
		// send information about the new player to all other butlers:
		notifyAllExcept(new MBAddCharOrder(parent, playerName,
				  		player.getPlayerData()), sourceButler);
	}

	@Override
	public void beforeRemove(ServerPlayer player, Butler butler, BMUnregisterOrder order) {
		parent.moverModule.tryStop(player);
		deleteAnimation(player);
		String playerName = player.getName();
		assert(parent.playerNameToPlayer.containsKey(playerName));
		parent.playerNameToPlayer.remove(playerName);
		if (order.getRemovePlayerMapEntry()) {
			assert(player.getMapName() != null);
			player.setMapName(null);
		}
		// inform every registered butler about the leave:
		parent.playerModule.notifyAll(new MBRemoveCharOrder(parent, playerName));
	}


	void notifyAll(MBOrder order) {
		for (Butler butler : parent.registeredPlayers.values()) {
			butler.put(order);
		}
	}
	void notifyAllExcept(MBOrder order, Butler exclude) {
		for (Butler butler : parent.registeredPlayers.values()) {
			if (butler != exclude) {
				butler.put(order);
			}
		}
	}

	void fireAnimation(ServerPlayer player, String animation) {
		PlayerData status = player.getPlayerData();
		status.currentAnimation = animation;
		notifyAll(new MBChangeCharOrder(parent, player.getName(), status));
	}

	void deleteAnimation(ServerPlayer player) {
		if (player.getCurrentAnimation() != null) {
			player.setCurrentAnimation(null);
			notifyAll(new MBChangeCharOrder(parent, player.getName(), player.getPlayerData()));
		}
	}

	void storeAnimation(ServerPlayer player, String animation) {
		player.setCurrentAnimation(animation);
		notifyAll(new MBChangeCharOrder(parent, player.getName(), player.getPlayerData()));
	}
	
	void setMood(ServerPlayer player, Mood mood) {
		player.setMood(mood);
		notifyAll(new MBChangeCharOrder(parent, player.getName(), player.getPlayerData()));
	}

}
