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
package server.butlerToMapmanOrders;

import communication.clientToButlerOrders.CBChatOrder;

import server.Butler;

/** Represents a chat order which is forwarded from Butler to Mapman or
 * between Mapmans. Note that the corresponding Butler for toName is not yet
 * identified, therefore the forwarding. */
public class BMChatMessageOrder extends BMOrder {
	private String message;
	private String toName;
	private String fromName;
	
	public BMChatMessageOrder(Butler source, String message, String toName, String fromName) {
		super(source);
		this.message = message;
		this.toName = toName;
		this.fromName = fromName;
	}
	
	public BMChatMessageOrder(Butler source, CBChatOrder order, String fromName) {
		super(source);
		this.message = order.getMessage();
		this.toName = order.getToName();
		this.fromName = fromName;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getToName() {
		return toName;
	}
	
	public String getFromName() {
		return fromName;
	}

}
