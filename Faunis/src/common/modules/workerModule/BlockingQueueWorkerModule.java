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

import java.util.concurrent.BlockingQueue;

import common.TerminationException;
import common.modules.ModuleOwner;

public abstract class BlockingQueueWorkerModule<MESSAGE_TYPE, POISON_PILL extends MESSAGE_TYPE,
												PARENT extends ModuleOwner>	extends WorkerModule<MESSAGE_TYPE, PARENT> {
	public BlockingQueue<MESSAGE_TYPE> input;
	private final POISON_PILL poisonPill;
	private final Class<POISON_PILL> poisonPillType;
	
	public BlockingQueueWorkerModule(POISON_PILL poisonPill, Class<POISON_PILL> poisonPillType,
									 String threadName, boolean notifyTooIfTerminatedPurposely) {
		super(threadName, notifyTooIfTerminatedPurposely);
		this.poisonPill = poisonPill;
		this.poisonPillType = poisonPillType;
	}
	
	@Override
	public void terminationLogic() {
		input.clear();
		try {
			input.put(poisonPill);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException("Interrupted while trying to terminate "+threadName+"!");
		}
	}
	
	@Override
	protected MESSAGE_TYPE tryGetMessage() throws TerminationException {
		// TODO Auto-generated method stub
		MESSAGE_TYPE message = null;
		while (message == null) {
			try {
				message = input.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (poisonPillType.isInstance(message))
			throw new TerminationException();
		else
			return message;
	}

	public void put(MESSAGE_TYPE message) {
		try {
			input.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not pass message to "+threadName+"!");
		}
	}
}
