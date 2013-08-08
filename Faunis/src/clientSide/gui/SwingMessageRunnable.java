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
package clientSide.gui;

import clientSide.MessageType;

/** A runnable to be put on the AWT stack which prints a given message into the GameWindow's loggingDocument. */
public class SwingMessageRunnable implements Runnable {
	private String message;
	private MessageType type;
	private GameWindow win;
	public SwingMessageRunnable(String message, MessageType type, GameWindow win) {
		this.message = message;
		this.type = type;
		this.win = win;
	}
	
	@Override
	public void run() {
		switch(type) {
			case error:
				win.logErrorMessage(message);
				break;
			case system:
				win.logSystemMessage(message);
				break;
			case whisper:
				win.logWhisperMessage(message);
				break;
			case broadcast:
				win.logBroadcastMessage(message);
				break;
		}
	}

}
