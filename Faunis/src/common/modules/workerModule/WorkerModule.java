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
package common.modules.workerModule;

import java.util.concurrent.locks.ReentrantLock;

import common.TerminationException;
import common.modules.ModuleOwner;

/** Represents a module that, having an own thread, works off messages
 * one after another from an input, therefore called Worker. */
public abstract class WorkerModule<MESSAGE_TYPE, PARENT extends ModuleOwner> {
	protected PARENT parent;
	protected Thread thread;
	protected final String threadName;
	protected final Runnable runnable;
	protected boolean terminatedPurposely;
	protected final boolean notifyTooIfTerminatedPurposely;
	protected final ReentrantLock terminationLock;
	protected boolean terminated;
	
	/** Creates a worker which still has to be started.
	 * Don't forget to call init() and start() afterwards.
	 * @param notifyTooIfTerminatedPurposely indicates if notifyTermination()
	 * should also be called if stop() had been called from outside. Note that
	 * setting it to false doesn't guarantee in all cases that notifyTermination()
	 * won't be called after executing stop(), only in most cases. */
	public WorkerModule(String threadName, boolean notifyTooIfTerminatedPurposely) {
		this.terminated = true;
		this.terminatedPurposely = false;
		this.terminationLock = new ReentrantLock();
		this.notifyTooIfTerminatedPurposely = notifyTooIfTerminatedPurposely;
		this.threadName = threadName;
		this.runnable = new WorkerRunnable();
	}
	
	public final void init(PARENT _parent) {
		this.parent = _parent;
	}
	
	/** When the worker is terminating, this method is called and you can put
	 * some notification logic in here. The worker will terminate afterwards. */
	protected abstract void notifyTermination();
	
	/** The implementation on how to terminate this worker. This method should
	 * then send some kind of termination signal over the worker's input,
	 * for example close the input stream or push a special message object. */
	protected abstract void terminationLogic();
	
	/** Tries to retrieve a message object from input. If this worker
	 * receives the signal to terminate over his input or the input is
	 * cut off, a TerminationException must be thrown by this method. */
	protected abstract MESSAGE_TYPE tryGetMessage() throws TerminationException;
	
	/** Whenever a message is read from input, handle it in here. Note that
	 * the message might be null. */
	protected abstract void handleMessage(MESSAGE_TYPE message);
	
	public void start() {
		terminationLock.lock();
		try {
			if (terminated) {
				terminated = false;
				terminatedPurposely = false;
				thread = new Thread(runnable, threadName);
				thread.start();
			}
		} finally {
			terminationLock.unlock();
		}
	}

	/** Call this to ask this worker to terminate. */
	public void stop(boolean waitForTermination, boolean errorIfSelfWaiting) {
		terminationLock.lock();
		try {
			if (!terminated) {
				terminated = true;
				terminatedPurposely = true;
				terminationLogic();
				if (waitForTermination)
					waitForTermination(errorIfSelfWaiting);
			}
		} finally {
			this.terminationLock.unlock();
		}
	}
	
	public void waitForTermination(boolean errorIfSelfWaiting) {
		if (Thread.currentThread() != thread) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else if (errorIfSelfWaiting) {
			throw new RuntimeException("Cannot wait for own termination!");
		}
	}
	
	protected class WorkerRunnable implements Runnable {
		@Override
		public void run() {
			while (true) {
				MESSAGE_TYPE message = null;
				try {
					message = tryGetMessage();
				} catch(TerminationException e) {
					if (terminationLock.tryLock()) {
						try {
							terminated = true;
							if (!terminatedPurposely || notifyTooIfTerminatedPurposely)
								notifyTermination();
							break;
						} finally {
							terminationLock.unlock();
						}
					} else if (terminatedPurposely) {
						// TODO: I know that there is a concurrency problem here,
						// but I don't know how to fix it
						// --> it may happen that termination is executed twice
						terminated = true;
						if (notifyTooIfTerminatedPurposely)
							notifyTermination();
						break;
					}
				}
				handleMessage(message);
			}
		}
	}
	
	public final PARENT parent() {
		return parent;
	}
}
