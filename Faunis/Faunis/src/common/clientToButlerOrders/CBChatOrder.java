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
package common.clientToButlerOrders;

/** The client tells the butler that he wants to send a chat message. */
public class CBChatOrder extends CBOrder {
	private static final long serialVersionUID = 1L;
	private String message;
	private String toName;
	
	/** If toName = "" or null, then the message will be broadcast on the map. */
	public CBChatOrder(String message, String toName){
		this.message = message;
		this.toName = toName;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getToName() {
		return toName;
	}
}
