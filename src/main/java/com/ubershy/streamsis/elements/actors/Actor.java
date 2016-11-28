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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.collections.ObservableList;

import com.ubershy.streamsis.StreamSis;
import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.ElementInfo;
import com.ubershy.streamsis.elements.actions.Action;
import com.ubershy.streamsis.elements.checkers.Checker;

/**
 * Actor takes the <b>most important role</b> in {@link StreamSis} functioning. <br>
 * Actor is an entity that has one or more {@link Action Actions} (<b>his hands \ /</b>) and single {@link Checker} (<b>his eyes o_O</b>). <br>
 * <p>
 * Actor regularly checks if {@link Checker#check()} returns true. <br>
 * If {@link Checker#check()} is true, the Actor is switching On and starting to execute his {@link Action onActions}. <br>
 * If {@link Checker#check()} is false, the Actor is switching Off and starting to execute his {@link Action offActions}. <br>
 * Based on how Actor is configured, {@link Action Actions} will repeat executing with specified time interval or will not repeat executing at all. <br>
 * <p>
 * Actor extends {@link CuteElement} interface.
 * <p>
 * Note: please consider that Actor's Switch (that can be On or Off) <b>is not</b> related to {@link ElementInfo ElementInfo's} isEnabled property.
 */
public interface Actor extends CuteElement {

	/**
	 * Adds the {@link Action} to Actor's On Actions.
	 *
	 * @param action
	 *            the {@link Action}
	 */
	public void addOffAction(Action action);

	/**
	 * Adds the {@link Action} to Actor's On Actions.
	 *
	 * @param action
	 *            the {@link Action}
	 */
	public void addOnAction(Action action);

	/**
	 * Performs {@link Checker#check()}. <br>
	 * And based on result, it starts or stops Actor's {@link ActorActionsRepeatingService ActorActionsRepeatingServices}
	 */
	public void checkAndAct();

	/**
	 * Gets Actor's check interval property. <br>
	 * It's the interval of time in milliseconds between Actor's checks.
	 *
	 * @return the integer check interval property
	 */
	public IntegerProperty checkIntervalProperty();

	/**
	 * Clears Actor's Off Actions.
	 */
	public void clearOffActions();

	/**
	 * Clears Actor's On Actions.
	 */
	public void clearOnActions();

	/**
	 * Execute Actor's Off Actions once.
	 */
	public void executeOffActions();

	/**
	 * Execute Actor's On Actions once.
	 */
	public void executeOnActions();

	/**
	 * Gets the Actor's {@link ActorCheckerService}.
	 *
	 * @return the Actor's check service
	 */
	public ActorCheckerService getActorCheckerService();

	/**
	 * Gets the Actor's {@link Checker}.
	 *
	 * @return the {@link Checker}
	 */
	public Checker getChecker();

	/**
	 * Gets the Actor's check interval. <br>
	 * It's the interval of time in milliseconds between Actor's checks.
	 *
	 * @return the check interval in milliseconds
	 */
	public int getCheckInterval();
	
	/**
	 * Sets the Actor's check interval. <br>
	 * It's the interval of time in milliseconds between Actor's checks.
	 * 
	 * @param interval
	 *            The interval in milliseconds to wait between checks.
	 */
	public void setCheckInterval(int interval);

	/**
	 * Gets the Actor's {@link ActorActionsRepeatingService} for switching Off.
	 *
	 * @return the Actor's Off {@link ActorActionsRepeatingService}
	 */
	public ActorActionsRepeatingService getOffActionRepeatingService();

	/**
	 * Gets the list of Actor's Off Actions.
	 *
	 * @return Actor's Off Actions.
	 */
	public ObservableList<Action> getOffActions();

	/**
	 * Gets the Actor's {@link ActorActionsRepeatingService} for switching On.
	 *
	 * @return the Actor's On {@link ActorActionsRepeatingService}
	 */
	public ActorActionsRepeatingService getOnActionRepeatingService();

	/**
	 * Gets the list of Actor's On Actions.
	 *
	 * @return Actor's On Actions.
	 */
	public ObservableList<Action> getOnActions();

	/**
	 * Gets the Actor's repeat interval. <br>
	 * It's the interval of time in milliseconds that specify how often Actor should repeat executing his {@link Action Actions}.
	 * 
	 * @return the repeat interval in milliseconds
	 */
	public int getRepeatInterval();
	
	/**
	 * Sets the Actor's repeat interval. <br>
	 * It's the interval of time in milliseconds that specify how often Actor should repeat executing his {@link Action Actions}.
	 * 
	 * @param interval
	 *            The interval in milliseconds to use for repeating Actions.
	 */
	public void setRepeatInterval(int interval);

	/**
	 * Tells if Actor will repeat executing "Off" Actions when it's switched off.
	 *
	 * @return true, if Actor will repeat "Off" Actions.
	 */
	public boolean getDoOffRepeat();

	/**
	 * Tells if Actor will repeat executing "On" Actions when it's switched on.
	 *
	 * @return true, if Actor will repeat "On" Actions.
	 */
	public boolean getDoOnRepeat();
	
	/**
	 * Tells if Actor will repeat executing "On" Actions when it's switched on.
	 *
	 * @return BooleanProperty
	 */
	public BooleanProperty doOnRepeatProperty();
	
	/**
	 * Tells if Actor will repeat executing "Off" Actions when it's switched off.
	 *
	 * @return BooleanProperty
	 */
	public BooleanProperty doOffRepeatProperty();

	/**
	 * Gets Actor's isSwitchOn property
	 *
	 * @return the boolean Actor's isSwitchOn property
	 */
	BooleanProperty isSwitchOnProperty();

	/**
	 * Gets the Actor's repeat interval property. <br>
	 * It's the interval of time in milliseconds that specify how often Actor should repeat executing his {@link Action Actions}.
	 * 
	 * @return the integer repeat interval property
	 */
	public IntegerProperty repeatIntervalProperty();

	/**
	 * Sets the Actor's {@link Checker}.
	 *
	 * @param checker
	 *            the new {@link Checker}
	 */
	public void setChecker(Checker checker);

	/**
	 * Tells if Actor will repeat executing "Off" Actions when it's switched off.
	 *
	 * @param doOffRepeat
	 *            set as true is you want Actor to repeat executing "Off" Actions.
	 */
	public void setDoOffRepeat(boolean doOffRepeat);

	/**
	 * Tells if Actor will repeat executing "On" Actions when it's switched on.
	 *
	 * @param doOnRepeat
	 *            set as true is you want Actor to repeat executing "On" Actions.
	 */
	public void setDoOnRepeat(boolean doOnRepeat);

	/**
	 * Start the Actor. <br>
	 * Internally Actor starts his {@link ActorCheckerService}
	 */
	public void start();

	/**
	 * Stop the Actor. Internally Actor stops his {@link ActorCheckerService}
	 */
	public void stop();
	
	/**
	 * Counts this Actor and Actor's children ( {@link CuteElement}s inside) recursively.
	 */
	public int countActorAndChildrenRecursivelyWithoutContainersOnTop();

}
