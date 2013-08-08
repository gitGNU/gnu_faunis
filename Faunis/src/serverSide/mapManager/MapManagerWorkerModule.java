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
package serverSide.mapManager;

import java.util.Set;

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
import common.Tools;
import common.enums.AniEndType;
import common.enums.CharacterClass;
import common.graphics.GraphicsContentManager;
import common.modules.workerModule.BlockingQueueWorkerModule;
import common.movement.Path;
import common.movement.PathFactory;

public class MapManagerWorkerModule extends
		BlockingQueueWorkerModule<BMOrder, BMPoisonPillOrder, MapManager> {

	public MapManagerWorkerModule(String threadName,
			boolean notifyTooIfTerminatedPurposely) {
		super(new BMPoisonPillOrder(null), BMPoisonPillOrder.class,
			  threadName, notifyTooIfTerminatedPurposely);
	}

	@Override
	protected void notifyTermination() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void handleMessage(BMOrder order) {
		assert(order != null);
		if (order instanceof BMMapInfoOrder) {
			MapInfo mapInfo = parent.getMapInfo();
			order.getSource().put(new MBMapInfoOrder(parent, mapInfo));
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
		parent.playerModule.add(order.getPlayer(), order.getSource(), order);
	}
	
	void unregisterPlayer(BMUnregisterOrder order) {
		parent.playerModule.remove(order.getPlayer(), order);
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

	
	/** locks registeredPlayers */
	private void bMChatMessageOrder(BMChatMessageOrder order) {
		Logger.log("Mapman forwards chat message");
		String playerName = order.getToName();
		if (playerName == null || playerName.equals("")) {
			// broadcast message to all players of this mapman
			parent.playerModule.notifyAll(new MBChatMessageOrder(parent, order));
			return;
		}
		// else find the butler that corresponds to playerName
		Player player;
		synchronized(parent.playerNameToPlayer) {
			player = parent.playerNameToPlayer.get(playerName);
		}
		if (player != null) {
			Butler butler;
			synchronized(parent.registeredPlayers) {
				butler = parent.registeredPlayers.get(player);
			}
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
		Object[] syncOn1 = parent.moverModule.getSynchroStuffForTryStop(null);
		Object[] syncOn2 = parent.moverModule.getSynchroStuffForTryStart(null);
		parent.sync().multisync(Tools.concat(syncOn1, syncOn2), true, new Runnable() {
			@Override
			public void run() {
				synchronized(player) {
					assert(parent().registeredPlayers.containsKey(player));
					// stop possible earlier movement:
					parent().moverModule.tryStop(player);
					// stop possible earlier animation:
					parent().playerModule.deleteAnimation(player);
					// if we are already at our target then return:
					if (order.getXTarget() == player.getX()
						&& order.getYTarget() == player.getY())
						return;
					// Start Movement:
					// build path and set its reference in the player object:
					Path path = PathFactory.createAirlinePath(player.getX(), player.getY(),
							order.getXTarget(), order.getYTarget());
					player.setPath(path);
					parent().moverModule.tryStart(player, null);
				}
			}
		});
	}
}
