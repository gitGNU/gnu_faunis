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
package communication.butlerToClientOrders;

import communication.enums.ClientStatus;

/** The butler tells the client that his client status should change. */
public class BCSetClientStatusOrder extends BCOrder {
	private static final long serialVersionUID = 1L;
	private ClientStatus newStatus;
	
	public BCSetClientStatusOrder(ClientStatus newStatus) {
		this.newStatus = newStatus;
	}
	
	public ClientStatus getNewStatus() {
		return this.newStatus;
	}

}
