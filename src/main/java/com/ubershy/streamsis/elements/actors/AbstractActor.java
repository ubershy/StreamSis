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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.ConstsAndVars;
import com.ubershy.streamsis.elements.AbstractCuteElement;
import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.CuteElementContainer;
import com.ubershy.streamsis.elements.ElementInfo;
import com.ubershy.streamsis.elements.actions.Action;
import com.ubershy.streamsis.elements.checkers.Checker;

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
public abstract class AbstractActor extends AbstractCuteElement implements Actor {

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

	/** Defines repeat or not On Actions when {@link #isSwitchOn} is true. Defined by the user. */
	@JsonIgnore
	protected BooleanProperty doOnRepeat = new SimpleBooleanProperty();

	/** Defines repeat or not Off Actions when {@link #isSwitchOn} is false. Defined by the user. */
	@JsonIgnore
	protected BooleanProperty doOffRepeat = new SimpleBooleanProperty();

	/**
	 * The Actor's Switch that is turning <b>On</b> when Actor's Checker returns true and <b>Off</b>
	 * when Actor's Checker returns false.
	 */
	@JsonIgnore
	protected BooleanProperty isSwitchOn = new SimpleBooleanProperty(false);;

	/** It's the interval of time in milliseconds between Actor's checks. <br>
	 * Can't be set less than {@link ConstsAndVars#minimumCheckInterval}. <br>
	 * Defined by the user.
	 */
	@JsonIgnore
	protected IntegerProperty checkInterval = new SimpleIntegerProperty();

	/** 
	 * It's the interval of time in milliseconds between Actor's Actions executions. <br>
	 * Can't be set less than {@link ConstsAndVars#minimumCheckInterval}. <br>
	 * Defined by the user.
	 */
	@JsonIgnore
	protected IntegerProperty repeatInterval = new SimpleIntegerProperty();
	
	/**
	 * The duration of time in milliseconds the Actor should sleep after the first successful check
	 * in a row. <br>
	 * Defined by the user.
	 */
	@JsonIgnore
	protected IntegerProperty sleepOnSuccessDuration = new SimpleIntegerProperty();

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
	protected ObservableList<CuteElement> children = generateExternalChildrenList();

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
	@JsonProperty("checkInterval")
	public void setCheckInterval(int interval) {
		checkInterval.set(interval);
	}

	@Override
	public IntegerProperty checkIntervalProperty() {
		return checkInterval;
	}
	
	@Override
	@JsonProperty("sleepOnSuccessDuration")
	public int getSleepOnSuccessDuration() {
		return sleepOnSuccessDuration.get();
	}
	
	@Override
	@JsonProperty("sleepOnSuccessDuration")
	public void setSleepOnSuccessDuration(int interval) {
		sleepOnSuccessDuration.set(interval);
	}

	@Override
	public IntegerProperty sleepOnSuccessDurationProperty() {
		return sleepOnSuccessDuration;
	}

	@Override
	@JsonProperty("repeatInterval")
	public int getRepeatInterval() {
		return repeatInterval.get();
	}
	
	@Override
	@JsonProperty("repeatInterval")
	public void setRepeatInterval(int interval) {
		repeatInterval.set(interval);
	}
	
	@Override
	public BooleanProperty doOnRepeatProperty() {
		return doOnRepeat;
	}
	
	@Override
	public BooleanProperty doOffRepeatProperty() {
		return doOffRepeat;
	}

	@Override
	public IntegerProperty repeatIntervalProperty() {
		return repeatInterval;
	}

	@JsonIgnore
	@Override
	public void setChecker(Checker checker) {
		this.checkers.clear();
		if (checker != null)
			this.checkers.add(checker);
	}

	@JsonIgnore
	@Override
	public Checker getChecker() {
		if (this.checkers.size() == 0)
			return null;
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
	@JsonProperty("doOnRepeat")
	public boolean getDoOnRepeat() {
		return doOnRepeat.get();
	}

	@Override
	@JsonProperty("doOffRepeat")
	public boolean getDoOffRepeat() {
		return doOffRepeat.get();
	}

	@Override
	@JsonProperty("doOnRepeat")
	public void setDoOnRepeat(boolean doOnRepeat) {
		this.doOnRepeat.set(doOnRepeat);
	}

	@Override
	@JsonProperty("doOffRepeat")
	public void setDoOffRepeat(boolean doOffRepeat) {
		this.doOffRepeat.set(doOffRepeat);
	}

	@Override
	public String toString() {
		return elementInfo.getName();
	}

	@JsonIgnore
	@Override
	public ObservableList<CuteElement> getChildren() {
		return children;
	}

	private ObservableList<CuteElement> generateExternalChildrenList() {
		CuteElementContainer<Checker> checkersContainer = new CuteElementContainer<Checker>(
				checkers, "Checker", AddableChildrenTypeInfo.CHECKER, MaxAddableChildrenCount.ONE,
				MinAddableChildrenCount.ONE, "Checker");
		CuteElementContainer<Action> onActionsContainer = new CuteElementContainer<Action>(
				onActions, "On Actions", AddableChildrenTypeInfo.ACTION,
				MaxAddableChildrenCount.INFINITY, MinAddableChildrenCount.UNDEFINEDORZERO,
				"On Actions");
		CuteElementContainer<Action> offActionsContainer = new CuteElementContainer<Action>(
				offActions, "Off Actions", AddableChildrenTypeInfo.ACTION,
				MaxAddableChildrenCount.INFINITY, MinAddableChildrenCount.UNDEFINEDORZERO,
				"Off Actions");
		ObservableList<CuteElement> result = FXCollections.observableArrayList(checkersContainer,
				onActionsContainer, offActionsContainer);
		return result;
	}
	
	@Override
	public int countActorAndChildrenRecursivelyWithoutContainersOnTop() {
		int count = 1;
		for (CuteElement container: getChildren()) {
			for (CuteElement child: container.getChildren()) {
				count+=countCuteElementRecursively(child);
			}
		}
		return count;
	}
	
	private int countCuteElementRecursively(CuteElement element) {
		int count = 0;
		if (element != null) {
			count++;
			ObservableList<? extends CuteElement> children = element.getChildren();
			if (children != null) {
				for(CuteElement subElement : children) {
					count += countCuteElementRecursively(subElement);
				}
			}
		}
		return count;
	}
}
