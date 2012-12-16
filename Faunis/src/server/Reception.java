/* Copyright 2012 Simon Ley alias "skarute"
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
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/** Exactly one instance is created.
 *	Receives clients that want to connect to the game,
 *	Creates butlers for them and redirects them to a new port.
 */
public class Reception{
	
	protected boolean stopRunning;
	protected MainServer parent;
	private int port; // reception port where clients come to
	private Thread recThread;	// runs in the background and listens at the port
	private Runnable recRunnable; // the job of recThread
	protected ServerSocket recSocket; // 
	
	public Reception(MainServer parent, int port){
		this.stopRunning = false;
		this.parent = parent;
		this.port = port;
		
		try {
			this.recSocket = new ServerSocket(this.port);
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Couldn't create reception socket!");
		}
		this.recRunnable = new RecRunnable();
		this.recThread = new Thread(this.recRunnable);
		this.recThread.start();
	}
	
	
	class RecRunnable implements Runnable {
		public void run() {
			System.out.println("Reception runnable runs");
			while(!stopRunning){
				// Steadily listens at recSocket:
				// accept() blocks the execution until there's
				// an incoming client.
				// Negotiates a connection (clientSocket) with new clients
				// and creates a new butler for them.
				Socket clientSocket;
				try {
					clientSocket = recSocket.accept();
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Error while listening to the reception socket!");
					return;
				}
				System.out.println("Client heard, create butler ...");
				parent.createButler(clientSocket);
			}
		}
	}
	
	/** NOTE: Execution doesn't wait for reception to terminate. */
	public void shutdown() {
		stopRunning = true;
		try {
			recSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Couldn't choke reception thread!");
		}
	}
}
