/** 
 * StreamSis
 * Copyright (C) 2015 Eva Balycheva
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ubershy.streamsis.actors;

import com.ubershy.streamsis.ConstsAndVars;

/**
 * This service is used by {@link Actor} to do regular {@link Actor#checkAndAct()} with specified
 * time interval. <br>
 * The state of Checking then can be observed. <br>
 */
public class ActorCheckerService extends javafx.concurrent.Service<Void> {

	/** The Actor to work with. */
	private Actor actor;

	/**
	 * Instantiates a new Actor Checker Service.
	 *
	 * @param actor
	 *            the Actor to work with.
	 */
	public ActorCheckerService(Actor actor) {
		super();
		this.actor = actor;
	}

	@Override
	protected javafx.concurrent.Task<Void> createTask() {
		ProgressTask ct = new ProgressTask();
		return ct;
	}

	public class ProgressTask extends javafx.concurrent.Task<Void> {

		@Override
		public Void call() {
			if (actor.getElementInfo().canWork()) {
				while (true) {
					actor.getElementInfo().setAsWorking();
					// updateProgress(0, 1);
					if (isCancelled()) {
						break;
					}
					// Lets allow user to update checkInterval during working Actor.
					// So lets tune 'sleepTime' value every loop.
					int sleepTime = actor.getCheckInterval();
					if (ConstsAndVars.performChecking) {
						actor.checkAndAct();
					} else {
						// Without a little amount of sleep we will not notice progress update
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							if (isCancelled()) {
								break;
							} else {
								e.printStackTrace();
							}
						}
					}
					// updateProgress(1, 1);
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						if (isCancelled()) {
							break;
						} else {
							e.printStackTrace();
						}
					}
					actor.getElementInfo().setSuccessfulResult();
				}
				actor.getElementInfo().setAsReady();
			}
			// updateProgress(0, 1);
			return null;
		}
	}
}