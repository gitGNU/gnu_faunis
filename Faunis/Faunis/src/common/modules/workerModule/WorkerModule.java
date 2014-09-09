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
package common.modules.workerModule;

import java.io.IOException;
import common.modules.ModuleOwner;
import mux.MuxObjectInputStream;


public abstract class WorkerModule<MODULE_OWNER extends ModuleOwner> {
	protected MODULE_OWNER parent;
	protected Thread thread;
	protected MuxObjectInputStream mux;
	boolean stopRunning;

	public WorkerModule(MuxObjectInputStream mux, String threadName) {
		this.mux = mux;
		this.thread = new Thread(new WorkerRunnable(), threadName);
		this.stopRunning = false;
	}

	@SuppressWarnings("hiding")
	public void init(MODULE_OWNER parent) {
		this.parent = parent;
	}

	public void start() {
		thread.start();
	}

	public void stop() {
		stopRunning = true;
		mux.close();
		if (Thread.currentThread() != thread) {
			thread.interrupt();
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	class WorkerRunnable implements Runnable {
		@Override
		public void run() {
			while (!stopRunning) {
				Object read = null;
				System.out.println("Worker: Try to read from mux");
				try {
					read = mux.readObject();
				} catch (IOException e) {
					if (stopRunning) {
						break;
					} else {
						e.printStackTrace();
					}
				}
				if (read != null) {
					System.out.println("dealing with message "+read);
					handleMessage(read);
				}
			}
		}
	}

	protected abstract void handleMessage(Object read);

}
