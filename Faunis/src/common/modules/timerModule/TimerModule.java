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
package common.modules.timerModule;

import java.util.Map;
import java.util.Map.Entry;

import common.modules.ModuleOwner;

/** A module to manage an amount of */
public abstract class TimerModule<MODIFIABLE extends TimeModifiable, TIMER extends ModTimer,
								  PARENT extends ModuleOwner, START_ARG> {
	protected Map<MODIFIABLE, TIMER> map;
	protected PARENT parent;
	
	/** Should return all objects to synchronize on when tryStop on given timeModifiable is executed. */
	public abstract Object[] getSynchroStuffForTryStop(MODIFIABLE timeModifiable);
	
	/** Post trigger after tryStop() has been called. */
	public abstract void unregistered(MODIFIABLE forTimeModifiable);
	public abstract Object[] getSynchroStuffForUnregisterModTimer(MODIFIABLE timeModifiable);

	/** Post trigger after tryStart() has been called. */
	public abstract void started(MODIFIABLE timeModifiable);
	/** Should return all objects to synchronize on when tryStart on given timeModifiable is executed. */
	public abstract Object[] getSynchroStuffForTryStart(MODIFIABLE timeModifiable);
	
	/** Called by tryStart(). <br/>
	 * Should return a ready-to-run ModTimer object for given timeModifiable.
	 * An additional argument for its construction can be given. If you want to cancel the start
	 * because of some condition not met, return null. */
	public abstract TIMER createCompleteModTimer(MODIFIABLE timeModifiable, START_ARG argument);
	
	/** Should return a list of all objects to synchronize on when a ModTimer runs. Since it might
	 * decide to stop running and unregister, this list must also contain everything from
	 * getSynchroStuffForUnregisterModTimer() in the correct order. */
	public abstract Object[] getSynchroStuffForModTimerRunOrUnregister(MODIFIABLE timeModifiable);

	/** Don't forget to call init() afterwards. */
	public TimerModule(Map<MODIFIABLE, TIMER> map) {
		this.map = map;
	}
	
	public final void init(PARENT _parent) {
		this.parent = _parent;
		assertContainment();
	}
	
	/** assert that getSynchroStuffForUnregisterModTimer() is contained in
	 * getSynchroStuffForModTimerRunOrUnregister():
	 */
	private void assertContainment() {
		Object[] superArray = getSynchroStuffForModTimerRunOrUnregister(null);
		Object[] subArray = getSynchroStuffForUnregisterModTimer(null);
		int superIndex = 0;
		int subIndex = 0;
		while (subIndex < subArray.length) {
			boolean found = false;
			while (superIndex < superArray.length) {
				if (subArray[subIndex] == superArray[superIndex]) {
					found = true;
					break;
				}
				superIndex++;
			}
			if (!found)
				throw new RuntimeException("assertContainment() failed!");
			subIndex++;
			superIndex++;
		}
	}
	
	/** Stops all registered ModTimers. */
	public void tryStopAll() {
		parent.sync().multisync(getSynchroStuffForTryStop(null), true, new Runnable() {
			@Override
			public void run() {
				for (Entry<MODIFIABLE, TIMER> entry : map.entrySet()) {
					MODIFIABLE timeModifiable = entry.getKey();
					TIMER myTimer = entry.getValue();
					synchronized(timeModifiable) {
						if (myTimer != null)
							myTimer.stopAndUnregister();
					}
				}
			}
		});
	}
	
	/** Stops ModTimer for given timeModifiable and unregisters it. */
	public void tryStop(final MODIFIABLE timeModifiable) {
		parent.sync().multisync(getSynchroStuffForTryStop(timeModifiable), new Runnable() {
			@Override
			public void run() {
				TIMER myTimer = map.get(timeModifiable);
				if (myTimer != null)
					myTimer.stopAndUnregister();
			}
		});
	}

	/** Unregisters ModTimer for given timeModifiable. Don't call this
	 * to stop it, call tryStop() instead. */
	public void unregisterModTimer(final MODIFIABLE forTimeModifiable) {
		parent.sync().multisync(getSynchroStuffForUnregisterModTimer(forTimeModifiable), new Runnable() {
			@Override
			public void run() {
				assert(map.containsKey(forTimeModifiable));
				map.remove(forTimeModifiable);
				unregistered(forTimeModifiable);
			}
		});
	}

	/** locks objects given by getSynchroStuffForTryStart() <br/>
	 * creates a new ModTimer for given timeModifiable and starts it. */
	public void tryStart(final MODIFIABLE timeModifiable, final START_ARG argument) {
		parent.sync().multisync(getSynchroStuffForTryStart(timeModifiable), new Runnable() {
			@Override
			public void run() {
				if (map.containsKey(timeModifiable))
					return;
				TIMER myTimer = createCompleteModTimer(timeModifiable, argument);
				if (myTimer == null)
					return;
				map.put(timeModifiable, myTimer);
				myTimer.start();
				started(timeModifiable);
			}
		});
	}
	
	/** Getter for ModuleOwner to which this module belongs. */
	public final PARENT parent() {
		return parent;
	}
}
