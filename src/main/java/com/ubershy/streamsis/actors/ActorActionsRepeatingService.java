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
 * This service is used by {@link Actor} to execute Actions. <br>
 * The state of this service then can be observed. <br>
 * If {@link Actor} needs their Actions to repeat, this service repeats execution of Actions regularily with specified time interval. <br>
 * Otherwise it executes Actions once and then stops.
 */
public class ActorActionsRepeatingService extends javafx.concurrent.Service<Void> {

	/** The Actor to work with. */
	private Actor actor;

	/** Do On Actions or Off Actions. */
	private boolean doOnOrOffActions;

	/**
	 * Instantiates a new Action Repeating Service.
	 *
	 * @param The
	 *            Actor to work with.
	 * @param doOnOrOffActions
	 *            Do On Actions or Off Actions. Select <i>True</i> to do On Actions.
	 */
	public ActorActionsRepeatingService(Actor actor, boolean doOnOrOffActions) {
		super();
		this.actor = actor;
		this.doOnOrOffActions = doOnOrOffActions;
	}

	@Override
	protected javafx.concurrent.Task<Void> createTask() {
		ActionsRepeatingProgressTask ct = new ActionsRepeatingProgressTask();
		return ct;
	}

	/**
	 * Task to do inside ActorActionsRepeatingService.
	 */
	public class ActionsRepeatingProgressTask extends javafx.concurrent.Task<Void> {

		@Override
		public Void call() {

			while (true) {
				if (isCancelled()) {
					break;
				}
				if (doOnOrOffActions) {
					if(ConstsAndVars.performActing) {
						actor.executeOnActions();
					}
					if (!actor.getDoOnRepeat())
						break;
				} else {
					if(ConstsAndVars.performActing) {
						actor.executeOffActions();
					}
					if (!actor.getDoOffRepeat())
						break;
				}
				try {
					Thread.sleep(actor.getRepeatInterval());
				} catch (InterruptedException notImportant) {
					if (isCancelled()) {
						break;
					} else {
						notImportant.printStackTrace();
					}
				}
			}
			return null;
		}
	}
}