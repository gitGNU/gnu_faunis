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

import common.Link;
import serverSide.mapManager.MapManager;

/** When the player steps on a link on the map, the MapManager tells the Butler
 * with this order that he should assign the player to a new MapManager. The
 * Butler has to do that on his own by calling unregister() and register().
*/
public class MBCharAtOtherMapmanOrder extends MBOrder {
	private final Link link;

	public MBCharAtOtherMapmanOrder(MapManager source, Link link) {
		super(source);
		this.link = link;
	}

	public Link getLink() {
		return link;
	}
}
