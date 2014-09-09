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

import java.util.List;
import java.util.Map.Entry;

import clientSide.graphics.Decoration;
import clientSide.player.ClientPlayer;
import common.Link;
import common.Logger;
import common.MapInfo;
import common.butlerToClientOrders.*;
import common.enums.ClientStatus;
import common.graphics.GraphicalDecoStatus;
import common.graphics.PlayerData;


public class ClientReceiver {
	private Client parent;

	@SuppressWarnings("hiding")
	public void init(Client parent) {
		this.parent = parent;
	}

	public void handleMessage(BCOrder order) {
		assert(order != null);
		if (order instanceof BCAddCharOrder) {
			addChar((BCAddCharOrder) order);
		} else if (order instanceof BCChangeCharOrder) {
			changeChar((BCChangeCharOrder) order);
		} else if (order instanceof BCRemoveCharOrder) {
			removeChar((BCRemoveCharOrder) order);
		} else if (order instanceof BCSetMapOrder) {
			setMap((BCSetMapOrder) order);
		} else if (order instanceof BCSetClientStatusOrder) {
			setClientStatusOrder((BCSetClientStatusOrder) order);
		} else if (order instanceof BCErrorMessageOrder) {
			parent.logErrorMessage((BCErrorMessageOrder) order);
		} else if (order instanceof BCSystemMessageOrder) {
			parent.logSystemMessage((BCSystemMessageOrder) order);
		} else if (order instanceof BCChatMessageOrder) {
			parent.showChatMessage((BCChatMessageOrder) order);
		} else if (order instanceof BCOwnPlayersInfoOrder) {
			List<String> playerNames = ((BCOwnPlayersInfoOrder) order).getPlayerNames();
			parent.logSystemMessage("Your players are: "+playerNames);
		}

		else if(order instanceof BCSendInventoryOrder){
			parent.showInventory((BCSendInventoryOrder) order);
		}

		else {
			Logger.log("Received unknown server order!");
		}
		// TODO: Implement the handling of further server orders
	}



	public void addChar(BCAddCharOrder order) {
		PlayerData status = order.getGraphStatus();
		ClientPlayer player = new ClientPlayer(status, parent, parent);
		String playerName = order.getPlayerName();
		parent.playerModule.add(playerName, player, null);
	}

	public void removeChar(BCRemoveCharOrder order) {
		String playerName = order.getPlayerName();
		parent.playerModule.remove(playerName, null);
	}

	public void changeChar(BCChangeCharOrder order) {
		PlayerData status = order.getGraphStatus();
		ClientPlayer newPlayer = new ClientPlayer(status, parent, parent);
		String playerName = order.getPlayerName();
		parent.playerModule.removeAndAdd(playerName, newPlayer, null , null);
	}



	/** unloads map if possible */
	void unloadMap() {
		parent.moverModule.tryStopAll();
		parent.currentPlayers.clear();
		parent.activePlayerName = null;
		parent.currentMap = null;
		parent.zOrderedDrawables.clear();
	}

	/**
	 * A new map will be loaded: Remove all movements and
	 * players and register them anew from the MapInfo
	 */
	public void setMap(final BCSetMapOrder order) {
		final MapInfo mapInfo = order.getMapInfo();
		unloadMap();
		// everything is unloaded, now load:
		parent.currentMap = mapInfo.map;
		assert(parent.currentMap != null);
		parent.activePlayerName = order.getActivePlayerName();
		// load decorations
		if (parent.currentMap.getDecoInfos() != null) {
			for (GraphicalDecoStatus decoInfo : parent.currentMap.getDecoInfos()) {
				Decoration decoration = new Decoration(parent, parent, decoInfo);
				parent.zOrderedDrawables.add(decoration, decoration.getZOrder());
			}
		}
		// create link decorations
		if (parent.currentMap.getLinks() != null) {
			for (Link link : parent.currentMap.getLinks()) {
				GraphicalDecoStatus decoInfo = new GraphicalDecoStatus();
				decoInfo.name = "link";
				decoInfo.x = link.getSourceX();
				decoInfo.y = link.getSourceY();
				Decoration decoration = new Decoration(parent, parent, decoInfo);
				parent.zOrderedDrawables.add(decoration, decoration.getZOrder());
			}
		}
		assert(mapInfo.players != null);
		for (Entry<String, PlayerData> entry : mapInfo.players.entrySet()) {
			String playerName = entry.getKey();
			PlayerData status = entry.getValue();
			ClientPlayer player = new ClientPlayer(status, parent, parent);
			parent.playerModule.add(playerName, player, null);
		}
	}

	public void setClientStatusOrder(BCSetClientStatusOrder order) {
		ClientStatus newStatus = order.getNewStatus();
		if (newStatus == ClientStatus.disconnected) {
			parent.senderPart.disconnect();
			return;
		}
		ClientStatus clientStatus = parent.getClientStatus();
		if ((  clientStatus == ClientStatus.exploring
			|| clientStatus == ClientStatus.fighting)
			&& newStatus != ClientStatus.exploring
			&& newStatus != ClientStatus.fighting)
		{
			unloadMap();
		}
		parent.setClientStatus(newStatus);
	}
}
