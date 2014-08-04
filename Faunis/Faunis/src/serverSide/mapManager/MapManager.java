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
package serverSide.mapManager;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import serverSide.MainServer;
import serverSide.butler.Butler;
import serverSide.butlerToMapmanOrders.BMOrder;
import serverSide.player.Player;

import common.Map;
import common.MapInfo;
import common.graphics.GraphicalPlayerStatus;
import common.modules.ModuleOwner;
import common.movement.Mover;

/** The map manager manages everything that happens on a map. There is exactly
 * one map manager for each map in the game. All players / butlers on a map have
 * to be registered at the map manager of that map. */
public class MapManager implements ModuleOwner {
	final MainServer parent;
	private final Map map;
	
	final ConcurrentHashMap<Player, Butler> registeredPlayers;
	final ConcurrentHashMap<String, Player> playerNameToPlayer;
	final ConcurrentHashMap<Player, Mover<Player, MapManager>> movingPlayers;

	final MapManagerPlayerModule playerModule;
	final MapManagerMoverModule moverModule;
	final MapManagerWorkerModule workerModule;
	
	public MapManager(MainServer parent, Map map) {
		this.parent = parent;
		this.map = map;
		
		this.registeredPlayers = new ConcurrentHashMap<Player, Butler>();
		this.playerNameToPlayer = new ConcurrentHashMap<String, Player>();
		this.movingPlayers = new ConcurrentHashMap<Player, Mover<Player, MapManager>>();
		
		this.playerModule = new MapManagerPlayerModule(registeredPlayers);
		this.moverModule = new MapManagerMoverModule(movingPlayers);
		this.workerModule = new MapManagerWorkerModule(
			new ArrayBlockingQueue<BMOrder>(50), map.getName()
		);
	}
	
	public void init()  {
		playerModule.init(this);
		moverModule.init(this);
		workerModule.init(this);
		workerModule.start();
	}
	
	
	public void put(BMOrder order) {
		workerModule.put(order);
	}
	
	
	public Map getMap() {
		return map;
	}
	
	public String getMapName() {
		return map.getName();
	}
	
	
	
	/** locks registeredPlayers */
	MapInfo getMapInfo() {
		HashMap<String, GraphicalPlayerStatus> players = new HashMap<String, GraphicalPlayerStatus>();
		for (Player player : registeredPlayers.keySet()) {
			GraphicalPlayerStatus graphStatus = player.getGraphicalPlayerStatus();
			players.put(player.getName(), graphStatus);
		}
		return new MapInfo(map, players);
	}

	@Override
	public String toString() {
		return "MapManager [map=" + map.getName() + "]";
	}
}