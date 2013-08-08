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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import serverSide.Account;
import serverSide.MainServer;
import serverSide.mapManager.MapManager;
import serverSide.mapmanToButlerOrders.*;
import serverSide.player.Player;
import common.Logger;
import common.Sync;
import common.modules.ModuleOwner;


/** The butler looks after the needs of a client.
 * He receives the socket that the reception has already created.
 * He represents his client on the server side. */
public class Butler implements ModuleOwner {
	private AtomicBoolean shutdownOccupied;
	protected final MainServer parent;
	protected final Socket clientSocket; // the socket through which to communicate with the client
	protected final ObjectOutputStream clientOutput; // to write to the socket; synchronised access only
	protected final ButlerClientsideModule clientsideWorker;
	protected final ButlerServersideModule serversideWorker;
	protected Account loggedAccount;		// account for which the client is currently logged in (may also be null)
	protected Player activePlayer;
	protected MapManager activeMapman;
	
	//MapMan's Variables
	protected final Sync sync;
	
	public Butler(MainServer parent, Socket clientSocket) {
		this.parent = parent;
		
		String clientThreadname = "butler_"+clientSocket.getLocalPort()+"_clientThread";
		String serverThreadname = "butler_"+clientSocket.getLocalPort()+"_serverThread";
		this.clientsideWorker = new ButlerClientsideModule(clientThreadname, false);
		this.serversideWorker = new ButlerServersideModule(serverThreadname, false);
		this.serversideWorker.input = new ArrayBlockingQueue<MBOrder>(50);
		
		this.clientSocket = clientSocket;
		// create input and output streams from clientSocket
		Logger.log("Butler: Try to create input / output streams...");
		try {
			// we cannot put clientOutput into clientsideWorker because
			// everything that has to be synced on should stay in the common class
			this.clientOutput = new ObjectOutputStream(this.clientSocket.getOutputStream());
			this.clientsideWorker.input = new ObjectInputStream(this.clientSocket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Couldn't create input / output stream!");
		}
		Logger.log("Input and output streams have been created.");
		
		this.sync = new Sync(new Object[] {this.clientOutput});
	}
	
	public void init() {
		clientsideWorker.init(this);
		serversideWorker.init(this);
		
		this.shutdownOccupied = new AtomicBoolean(false);
				
		clientsideWorker.start();
		serversideWorker.start();
	}
	
	public boolean assertActivePlayer() {
		if (activePlayer == null) {
			clientsideWorker.sendErrorMessage("Command requires loaded player!");
			return false;
		}
		return true;
	}
	public boolean assertLoggedAccount() {
		if (loggedAccount == null) {
			clientsideWorker.sendErrorMessage("Command requires logged account!");
			return false;
		}
		return true;
	}
	
	@Override
	public Sync sync() {
		return sync;
	}
	
	public Socket getClientSocket() {
		return clientSocket;
	}
	
	public int getLocalPort() {
		return clientSocket.getLocalPort();
	}
	
	/** Shuts this butler down. Only one thread is allowed
	 * to call this simultaneously. All others are rejected (returns false),
	 * thus not blocked by calling this.<br/>
	 * Asserts that clientThread and serverThread terminate,
	 * unless it isn't one of them that calls this method, in
	 * which case only the termination of the other is asserted
	 * and it is the responsibility of the caller to terminate afterwards
	 * immediately. Thus, wherever clientsideWorker / serversideWorker calls
	 * shutdown(), it must terminate thereafter. */
	public boolean shutdown(){
		if (shutdownOccupied.compareAndSet(false, true)) {
			clientsideWorker.stop(true, false);
			serversideWorker.stop(true, false);
			// Save data, log out, unregister butler etc.
			if (loggedAccount != null)
				clientsideWorker.logoutAccount();
			parent.deleteButler(this);
			return true;
		} else
			return false;
	}
	
	public void put(MBOrder order) {
		serversideWorker.put(order);
	}
}
