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
package common.modules.timerModule;

import java.util.Map;
import java.util.Map.Entry;

import common.modules.ModuleOwner;

/** A module to manage an amount of */
public abstract class TimerModule<MODIFIABLE extends TimeModifiable, TIMER extends ModTimer,
								  PARENT extends ModuleOwner, START_ARG> {
	protected Map<MODIFIABLE, TIMER> map;
	protected PARENT parent;
		
	/** Post trigger after tryStop() has been called. */
	public abstract void unregistered(MODIFIABLE forTimeModifiable);

	/** Post trigger after tryStart() has been called. */
	public abstract void started(MODIFIABLE timeModifiable);
	
	/** Called by tryStart(). <br/>
	 * Should return a ready-to-run ModTimer object for given timeModifiable.
	 * An additional argument for its construction can be given. If you want to cancel the start
	 * because of some condition not met, return null. */
	public abstract TIMER createCompleteModTimer(MODIFIABLE timeModifiable, START_ARG argument);
	
	/** Don't forget to call init() afterwards. */
	public TimerModule(Map<MODIFIABLE, TIMER> map) {
		this.map = map;
	}
	
	public final void init(PARENT _parent) {
		this.parent = _parent;
	}	
	
	/** Stops all registered ModTimers. */
	public void tryStopAll() {
		while (!map.isEmpty()) {
			Entry<MODIFIABLE, TIMER> firstEntry = map.entrySet().iterator().next(); 
			TIMER myTimer = firstEntry.getValue();
			if (myTimer != null)
				myTimer.stopAndUnregister();
		}
	}
	
	/** Stops ModTimer for given timeModifiable and unregisters it. */
	public void tryStop(final MODIFIABLE timeModifiable) {
		TIMER myTimer = map.get(timeModifiable);
		if (myTimer != null)
			myTimer.stopAndUnregister();
	}

	/** Unregisters ModTimer for given timeModifiable. Don't call this
	 * to stop it, call tryStop() instead. */
	public void unregisterModTimer(final MODIFIABLE forTimeModifiable) {
		assert(map.containsKey(forTimeModifiable));
		map.remove(forTimeModifiable);
		unregistered(forTimeModifiable);
	}

	/** locks objects given by getSynchroStuffForTryStart() <br/>
	 * creates a new ModTimer for given timeModifiable and starts it. */
	public void tryStart(final MODIFIABLE timeModifiable, final START_ARG argument) {
		if (map.containsKey(timeModifiable))
			return;
		TIMER myTimer = createCompleteModTimer(timeModifiable, argument);
		if (myTimer == null)
			return;
		map.put(timeModifiable, myTimer);
		myTimer.start();
		started(timeModifiable);
	}
	
	/** Getter for ModuleOwner to which this module belongs. */
	public final PARENT parent() {
		return parent;
	}
}
