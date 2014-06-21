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
package common.modules.objectModule;

import java.util.Map;

import common.modules.ModuleOwner;

/** A module for managing an amount of objects, stored in a map.
 * A post-processing trigger when adding and a pre-processing trigger when
 * removing elements must be defined by the subclass. You can also
 * pass an additional argument to the adding / removing method. */
public abstract class ObjectModule<KEY, VALUE, PARENT extends ModuleOwner, ADD_ARG, REMOVE_ARG> {
	protected Map<KEY, VALUE> map;
	protected PARENT parent;
	
	/** Post trigger called after an element has been added by add(). */
	public abstract void added(KEY key, VALUE value, ADD_ARG argument);
	/** Pre trigger called before an element will be removed by remove(). */
	public abstract void beforeRemove(KEY key, VALUE value, REMOVE_ARG argument);
	/** Should return all objects to synchronize on when add() / remove()
	 * is executed. */
	
	/** Don't forget to call init() afterwards. */
	public ObjectModule(Map<KEY, VALUE> map) {
		this.map = map;
	}
	
	public final void init(PARENT _parent) {
		this.parent = _parent;
	}
	
	/** locks on objects given by getSynchroStuffForModification()<br/>
	 * Adds given key-value pair to the administered map. The optional argument
	 * may be used by the post trigger added(). */
	public void add(final KEY key, final VALUE value, final ADD_ARG argument) {
		assert(key != null);
		assert(!map.containsKey(key));
		assert(value != null);
		map.put(key, value);
		added(key, value, argument);
	}

	/** locks on objects given by getSynchroStuffForModification()<br/>
	 * Removes given key and its according value from the administered map.
	 * The optional argument may be used by the pre trigger beforeRemove(). */
	public void remove(final KEY key, final REMOVE_ARG argument) {
		assert(key != null);
		VALUE value = map.get(key);
		assert(value != null);
		beforeRemove(key, value, argument);
		map.remove(key);
	}

	/** locks on objects given by getSynchroStuffForModification()<br/>
	 * Advantage of this method over calling remove() and add() separately:
	 * The execution stays synchronized the whole time.<br/>
	 * First removes key from, then adds it with given newValue to administered map.
	 * The optional arguments may be used by the pre trigger beforeRemove() and post
	 * trigger added() respectively. */
	public void removeAndAdd(final KEY key, final VALUE newValue, final REMOVE_ARG removeArgument, final ADD_ARG addArgument) {
		remove(key, removeArgument);
		add(key, newValue, addArgument);
	}
}
