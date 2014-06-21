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

import java.io.ObjectInputStream;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import mux.MuxObjectInputStream;
import mux.MuxObjectInputStream.BlockingQueueRunnable;
import clientSide.animation.AnimationData;
import serverSide.Result;
import serverSide.butler.Butler;
import serverSide.butlerToMapmanOrders.*;
import serverSide.mapmanToButlerOrders.MBChatMessageOrder;
import serverSide.mapmanToButlerOrders.MBErrorMessageOrder;
import serverSide.mapmanToButlerOrders.MBMapInfoOrder;
import serverSide.player.Player;
import common.Logger;
import common.MapInfo;
import common.enums.AniEndType;
import common.enums.CharacterClass;
import common.graphics.GraphicsContentManager;
import common.modules.workerModule.WorkerModule;
import common.movement.Path;
import common.movement.PathFactory;


public class MapManagerWorkerModule extends WorkerModule<MapManager> {

	@SuppressWarnings("rawtypes")
	public MapManagerWorkerModule(BlockingQueue queue, String name) {
		super(
			new MuxObjectInputStream(
				name, 16, true, new ObjectInputStream[0], new Class[0],
				new BlockingQueue[] {queue}, new Class[] {BMOrder.class}),
			  "MapmanThread[name="+name+"]");
	}
	
	@SuppressWarnings("unchecked")
	public void put(BMOrder order) {
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
	protected void handleMessage(Object order) {
		assert(order != null);
		if (order instanceof BMMapInfoOrder) {
			MapInfo mapInfo = parent.getMapInfo();
			((BMMapInfoOrder) order).getSource().put(new MBMapInfoOrder(parent, mapInfo));
		}
		else if (order instanceof BMRegisterOrder)
			registerPlayer((BMRegisterOrder) order);
		else if (order instanceof BMUnregisterOrder)
			unregisterPlayer((BMUnregisterOrder) order);
		else if (order instanceof BMMoveOrder)
			movePlayer((BMMoveOrder) order);
		else if (order instanceof BMChatMessageOrder)
			bMChatMessageOrder((BMChatMessageOrder) order);
		else if (order instanceof BMTriggerAnimationOrder)
			bMTriggerAnimationOrder((BMTriggerAnimationOrder) order);
		// TODO Handle further orders
	}
	
	void registerPlayer(BMRegisterOrder order) {
		Player player = order.getPlayer();
		Butler source = order.getSource();
		parent.playerModule.add(player, source, order);
		System.out.println("dealt with BMRegisterOrder.");
		order.setDone(true);
	}
	
	void unregisterPlayer(BMUnregisterOrder order) {
		Player player = order.getPlayer();
		parent.playerModule.remove(player, order);
		order.setDone(true);
	}
	
	private void bMTriggerAnimationOrder(BMTriggerAnimationOrder order) {
		// determine if animation is valid for given player type:
		Player player = order.getPlayer();
		CharacterClass type = player.getType();
		String animation = order.getAnimation();
		if (animation == null || animation.equals("")) {
			// if an animation had been stored, delete it and notify all butlers
			parent.playerModule.deleteAnimation(player);
			return;
		}
		GraphicsContentManager contentManager = parent.parent.getGraphicsContentManager();
		Set<String> animations = contentManager.getAvailableAnimations(type);
		if (!animations.contains(animation)) {
			order.getSource().put(new MBErrorMessageOrder(parent, "Animation is invalid!"));
			return;
		}
		// determine animation's AniEndType:
		AnimationData animationData = contentManager.getAnimationData(type, animation);
		assert(animationData != null);
		AniEndType endType = animationData.endType;
		if (endType != AniEndType.revert) {
			// store animation in player and notify all
			parent.playerModule.storeAnimation(player, animation);
		} else {
			// fire and forget
			parent.playerModule.fireAnimation(player, animation);
		}
	}

	
	private void bMChatMessageOrder(BMChatMessageOrder order) {
		Logger.log("Mapman forwards chat message");
		order.setDone(true);
		String playerName = order.getToName();
		if (playerName == null || playerName.equals("")) {
			// broadcast message to all players of this mapman
			parent.playerModule.notifyAll(new MBChatMessageOrder(parent, order));
			return;
		}
		// else find the butler that corresponds to playerName
		Player player;
		player = parent.playerNameToPlayer.get(playerName);
		if (player != null) {
			Butler butler;
			butler = parent.registeredPlayers.get(player);
			butler.put(new MBChatMessageOrder(parent, order));
		} else {
			Result<Butler> butlerQuery = parent.parent.getButlerByPlayerName(playerName);
			if (!butlerQuery.successful()) {
				String error = "Couldn't deliver message to given player.";
				order.getSource().put(new MBErrorMessageOrder(parent, error));
				return;
			}
			Butler butler = butlerQuery.getResult();
			// Since the butler will only listen to his mapman,
			// we have to show him that it's okay by setting
			// the source to null:
			butler.put(new MBChatMessageOrder(null, order));
		}
	}
	
	private void movePlayer(final BMMoveOrder order) {
		assert(order != null);
		final Player player = order.getPlayer();
		assert(parent.registeredPlayers.containsKey(player));
		// stop possible earlier movement:
		parent.moverModule.tryStop(player);
		// stop possible earlier animation:
		parent.playerModule.deleteAnimation(player);
		// if we are already at our target then return:
		if (order.getXTarget() == player.getX()
			&& order.getYTarget() == player.getY())
			return;
		// Start Movement:
		// build path and set its reference in the player object:
		Path path = PathFactory.createAirlinePath(player.getX(), player.getY(),
				order.getXTarget(), order.getYTarget());
		player.setPath(path);
		parent.moverModule.tryStart(player, null);
		order.setDone(true);
	}
}
