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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import common.TerminationException;
import common.modules.ModuleOwner;

public abstract class ObjectInputStreamWorkerModule<MESSAGE_TYPE extends Serializable, PARENT extends ModuleOwner>
											 extends WorkerModule<MESSAGE_TYPE, PARENT> {
	private final Class<MESSAGE_TYPE> messageType;
	public ObjectInputStream input;
	
	public ObjectInputStreamWorkerModule(Class<MESSAGE_TYPE> messageType,
			String threadName,
			boolean notifyTooIfTerminatedPurposely) {
		super(threadName, notifyTooIfTerminatedPurposely);
		this.messageType = messageType;
	}

	@Override
	public void terminationLogic() {
		try {
			input.close();
		} catch (IOException e) {
			throw new RuntimeException("Couldn't choke "+threadName+"!");
		}
	}
	
	@Override
	protected MESSAGE_TYPE tryGetMessage() throws TerminationException {
		Object read = null;
		while (read == null || !(messageType.isInstance(read))) {
			try {
				read = input.readObject();
			} catch(IOException e) {
				throw new TerminationException();
			} catch(ClassNotFoundException e) {
				throw new RuntimeException("Class not found!");
			}
		}
		return messageType.cast(read);
	}
}




