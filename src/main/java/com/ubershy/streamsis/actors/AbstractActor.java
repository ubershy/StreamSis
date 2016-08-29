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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.actions.Action;
import com.ubershy.streamsis.checkers.Checker;
import com.ubershy.streamsis.project.AbstractCuteNode;
import com.ubershy.streamsis.project.CuteNode;
import com.ubershy.streamsis.project.CuteNodeContainer;
import com.ubershy.streamsis.project.ElementInfo;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeView;

/**
 * AbstractActor is very abstract.
 */
public abstract class AbstractActor extends AbstractCuteNode implements Actor {

	/**
	 * The list of Checkers assigned to Actor. <br>
	 * Though it can contain only one Checker.
	 * <p>
	 * <b>Why?</b> <br>
	 * Because Actor can have only one Checker assigned. <br>
	 * The list is needed only for {@link #getChildren()} method. This allows easy editing in GUI
	 * for Checker {@link TreeView}.
	 */
	@JsonProperty
	protected final ObservableList<Checker> checkers = FXCollections.observableArrayList();

	/**
	 * "On Actions" <br>
	 * The Actor's list of {@link Action Actions} that execute when the Actor switches On.
	 */
	@JsonProperty
	protected final ObservableList<Action> onActions = FXCollections.observableArrayList();

	/**
	 * "Off Actions" <br>
	 * The Actor's list of {@link Action Actions} that execute when the Actor switches Off.
	 */
	@JsonProperty
	protected final ObservableList<Action> offActions = FXCollections.observableArrayList();

	/** Defines repeat or not On Actions when {@link #isSwitchOn} is true */
	@JsonProperty
	protected boolean doOnRepeat;

	/** Defines repeat or not Off Actions when {@link #isSwitchOn} is false */
	@JsonProperty
	protected boolean doOffRepeat;

	/**
	 * The Actor's Switch that is turning <b>On</b> when Actor's Checker returns true and <b>Off</b>
	 * when Actor's Checker returns false
	 */
	@JsonIgnore
	protected BooleanProperty isSwitchOn = new SimpleBooleanProperty(false);;

	/** It's the interval of time in milliseconds between Actor's checks. */
	@JsonIgnore
	protected IntegerProperty checkInterval = new SimpleIntegerProperty();

	/** It's the interval of time in milliseconds between Actor's Actions executions. */
	@JsonIgnore
	protected IntegerProperty repeatInterval = new SimpleIntegerProperty();

	/**
	 * The Actor's CheckService. <br>
	 * 
	 * @see {@link ActorCheckerService}
	 */
	@JsonIgnore
	protected ActorCheckerService actorCheckerService = new ActorCheckerService(this);

	/**
	 * The Actor's On Actions RepeatingService.
	 * 
	 * @see {@link ActorActionsRepeatingService}
	 */
	@JsonIgnore
	protected ActorActionsRepeatingService onRepeatingService = new ActorActionsRepeatingService(
			this, true);

	/**
	 * The Actor's Off Actions RepeatingService.
	 * 
	 * @see {@link ActorActionsRepeatingService}
	 */
	@JsonIgnore
	protected ActorActionsRepeatingService offRepeatingService = new ActorActionsRepeatingService(
			this, false);

	/** The list of Actor's External children needed just for {@link #getChildren()} method. */
	@JsonIgnore
	protected ObservableList<CuteNode> children = generateExternalChildrenList();

	@JsonIgnore
	@Override
	public BooleanProperty isSwitchOnProperty() {
		return isSwitchOn;
	}

	@JsonIgnore
	@Override
	public ActorActionsRepeatingService getOnActionRepeatingService() {
		return onRepeatingService;
	}

	@JsonIgnore
	@Override
	public ActorActionsRepeatingService getOffActionRepeatingService() {
		return offRepeatingService;
	}

	@Override
	public ElementInfo getElementInfo() {
		return elementInfo;
	}

	@JsonIgnore
	@Override
	public ActorCheckerService getActorCheckerService() {
		return this.actorCheckerService;
	}

	@Override
	@JsonProperty("checkInterval")
	public int getCheckInterval() {
		return checkInterval.get();
	}

	@Override
	public IntegerProperty checkIntervalProperty() {
		return checkInterval;
	}

	@Override
	@JsonProperty("repeatInterval")
	public int getRepeatInterval() {
		return repeatInterval.get();
	}

	@Override
	public IntegerProperty repeatIntervalProperty() {
		return repeatInterval;
	}

	@JsonIgnore
	@Override
	public void setChecker(Checker checker) {
		this.checkers.clear();
		this.checkers.add(checker);
	}

	@JsonIgnore
	@Override
	public Checker getChecker() {
		return this.checkers.get(0);
	}

	@Override
	public void addOnAction(Action action) {
		onActions.add(action);
	}

	@Override
	public void addOffAction(Action action) {
		offActions.add(action);
	}

	@Override
	public void clearOnActions() {
		onActions.clear();
	}

	@Override
	public void clearOffActions() {
		offActions.clear();
	}

	@Override
	public ObservableList<Action> getOnActions() {
		return onActions;
	}

	@Override
	public ObservableList<Action> getOffActions() {
		return offActions;
	}

	@Override
	public void executeOnActions() {
		for (Action a : onActions) {
			if (!executeAction(a, true)) {
				return; // the problem occurred, so lets not execute any other actions
			}
		}
	}

	@Override
	public void executeOffActions() {
		for (Action a : offActions) {
			if (!executeAction(a, false)) {
				return; // the problem occurred, so lets not execute any other actions
			}
		}
	}

	/**
	 * Execute the chosen action. Action that is already broken will just be passed. If Action
	 * breaks during execution, it will break the Actor and stop it.
	 *
	 * @param a
	 *            the Action to execute
	 * @param onOrOff
	 *            tells if this Action is executed under 'On Actions' or 'Off Actions' to generate
	 *            good 'broken' message.
	 * @return true, if the Action executed normally or if is was already broken, <br>
	 *         false, if the Action broke during execution.
	 */
	private boolean executeAction(Action a, boolean onOrOff) {
		String actionsType = onOrOff ? "On" : "Off";
		ElementInfo aInfo = a.getElementInfo();
		if (!aInfo.isBroken()) {
			a.execute();
			if (a.getElementInfo().isBroken()) {
				this.stop();
				elementInfo.setAsBroken(
						"One of the '" + actionsType + " Actions' broke during execution. "
								+ "The Actor was stopped and set as broken for safety");
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isDoOnRepeat() {
		return doOnRepeat;
	}

	@Override
	public boolean isDoOffRepeat() {
		return doOffRepeat;
	}

	@Override
	public void setDoOnRepeat(Boolean doOnRepeat) {
		this.doOnRepeat = doOnRepeat;
	}

	@Override
	public void setDoOffRepeat(Boolean doOffRepeat) {
		this.doOffRepeat = doOffRepeat;
	}

	@Override
	public String toString() {
		return elementInfo.getName();
	}

	@JsonIgnore
	@Override
	public ObservableList<CuteNode> getChildren() {
		return children;
	}

	private ObservableList<CuteNode> generateExternalChildrenList() {
		CuteNodeContainer checkersContainer = new CuteNodeContainer(checkers, "Checker",
				AddableChildrenTypeInfo.CHECKER, MaxAddableChildrenCount.ONE);
		CuteNodeContainer onActionsContainer = new CuteNodeContainer(onActions, "On Actions",
				AddableChildrenTypeInfo.ACTION, MaxAddableChildrenCount.INFINITY);
		CuteNodeContainer offActionsContainer = new CuteNodeContainer(offActions, "Off Actions",
				AddableChildrenTypeInfo.ACTION, MaxAddableChildrenCount.INFINITY);
		ObservableList<CuteNode> result = FXCollections.observableArrayList(checkersContainer,
				onActionsContainer, offActionsContainer);
		return result;
	}
}
