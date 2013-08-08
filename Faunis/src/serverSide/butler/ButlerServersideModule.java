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
package serverSide.butler;

import serverSide.butlerToMapmanOrders.BMRegisterOrder;
import serverSide.butlerToMapmanOrders.BMUnregisterOrder;
import serverSide.mapManager.MapManager;
import serverSide.mapmanToButlerOrders.MBAddCharOrder;
import serverSide.mapmanToButlerOrders.MBChangeCharOrder;
import serverSide.mapmanToButlerOrders.MBCharAtOtherMapmanOrder;
import serverSide.mapmanToButlerOrders.MBChatMessageOrder;
import serverSide.mapmanToButlerOrders.MBErrorMessageOrder;
import serverSide.mapmanToButlerOrders.MBMapInfoOrder;
import serverSide.mapmanToButlerOrders.MBOrder;
import serverSide.mapmanToButlerOrders.MBRemoveCharOrder;
import serverSide.mapmanToButlerOrders.MBPoisonPillOrder;
import common.Link;
import common.Logger;
import common.butlerToClientOrders.BCAddCharOrder;
import common.butlerToClientOrders.BCChangeCharOrder;
import common.butlerToClientOrders.BCChatMessageOrder;
import common.butlerToClientOrders.BCRemoveCharOrder;
import common.butlerToClientOrders.BCSetMapOrder;
import common.modules.workerModule.BlockingQueueWorkerModule;

public class ButlerServersideModule extends BlockingQueueWorkerModule<MBOrder, MBPoisonPillOrder, Butler> {

	public ButlerServersideModule(String threadName,
			boolean notifyTooIfTerminatedPurposely) {
		super(new MBPoisonPillOrder(null), MBPoisonPillOrder.class, threadName, notifyTooIfTerminatedPurposely);
	}

	@Override
	protected void notifyTermination() {
		parent.shutdown();
	}

	@Override
	protected void handleMessage(MBOrder order) {
		// Assert that given order comes from our active mapman!
		// the only exception is if it's set to null (needed for chat orders, f.ex.)
		if (order.getSource() != null && order.getSource() != parent.activeMapman) {
			Logger.log("Butler: Received order from foreign mapman!");
			return;
		}
		if (order instanceof MBAddCharOrder) {
			clientAddChar((MBAddCharOrder) order);
		} else if (order instanceof MBChangeCharOrder) {
			clientChangeChar((MBChangeCharOrder) order);
		} else if (order instanceof MBRemoveCharOrder) {
			clientRemoveChar((MBRemoveCharOrder) order);
		} else if (order instanceof MBChatMessageOrder) {
			clientChatMessage((MBChatMessageOrder) order);
		} else if (order instanceof MBMapInfoOrder) {
			clientMapInfo((MBMapInfoOrder) order);
		} else if (order instanceof MBCharAtOtherMapmanOrder) {
			changeMapman((MBCharAtOtherMapmanOrder) order);
		} else if (order instanceof MBErrorMessageOrder) {
			parent.clientsideWorker.sendErrorMessage(((MBErrorMessageOrder) order).getErrorMessage());
		}
		// TODO: Handle further serverside orders
	}
	
	void clientChatMessage(MBChatMessageOrder order) {
		String toName = order.getToPlayername();
		boolean isBroadcast = (toName == null || toName.equals(""));
		parent.clientsideWorker.sendOrderToClient(new BCChatMessageOrder(order.getMessage(),
													order.getFromPlayername(),
													isBroadcast));
	}
	
	void clientAddChar(MBAddCharOrder order) {
		parent.clientsideWorker.sendOrderToClient(new BCAddCharOrder(order));
	}
	
	void clientChangeChar(MBChangeCharOrder order) {
		parent.clientsideWorker.sendOrderToClient(new BCChangeCharOrder(order));
	}
	
	void clientRemoveChar(MBRemoveCharOrder order) {
		parent.clientsideWorker.sendOrderToClient(new BCRemoveCharOrder(order));
	}
	
	void clientMapInfo(MBMapInfoOrder order) {
		parent.clientsideWorker.sendOrderToClient(new BCSetMapOrder(order, parent.activePlayer.getName()));
	}
	
	/** locks parent.activePlayer */
	void changeMapman(MBCharAtOtherMapmanOrder order) {
		MapManager oldMapman = order.getSource();
		assert(parent.activeMapman == oldMapman);
		Link link = order.getLink();
		String newMap = link.getTargetMap();
		MapManager newMapman = parent.parent.getMapman(newMap);
		removePlayerFromMapman(oldMapman, true);
		do {
			// TODO: Performance!
			Logger.log("Waiting for mapman to remove active player...");
		} while (parent.activePlayer.getMapName() != null);
		synchronized(parent.activePlayer) {
			link.move(parent.activePlayer);
		}
		addPlayerToMapman(newMapman, true);
		parent.activeMapman = newMapman;
	}
	
	/** Registers the active player at given mapman.
	 * By doing so, the mapman will consequently send a MBMapInfoOrder
	 * to the butler, who passes it on to the client.*/
	void addPlayerToMapman(MapManager mapman, boolean addPlayerMapEntry) {
		assert(parent.activePlayer != null);
		assert(mapman != null);
		mapman.put(new BMRegisterOrder(parent, parent.activePlayer, addPlayerMapEntry));
	}
	
	void removePlayerFromMapman(MapManager mapman, boolean removePlayerMapEntry) {
		assert(parent.activePlayer != null);
		assert(mapman != null);
		mapman.put(new BMUnregisterOrder(parent, parent.activePlayer, removePlayerMapEntry));
	}
}
