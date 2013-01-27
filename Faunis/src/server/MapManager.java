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
package server;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import client.AnimationData;
import communication.GraphicalPlayerStatus;
import communication.GraphicsContentManager;
import communication.Link;
import communication.Map;
import communication.MapInfo;
import communication.enums.AniEndType;
import communication.enums.CharacterClass;
import communication.movement.Moveable;
import communication.movement.Mover;
import communication.movement.MoverManager;
import communication.movement.MovingTask;
import communication.movement.Path;
import communication.movement.PathFactory;
import communication.movement.RoughMovingTask;
import server.butlerToMapmanOrders.*;
import server.mapmanToButlerOrders.*;

/** The map manager manages everything that happens on a map. There is exactly
 * one map manager for each map in the game. All players / butlers on a map have
 * to be registered at the map manager of that map. */
public class MapManager implements MoverManager {
	private MainServer parent;
	private Map map;
	private Thread thread;
	private Runnable runnable;
	protected BlockingQueue<BMOrder> orders;
	private HashMap<Player, Butler> registeredPlayers;
	private HashMap<String, Player> playerNameToPlayer;
	private HashMap<Player, Mover> movingPlayers;
	
	public MapManager(MainServer parent, Map map) {
		this.parent = parent;
		this.map = map;
		this.registeredPlayers = new HashMap<Player, Butler>();
		this.playerNameToPlayer = new HashMap<String, Player>();
		this.movingPlayers = new HashMap<Player, Mover>();
		orders = new ArrayBlockingQueue<BMOrder>(50);
		runnable = new MapManRunnable(this);
		thread = new Thread(runnable);
		thread.start();
	}
	
	/** locks registeredPlayers, movingPlayers, player */
	private void tryStopMovement(Player player) {
		synchronized(registeredPlayers) {
			synchronized(movingPlayers) {
				synchronized(player) {
					Mover mover = movingPlayers.get(player);
					if (mover != null) {
						mover.stopAndUnregister();
					}
				}
			}
		}
	}
	
	/** If a mover wants to stop movement, it has to call
	 * MapManager.unregisterMover(). Unfortunately, that function locks
	 * registeredPlayers and movingPlayers AFTER the mover has already locked
	 * stuff like moveable. Solution:
	 * => Mover fetches these objects by calling this function and
	 * locks them beforehand in the right order. */
	public Object[] getSynchroStuffForMoverStop() {
		return new Object[] {registeredPlayers, movingPlayers};
	}
	
	// nothing to lock on the server side during movement
	public Object getSynchroStuffForMovement() {
		return new Object();
	}
	
	/** locks movingPlayers; registeredPlayers, moveable*/
	@Override
	public void unregisterMover(Moveable moveable) {
		assert(moveable instanceof Player);
		Player movingPlayer = (Player) moveable;
		synchronized(movingPlayers) {
			movingPlayers.remove(movingPlayer);
		}
		// TODO: Check if the player has landed on a link to another map
		synchronized(registeredPlayers) {
			Link link;
			synchronized(movingPlayer) {
				link = map.getOutgoingLink(movingPlayer.getX(), movingPlayer.getY());
				if (link != null) {
					if (link.getTargetMap().equals(this.map.getName())) {
						// just move the player to the targetField
						link.move(movingPlayer);
					} else {
						// the player must be moved to another mapman:
						// -> inform the butler so that he can apply the change
						registeredPlayers.get(movingPlayer).put(
							new MBCharAtOtherMapmanOrder(this, link));
					}
				}
			}
		}
	}
	
	public void put(BMOrder order) {
		try {
			orders.put(order);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException("Couldn't pass the order to the mapman!");
		}
	}
	
	public Map getMap() {
		return map;
	}
	
	public String getMapName() {
		return map.getName();
	}
	
	protected void handleButlerOrder(BMOrder order) {
		assert(order != null);
		if (order instanceof BMMapInfoOrder) {
			MapInfo mapInfo = getMapInfo();
			order.getSource().put(new MBMapInfoOrder(this, mapInfo));
		}
		else if (order instanceof BMRegisterOrder)
			registerPlayer((BMRegisterOrder) order);
		else if (order instanceof BMUnregisterOrder)
			unregisterPlayer((BMUnregisterOrder) order);
		else if (order instanceof BMMoveOrder)
			movePlayer((BMMoveOrder) order);
		else if (order instanceof BMChatMessageOrder)
			bMChatMessageOrder((BMChatMessageOrder) order);
		else if (order instanceof BMTriggerEmoteOrder)
			bMTriggerEmoteOrder((BMTriggerEmoteOrder) order);
		// TODO Handle further orders
	}
	
	private void bMTriggerEmoteOrder(BMTriggerEmoteOrder order) {
		// TODO
		// determine if emote is valid for given player type:
		Player player = order.getPlayer();
		CharacterClass type = player.getType();
		String emote = order.getEmote();
		if (emote == null || emote.equals("")) {
			// if an emote had been stored, delete it and notify all butlers
			deleteEmote(player);
			return;
		}
		GraphicsContentManager contentManager = parent.getGraphicsContentManager();
		Set<String> emotes = contentManager.getAvailableAnimations(type);
		if (!emotes.contains(emote)) {
			order.getSource().put(new MBErrorMessageOrder(this, "Emote is invalid!"));
			return;
		}
		// determine emote's AniEndType:
		AnimationData animationData = contentManager.getAnimationData(type, emote);
		assert(animationData != null);
		AniEndType endType = animationData.endType;
		if (endType != AniEndType.revert) {
			// store emote in player and notify all
			storeEmote(player, emote);
		} else {
			// fire and forget
			fireEmote(player, emote);
		}
	}
	
	/** locks registeredPlayers, player */
	private void fireEmote(Player player, String emote) {
		synchronized(registeredPlayers) {
			synchronized(player) {
				GraphicalPlayerStatus status = player.getGraphicalPlayerStatus();
				status.currentEmote = emote;
				notifyAllCharChanged(player.getName(), status);
			}
		}
	}

	/** locks registeredPlayers, player */
	private void deleteEmote(Player player) {
		synchronized(registeredPlayers) {
			synchronized(player) {
				if (player.getCurrentEmote() != null) {
					player.setCurrentEmote(null);
					notifyAllCharChanged(player.getName(), player.getGraphicalPlayerStatus());
				}
			}
		}
	}
	
	/** locks registeredPlayers, player */
	private void storeEmote(Player player, String emote) {
		synchronized(registeredPlayers) {
			synchronized(player) {
				player.setCurrentEmote(emote);
				notifyAllCharChanged(player.getName(), player.getGraphicalPlayerStatus());
			}
		}
	}
	
	/** locks registeredPlayers */
	private void notifyAllCharChanged(String playerName, GraphicalPlayerStatus status) {
		synchronized(registeredPlayers	) {
			for (Butler butler : registeredPlayers.values()) {
				butler.put(new MBChangeCharOrder(this, playerName,
						status));
			}
		}
	}
	
	private void bMChatMessageOrder(BMChatMessageOrder order) {
		System.out.println("Mapman forwards chat message");
		String playerName = order.getToName();
		if (playerName == null || playerName.equals("")) {
			// broadcast message to all players of this mapman
			synchronized(registeredPlayers) {
				for (Butler butler : registeredPlayers.values()) {
					butler.put(new MBChatMessageOrder(this, order));
				}
			}
			return;
		}
		// else find the butler that corresponds to playerName
		Player player;
		synchronized(playerNameToPlayer) {
			player = playerNameToPlayer.get(playerName);
		}
		if (player != null) {
			Butler butler;
			synchronized(registeredPlayers	) {
				butler = registeredPlayers.get(player);
			}
			butler.put(new MBChatMessageOrder(this, order));
		} else {
			Result<Butler> butlerQuery = parent.getButlerByPlayerName(playerName);
			if (!butlerQuery.successful()) {
				String error = "Couldn't deliver message to given player.";
				order.getSource().put(new MBErrorMessageOrder(this, error));
				return;
			}
			Butler butler = butlerQuery.getResult();
			// Since the butler will only listen to his mapman,
			// we have to show him that it's okay by setting
			// the source to null:
			butler.put(new MBChatMessageOrder(null, order));
		}
	}
	
	private void movePlayer(BMMoveOrder order) {
		assert(order != null);
		Player player = order.getPlayer();
		synchronized (registeredPlayers) {
			assert(registeredPlayers.containsKey(player));
		}
		// stop possible earlier movement:
		tryStopMovement(player);
		// stop possible earlier emote:
		deleteEmote(player);
		// if we are already at our target then return:
		if (order.getXTarget() == player.getX()
			&& order.getYTarget() == player.getY())
			return;
		String playerName;
		GraphicalPlayerStatus status;
		// build path and set its reference in the player object:
		synchronized(player) {
			Path path = PathFactory.createAirlinePath(player.getX(), player.getY(),
					order.getXTarget(), order.getYTarget());
			player.setPath(path);
			playerName = player.getName();
			status = player.getGraphicalPlayerStatus();
		}
		// create new mover for player and add it:
		Mover mover;
		synchronized(movingPlayers) {
			mover = new Mover(this, player, 500); // TODO Zeiteinheit
			MovingTask movingTask = new RoughMovingTask(mover, player);
			mover.setMovingTask(movingTask);
			movingPlayers.put(player, mover);
		}
		// inform every registered butler about the change:
		synchronized(registeredPlayers) {
			notifyAllCharChanged(playerName, status);
		}
		mover.start();
		// TODO
	}
	
	
	private void registerPlayer(BMRegisterOrder order) {
		assert(order != null);
		synchronized(registeredPlayers) {
			synchronized(playerNameToPlayer) {
				Player player = order.getPlayer();
				String playername = player.getName();
				Butler butler = order.getSource();
				synchronized(player) {
					assert(!player.hasPath());	
				}
				assert(!playerNameToPlayer.containsKey(playername));
				playerNameToPlayer.put(playername, player);
				assert(!registeredPlayers.containsKey(player));
				registeredPlayers.put(player, butler);
				if (order.getAddPlayerMapEntry()) {
					synchronized(player) {
						assert(player.getMapName() == null);
						player.setMapName(this.map.getName());
					}
				}
				// send map information to the new player's butler:
				MapInfo mapInfo = getMapInfo();
				butler.put(new MBMapInfoOrder(this, mapInfo));
				// send information about the new player to all other butlers:
				for (Butler butler2:registeredPlayers.values()) {
					if (butler2 != butler)
						butler2.put(new MBAddCharOrder(this, playername,
						  player.getGraphicalPlayerStatus()));
					// TODO
				}
			}
		}
	}
	
	/** locks registeredPlayers, playerNameToPlayer, movingPlayers, player */
	private void unregisterPlayer(BMUnregisterOrder order) {
		assert(order != null);
		synchronized(registeredPlayers) {
			synchronized(playerNameToPlayer) {
				Player player = order.getPlayer();
				String playername = player.getName();
				assert(playerNameToPlayer.containsKey(playername));
				// stop possible earlier movement:
				tryStopMovement(player);
				// stop possible earlier emote:
				deleteEmote(player);
				
				playerNameToPlayer.remove(playername);
				assert(registeredPlayers.containsKey(player));
				registeredPlayers.remove(player);
				if (order.getRemovePlayerMapEntry()) {
					synchronized(player) {
						assert(player.getMapName() != null);
						player.setMapName(null);
					}
				}
				// inform every registered butler about the leave:
				for (Butler butler2:registeredPlayers.values()) {
					butler2.put(new MBRemoveCharOrder(this, playername));
				}
			}
		}
	}
	
	private MapInfo getMapInfo() {
		HashMap<String, GraphicalPlayerStatus> players = new HashMap<String, GraphicalPlayerStatus>();
		synchronized(registeredPlayers) {
			for (Player player:registeredPlayers.keySet()) {
				GraphicalPlayerStatus graphStatus = player.getGraphicalPlayerStatus();
				players.put(player.getName(), graphStatus);
			}
		}
		return new MapInfo(map, players);
	}
	
	
	private class MapManRunnable implements Runnable {
		private MapManager myMapman;

		public MapManRunnable(MapManager parent) {
			this.myMapman = parent;
		}
		
		@Override
		public void run() {
			BMOrder order;
			while (true) {
				order = null;
				try {
					order = myMapman.orders.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (order != null)
					myMapman.handleButlerOrder(order);
			}
		}
		
		
	}
}
