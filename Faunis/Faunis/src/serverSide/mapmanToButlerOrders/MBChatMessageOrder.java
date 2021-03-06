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
package serverSide.mapmanToButlerOrders;

import serverSide.butlerToMapmanOrders.BMChatMessageOrder;
import serverSide.mapManager.MapManager;

public class MBChatMessageOrder extends MBOrder {
	private final String fromPlayername;
	private final String toPlayername;
	private final String message;

	public MBChatMessageOrder(MapManager source, String from, String to, String msg) {
		super(source);
		this.fromPlayername = from;
		this.toPlayername = to;
		this.message = msg;
	}

	public MBChatMessageOrder(MapManager source, BMChatMessageOrder order) {
		super(source);
		this.fromPlayername = order.getFromName();
		this.toPlayername = order.getToName();
		this.message = order.getMessage();
	}

	public String getFromPlayername() {
		return fromPlayername;
	}

	public String getToPlayername() {
		return toPlayername;
	}

	public String getMessage() {
		return message;
	}
}
