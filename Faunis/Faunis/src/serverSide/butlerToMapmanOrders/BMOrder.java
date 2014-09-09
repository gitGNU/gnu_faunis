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
package serverSide.butlerToMapmanOrders;

import serverSide.butler.Butler;

public abstract class BMOrder {
	private Butler source;
	/**
	 * "done" is set to true when or before the mapman has
	 * executed what the order said.
	 * Note that both the butlers and the mapmans may block:
	 * The butlers block until "done" is set to true, and the
	 * mapmans block until they can push orders to the butlers.
	 * Thus, the "done" flag must be set before the mapman
	 * pushes any orders by himself. */
	private boolean done;

	BMOrder(Butler source) {
		this.source = source;
		this.done = false;
	}

	public Butler getSource() {
		return source;
	}

	public boolean isDone() {
		return done;
	}
	public void setDone(boolean value) {
		this.done = value;
	}

	public void waitUntilDone() {
		while (!this.done) {
			Thread.yield();
		}
	}
}
