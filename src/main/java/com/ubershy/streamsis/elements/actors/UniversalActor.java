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
package com.ubershy.streamsis.elements.actors;

import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.ConstsAndVars;
import com.ubershy.streamsis.StreamSis;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.elements.actions.Action;
import com.ubershy.streamsis.elements.checkers.Checker;
import com.ubershy.streamsis.project.CuteProject;

import javafx.application.Platform;
import javafx.concurrent.Worker.State;

/** The main implementation of {@link Actor} that is used in {@link StreamSis}. */
public class UniversalActor extends AbstractActor implements Actor {

	static final Logger logger = LoggerFactory.getLogger(UniversalActor.class);

	/**
	 * Default way of instantiating a new UniversalActor.
	 *
	 * @param name
	 *            The name of {@link Actor}, must be unique actor name in the {@link CuteProject}.
	 * @param checkInterval
	 *            The {@link #checkInterval} in milliseconds.
	 * @param repeatInterval
	 *            The {@link #repeatInterval} in milliseconds.
	 * @param doOnRepeat
	 *            True to repeat On Actions when Actor is switched On.
	 * @param doOffRepeat
	 *            True to repeat Off Actions when Actor is switched Off.
	 */
	public UniversalActor(String name, int checkInterval, int repeatInterval, boolean doOnRepeat,
			boolean doOffRepeat) {
		this.elementInfo.setName(name);
		this.checkInterval.set(checkInterval);
		this.repeatInterval.set(repeatInterval);
		this.setDoOnRepeat(doOnRepeat);
		this.setDoOffRepeat(doOffRepeat);
	}

	/**
	 * Instantiates a new UniversalActor with known arrays of elements. <br>
	 * This constructor mainly used for deserialization.
	 *
	 * @param name
	 *            The name of {@link Actor}, must be unique actor name in the {@link CuteProject}.
	 * @param checkInterval
	 *            The {@link #checkInterval} in milliseconds.
	 * @param repeatInterval
	 *            The {@link #repeatInterval} in milliseconds.
	 * @param doOnRepeat
	 *            True to repeat On Actions when Actor is switched On.
	 * @param doOffRepeat
	 *            True to repeat Off Actions when Actor is switched Off.
	 * @param checker
	 *            The Actor's {@link Checker}.
	 * @param onActions
	 *            The Actor's On {@link Action}s, which run when Actor is switched On.
	 * @param offActions
	 *            The Actor's Off {@link Action}s, which run when Actor is switched Off.
	 */
	@JsonCreator
	public UniversalActor(@JsonProperty("name") String name,
			@JsonProperty("checkInterval") int checkInterval,
			@JsonProperty("repeatInterval") int repeatInterval,
			@JsonProperty("doOnRepeat") boolean doOnRepeat,
			@JsonProperty("doOffRepeat") boolean doOffRepeat,
			@JsonProperty("checkers") ArrayList<Checker> checkers,
			@JsonProperty("onActions") ArrayList<Action> onActions,
			@JsonProperty("offActions") ArrayList<Action> offActions) {
		this(name, checkInterval, repeatInterval, doOnRepeat, doOffRepeat);
		this.checkers.setAll(checkers);
		this.onActions.setAll(onActions);
		this.offActions.setAll(offActions);
	}

	/**
	 * Instantiates a new UniversalActor with known arrays of elements. <br>
	 * This constructor mainly used for deserialization.
	 *
	 * @param name
	 *            The name of {@link Actor}, must be unique actor name in the {@link CuteProject}.
	 * @param checkInterval
	 *            The {@link #checkInterval} in milliseconds.
	 * @param repeatInterval
	 *            The {@link #repeatInterval} in milliseconds.
	 * @param doOnRepeat
	 *            True to repeat On Actions when Actor is switched On.
	 * @param doOffRepeat
	 *            True to repeat Off Actions when Actor is switched Off.
	 * @param checker
	 *            The Actor's {@link Checker}.
	 * @param onActions
	 *            The Actor's On {@link Action}s, which run when Actor is switched On.
	 * @param offActions
	 *            The Actor's Off {@link Action}s, which run when Actor is switched Off.
	 */
	public UniversalActor(String name, int checkInterval, int repeatInterval, boolean doRepeat,
			boolean doOffRepeat, Checker checker, Action[] onActions, Action[] offActions) {
		this(name, checkInterval, repeatInterval, doRepeat, doOffRepeat,
				(checker == null) ? new ArrayList<Checker>() : Util.singleItemAsList(checker),
				(onActions == null) ? new ArrayList<Action>()
						: new ArrayList<Action>(Arrays.asList(onActions)),
				(offActions == null) ? new ArrayList<Action>()
						: new ArrayList<Action>(Arrays.asList(offActions)));
	}

	@Override
	public void checkAndAct() {
		// Actor breaks during init() if checker is found broken.
		// So if this method executes, we can assume that the checker is not broken.
		boolean state = checkers.get(0).check();
		if (checkers.get(0).getElementInfo().isBroken()) { // broke during execution
			this.stop();
			elementInfo.setAsBroken("Checker broke during execution. "
					+ "The Actor was stopped and set as broken for safety");
			return;
		}
		if (!isSwitchOn.get() && state) {
			logger.info(elementInfo.getName() + ": Target aquired!");
			runEnable();
			isSwitchOn.set(true);
		} else if (isSwitchOn.get() && !state) {
			logger.info(elementInfo.getName() + ": Target lost!");
			runDisable();
			isSwitchOn.set(false);
		}
	}

	@Override
	public void init() {
		initWithoutChildrenStuff();
		if (elementInfo.getName().isEmpty()) {
			elementInfo.setAsBroken("Actor's name must not be empty");
		}
		if (checkInterval.get() < ConstsAndVars.minimumCheckInterval) {
			elementInfo.setAsBroken("Actor check interval must not be less than "
					+ ConstsAndVars.minimumCheckInterval + " ms");
		}
		if (repeatInterval.get() < ConstsAndVars.minimumCheckInterval
				&& (getDoOnRepeat() || getDoOffRepeat())) {
			elementInfo.setAsBroken("Actor repeat interval must not be less than "
					+ ConstsAndVars.minimumCheckInterval + " ms");
		}
		if (checkers.isEmpty()) {
			elementInfo.setAsBroken("You must assign a Checker to Actor before it can work");
		} else {
			checkers.get(0).init();
			if (checkers.get(0).getElementInfo().isBroken()) {
				elementInfo.setAsBroken(
						"The Checker assigned to this Actor is broken. Please fix it first");
			}
		}
		if ((onActions.size() == 0) && (offActions.size() == 0)) {
			elementInfo.setAsBroken("Actor must have at least one Action assigned to it");
		} else {
			for (Action action : onActions) {
				action.init();
				if (action.getElementInfo().isBroken()) {
					elementInfo.setAsBroken("One or more contained On Actions are broken. "
							+ "Please fix them first or delete");
				}
			}
			for (Action action : offActions) {
				action.init();
				if (action.getElementInfo().isBroken()) {
					elementInfo.setAsBroken("One or more contained Off Actions are broken. "
							+ "Please fix them first or delete");
				}
			}
		}
	}

	/**
	 * Starts Off Actions service and stops On Actions service.
	 */
	protected void runDisable() {
		Platform.runLater(() -> {
			onRepeatingService.cancel();
			if (!offActions.isEmpty()) {
				if (offRepeatingService.getState() == State.READY) {
					offRepeatingService.start();
				} else {
					offRepeatingService.restart();
				}
			}
		});
	}

	/**
	 * Starts On Actions service and stops Off Actions service.
	 */
	protected void runEnable() {
		Platform.runLater(() -> {
			offRepeatingService.cancel();
			if (!onActions.isEmpty()) {
				if (onRepeatingService.getState() == State.READY) {
					onRepeatingService.start();
				} else {
					onRepeatingService.restart();
				}
			}
		});
	}

	@Override
	public void start() {
		Platform.runLater(() -> {
			if (actorCheckerService.getState() == State.READY) {
				actorCheckerService.start();
			} else {
				actorCheckerService.restart();
			}
		});
	}

	@Override
	public void stop() {
		Platform.runLater(() -> {
			actorCheckerService.cancel();
			onRepeatingService.cancel();
			offRepeatingService.cancel();
			isSwitchOn.set(false); // reset SwitchOn
		});
	}

}
