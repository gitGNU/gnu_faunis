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

import java.io.IOException;

import serverSide.Account;
import serverSide.Result;
import serverSide.butlerToMapmanOrders.BMChatMessageOrder;
import serverSide.butlerToMapmanOrders.BMMoveOrder;
import serverSide.butlerToMapmanOrders.BMOrder;
import serverSide.butlerToMapmanOrders.BMTriggerAnimationOrder;
import serverSide.player.Player;

import common.Logger;
import common.butlerToClientOrders.*;
import common.clientToButlerOrders.*;
import common.enums.CharacterClass;
import common.enums.ClientStatus;
import common.modules.workerModule.ObjectInputStreamWorkerModule;

public class ButlerClientsideModule extends ObjectInputStreamWorkerModule<CBOrder, Butler> {
	public ButlerClientsideModule(String threadName, boolean notifyTooIfTerminatedPurposely) {
		super(CBOrder.class, threadName, notifyTooIfTerminatedPurposely);
	}

	@Override
	protected void notifyTermination() {
		parent.shutdown();
	}

	@Override
	protected void handleMessage(CBOrder read) {
		if (read instanceof CBDisconnectOrder){
			Logger.log("CBDisconnectOrder");
			disconnect();
		} else if (read instanceof CBCreatePlayerOrder) {
			Logger.log("CBCreatePlayerOrder");
			createNewPlayer((CBCreatePlayerOrder) read);
		} else if (read instanceof CBChatOrder){
			Logger.log("CBChatOrder");
			forwardChatOrder((CBChatOrder) read);
		} else if (read instanceof CBLoginOrder){
			Logger.log("CBLoginOrder");
			loginAccount((CBLoginOrder) read);
		} else if (read instanceof CBLogoutOrder){
			Logger.log("CBLogoutOrder");
			logoutAccount();
		} else if (read instanceof CBLoadPlayerOrder) {
			Logger.log("CBLoadPlayerOrder");
			loadActivePlayer((CBLoadPlayerOrder) read);
		} else if (read instanceof CBUnloadPlayerOrder) {
			Logger.log("CBUnloadPlayerOrder");
			unloadActivePlayer();
		} else if (read instanceof CBMoveOrder) {
			Logger.log("CBMoveOrder");
			moveChar((CBMoveOrder) read);
		} else if (read instanceof CBTriggerAnimationOrder) {
			Logger.log("CBTriggerAnimationOrder");
			forwardAnimationOrder((CBTriggerAnimationOrder) read);
		} else if (read instanceof CBServerSourceOrder) {
			Logger.log("CBServerSourceOrder");
			sendOrderToClient(new BCSystemMessageOrder(
				"Server source code at "+parent.parent.getServerSettings().serverSourceAt()));
		} else if (read instanceof CBQueryOwnPlayersOrder) {
			sendOrderToClient(new BCOwnPlayersInfoOrder(parent.loggedAccount.getPlayerNames()));
		}
	}

	/** locks parent.clientOutput */
	void sendOrderToClient(BCOrder order) {
		assert(parent.clientOutput != null);
		synchronized(parent.clientOutput) {
			if (order == null) {
				Logger.log("Order is null!");
			}
			try {
				parent.clientOutput.writeObject(order);
			} catch (IOException e) {
				Logger.log("Butler: Couldn't pass order to client!");
			}
		}
	}
	
	void sendOrderToMapman(BMOrder order) {
		parent.activeMapman.put(order);
	}
	
	void sendErrorMessage(String errorMessage) {
		sendOrderToClient(new BCErrorMessageOrder(errorMessage));
	}

	void loginAccount(CBLoginOrder order) {
		if (parent.loggedAccount == null) {
			Result<Account> query = parent.parent.loginAccount(parent, order.getName(),
															order.getPassword());
			if (query.successful()) {
				parent.loggedAccount = query.getResult();
				sendOrderToClient(new BCSetClientStatusOrder(ClientStatus.noCharLoaded));
			} else {
				sendErrorMessage(query.getErrorMessage());
			}
		} else {
			sendErrorMessage("Butler: Couldn't log in since account seems to be already logged in.");
		}
	}
	
	public void createNewPlayer(CBCreatePlayerOrder order) {
		if (!parent.assertLoggedAccount()) return;
		Result<Boolean> result = parent.parent.createNewPlayer(parent.loggedAccount,
									order.getPlayerName(), CharacterClass.ursine);
		if (!result.successful())
			sendErrorMessage(result.getErrorMessage());
	}
	
	public void moveChar(CBMoveOrder order) {
		if (!parent.assertLoggedAccount()) return;
		if (!parent.assertActivePlayer()) return;
		sendOrderToMapman(new BMMoveOrder(parent.activePlayer, parent, order));
	}
	
	void logoutAccount() {
		if (parent.loggedAccount != null) {
			if (parent.activePlayer != null) {
				unloadActivePlayer();
			}
			Result<Boolean> result = parent.parent.logoutAccount(parent.loggedAccount.getName());
			if (!result.successful()) {
				sendErrorMessage(result.getErrorMessage());
				return;
			}
			parent.loggedAccount = null;
			sendOrderToClient(new BCSetClientStatusOrder(ClientStatus.loggedOut));
		} else {
			sendErrorMessage("Butler: Couldn't log out since there's no account logged in!");
		}
	}
	
	public void unloadActivePlayer() {
		if (!parent.assertLoggedAccount()) return;
		if (!parent.assertActivePlayer()) return;
		assert(parent.activeMapman != null);
		parent.serversideWorker.removePlayerFromMapman(parent.activeMapman, false);
		parent.parent.unloadPlayer(parent.activePlayer);
		parent.activePlayer = null;
		parent.activeMapman = null;
		sendOrderToClient(new BCSetClientStatusOrder(ClientStatus.noCharLoaded));
		Logger.log("Butler: Unloaded player.");
	}
	
	/** friendly shutdown: Tells client that he's now disconnected */
	public void disconnect() {
		sendOrderToClient(new BCSetClientStatusOrder(ClientStatus.disconnected));
		parent.shutdown();
	}
	
	public void loadActivePlayer(CBLoadPlayerOrder order) {
		if (!parent.assertLoggedAccount())
			return;
		else if (parent.activePlayer != null) {
			sendErrorMessage("Butler: Couldn't load player: There's already one loaded.");
			return;
		}
		String playerName = order.getPlayerName();
		Result<Player> query = parent.parent.loadAndActivatePlayer(parent.loggedAccount, playerName);
		if (!query.successful()) {
			sendErrorMessage(query.getErrorMessage());
			return;
		}
		parent.activePlayer = query.getResult();
		// Assign player to mapman:
		String mapname = parent.activePlayer.getMapName();
		assert(mapname != null);
		assert(parent.activeMapman == null);
		parent.activeMapman = parent.parent.getMapman(mapname);
		parent.serversideWorker.addPlayerToMapman(parent.activeMapman, false);
		// => the mapman will send a MBMapInfoOrder
		// to the butler who passes it on to the client
		sendOrderToClient(new BCSetClientStatusOrder(ClientStatus.exploring));
		Logger.log("Butler: Player "+playerName+" successfully loaded.");
	}
	
	public void forwardChatOrder(CBChatOrder order) {
		if (!parent.assertActivePlayer()) return;
		sendOrderToMapman(new BMChatMessageOrder(parent, order, parent.activePlayer.getName()));
	}
	
	public void forwardAnimationOrder(CBTriggerAnimationOrder order) {
		if (!parent.assertActivePlayer()) return;
		sendOrderToMapman(new BMTriggerAnimationOrder(parent, parent.activePlayer,
				order.getAnimation()));
	}
}
