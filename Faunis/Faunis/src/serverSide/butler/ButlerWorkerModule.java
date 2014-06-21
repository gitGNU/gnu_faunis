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
package serverSide.butler;

import mux.MuxObjectInputStream;
import mux.MuxObjectInputStream.BlockingQueueRunnable;
import serverSide.mapmanToButlerOrders.MBOrder;
import common.clientToButlerOrders.CBOrder;
import common.modules.workerModule.WorkerModule;


public class ButlerWorkerModule extends WorkerModule<Butler> {
	public ButlerWorkerModule(MuxObjectInputStream mux, String threadName) {
		super(mux, threadName);
	}
	
	@SuppressWarnings("unchecked")
	public void put(MBOrder order) {
		BlockingQueueRunnable queueRunnable;
		try {
			queueRunnable = mux.getQueueRunnable(0);
		} catch(ArrayIndexOutOfBoundsException e) {
			return;
		}
		try {
			queueRunnable.getQueue().put(order);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void handleMessage(Object read) {
		if (read instanceof CBOrder) {
			parent.clientsideWorker.handleMessage((CBOrder) read);
		} else if (read instanceof MBOrder) {
			parent.serversideWorker.handleMessage((MBOrder) read);
		}
	}
}
