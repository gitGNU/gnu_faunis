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
package clientSide.animation;

import java.util.Timer;
import java.util.TimerTask;

import common.Logger;
import common.enums.AniEndType;
import common.modules.timerModule.AnimatorModule;
import common.modules.timerModule.ModTimer;

public class Animator<ANIMATEABLE extends Animateable> implements ModTimer {
	protected Object runningMutexKey;
	protected boolean stopRunning;
	protected AnimatorModule<ANIMATEABLE, ?, ?> parent;
	protected ANIMATEABLE animateable;
	private Timer timer;
	private AnimatingTask animatingTask;
	protected AniEndType endType;
	private long interval;
	protected int maxFrameIndex;
	
	/** Creates an Animator, but doesn't start it yet. */
	public Animator(AnimatorModule<ANIMATEABLE, ?, ?> parent, ANIMATEABLE animateable, long delay,
			AniEndType endType, int maxFrameIndex) {
		this.parent = parent;
		this.animateable = animateable;
		this.timer = new Timer();
		this.animatingTask = new AnimatingTask();
		this.runningMutexKey = new Object();
		this.stopRunning = false;
		this.endType = endType;
		this.interval = delay;
		this.maxFrameIndex = maxFrameIndex;
	}
	
	/** Starts the Animator. Has to be explicitely called,
	 * as that isn't done automatically.<br/>
	 * HINT: Doesn't register this Animator at the Mapman, because that must
	 * already happen while creating the Animator. */
	@Override
	public void start() {
		this.timer.scheduleAtFixedRate(animatingTask, 0, interval);
	}
	
	public ANIMATEABLE getAnimateable() {
		return animateable;
	}
	
	/** asserts that at the method end, no
	 * timerTask is running. */
	@Override
	public void stop() {
		this.timer.cancel();
		synchronized(runningMutexKey) {
			stopRunning = true;
		}
		synchronized(animateable) {
			animateable.resetAnimation();
		}
	}
	
	@Override
	public void stopAndUnregister() {
		this.stop();
		parent.unregisterModTimer(animateable);
	}
	
	protected class AnimatingTask extends TimerTask {
		@Override
		public void run() {
			// increase animateable's frame index and
			// unregister at parent when animation is over
			Object list[] = parent.getSynchroStuffForModTimerRunOrUnregister(animateable);
			assert(list != null);
			parent.parent().sync().multisync(list, new Runnable() {
				@Override
				public void run(){
					synchronized(runningMutexKey) {
						if (stopRunning)
							return;
						if (isAnimationFinished()) {
							stopAndUnregister();
						} else {
							animate();
						}
					}
				}
			});
		}
		
		protected boolean isAnimationFinished() {
			int frame = animateable.getFrame();
			if (frame >= maxFrameIndex && endType == AniEndType.revert)
				return true;
			return false;
		}
		
		protected void animate() {
			Object[] animationSynchroStuff = parent.getSynchroStuffForModTimerRunOrUnregister(animateable);
			parent.parent().sync().multisync(animationSynchroStuff, new Runnable() {
				@Override
				public void run() {
					synchronized(runningMutexKey) {
						if (stopRunning)
							return;
						int frame = animateable.getFrame();
						switch (endType) {
							case repeat:
								int oldFrame = frame;
								frame++;
								if (frame > maxFrameIndex)
									frame = 0;
								Logger.log("Increase frame from "+oldFrame+" to "+frame);
								animateable.setFrame(frame);
								break;
							case revert:
							case end:
								if (frame < maxFrameIndex) {
									Logger.log("Increase frame from "+frame+" to "+(frame+1));
									animateable.setFrame(frame+1);
								}
								break;
						}
					}
				}
			});
		}
	}
}
