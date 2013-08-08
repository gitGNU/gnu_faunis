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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map.Entry;

import clientSide.graphics.Decoration;
import clientSide.player.PlayerGraphics;

import common.Link;
import common.Logger;
import common.MapInfo;
import common.TerminationException;
import common.Tools;
import common.butlerToClientOrders.*;
import common.enums.ClientStatus;
import common.graphics.GraphicalDecoStatus;
import common.graphics.GraphicalPlayerStatus;
import common.modules.workerModule.WorkerModule;

public class ClientReceiver extends WorkerModule<BCOrder, Client> {
	ObjectInputStream input;
	
	public ClientReceiver() {
		super("client_serverThread", false);
	}
	
	@Override
	public void terminationLogic() {
		try {
			synchronized(parent.senderPart.output) {
			parent.socket.close();
			}
		} catch (IOException e) {
			Logger.log("Couldn't choke serverThread!");
		}
	}
	
	@Override
	protected BCOrder tryGetMessage() throws TerminationException {
		Object read = null;
		try {
			read = input.readObject();
		} catch (IOException e) {
			parent.logErrorMessage("Couldn't read anymore from server!");
			Logger.log("serverRunnable ends.");
			throw new TerminationException();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Logger.log("ClassNotFoundException!");
		}
		return (BCOrder) read;
	}
	
	@Override
	protected void notifyTermination() {
		parent.senderPart.disconnect();
	}
	
	@Override
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
	
	
	
	/** locks many things */
	public void addChar(BCAddCharOrder order) {
		GraphicalPlayerStatus status = order.getGraphStatus();
		PlayerGraphics playerGraphics = new PlayerGraphics(status, parent, parent);
		String playerName = order.getPlayerName();
		parent.playerModule.add(playerName, playerGraphics, null);
	}
	
	/** locks many things */
	public void removeChar(BCRemoveCharOrder order) {
		String playerName = order.getPlayerName();
		parent.playerModule.remove(playerName, null);
	}
	
	/** locks many things */
	public void changeChar(BCChangeCharOrder order) {
		GraphicalPlayerStatus status = order.getGraphStatus();
		PlayerGraphics newPlayerGraphics = new PlayerGraphics(status, parent, parent);
		String playerName = order.getPlayerName();
		parent.playerModule.removeAndAdd(playerName, newPlayerGraphics, null , null);
	}
	
	
	
	/** locks movingPlayerGraphics, animatedPlayerGraphics, zOrderedDrawables */
	void unloadMap() {
		parent.sync().multisync(parent.moverModule.getSynchroStuffForTryStop(null), true, new Runnable() {
			@Override
			public void run() {
				parent().moverModule.tryStopAll();
				synchronized(parent().zOrderedDrawables) {
					parent().currentPlayerGraphics.clear();
					parent().activePlayerName = null;
					parent().currentMap = null;
					parent().zOrderedDrawables.clear();
				}
			}
		});
	}
	
	/** locks currentPlayerGraphics, movingPlayerGraphics, animatedPlayerGraphics, zOrderedDrawables<br/>
	 * A new map will be loaded: Remove all movements and
	 * playerGraphics and register them anew from the MapInfo */
	public void setMap(final BCSetMapOrder order) {
		final MapInfo mapInfo = order.getMapInfo();
		Object[] syncOn = Tools.concat(parent.playerModule.getSynchroStuffForModification(),
									parent.moverModule.getSynchroStuffForTryStop(null));
		parent.sync().multisync(syncOn, true, new Runnable() {
			@Override
			public void run() {
				unloadMap();
				// everything is unloaded, now load:
				parent().currentMap = mapInfo.map;
				assert(parent().currentMap != null);
				parent().activePlayerName = order.getActivePlayerName();
				// load decorations
				if (parent().currentMap.getDecoInfos() != null) {
					for (GraphicalDecoStatus decoInfo : parent().currentMap.getDecoInfos()) {
						Decoration decoration = new Decoration(parent(), parent(), decoInfo);
						parent().zOrderedDrawables.add(decoration, decoration.getZOrder());
					}
				}
				// create link decorations
				if (parent().currentMap.getLinks() != null) {
					for (Link link : parent().currentMap.getLinks()) {
						GraphicalDecoStatus decoInfo = new GraphicalDecoStatus();
						decoInfo.name = "link";
						decoInfo.x = link.getSourceX();
						decoInfo.y = link.getSourceY();
						Decoration decoration = new Decoration(parent(), parent(), decoInfo);
						parent().zOrderedDrawables.add(decoration, decoration.getZOrder());
					}
				}
				assert(mapInfo.players != null);
				for (Entry<String, GraphicalPlayerStatus> entry : mapInfo.players.entrySet()) {
					String playerName = entry.getKey();
					GraphicalPlayerStatus status = entry.getValue();
					PlayerGraphics playerGraphics = new PlayerGraphics(status, parent(), parent());
					parent().playerModule.add(playerName, playerGraphics, null);
				}
			}
		});
	}
	
	/** locks clientStatusMutexLock, movingPlayerGraphics,
	 *  animatedPlayerGraphics, zOrderedGraphics */
	public void setClientStatusOrder(BCSetClientStatusOrder order) {
		ClientStatus newStatus = order.getNewStatus();
		if (newStatus == ClientStatus.disconnected) {
			// Don't call disconnect() here as the serverRunnable cannot
			// force its own termination at this point. Instead
			// we'll just close the input, causing this serverRunnable to
			// call disconnect() in its main loop where it can terminate.
			terminationLogic();
			return;
		}
		synchronized(parent.clientStatusMutexLock) {
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
}
