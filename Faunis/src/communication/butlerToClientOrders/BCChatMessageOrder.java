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
package communication.butlerToClientOrders;

/** The butler tells the client that a system message should be shown. */
public class BCChatMessageOrder extends BCOrder {
	private static final long serialVersionUID = 1L;
	private String message;
	private String fromName;
	private boolean isBroadcast;
	
	public BCChatMessageOrder(String message, String fromName, boolean isBroadcast) {
		this.message = message;
		this.fromName = fromName;
		this.isBroadcast = isBroadcast;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getFromName() {
		return fromName;
	}
	
	public boolean isBroadcast() {
		return isBroadcast;
	}
}
