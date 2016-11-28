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
package com.ubershy.streamsis.elements.actions;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.CuteElementContainer;
import com.ubershy.streamsis.elements.checkers.Checker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Confirmed Delayed Actions. <br>
 * This {@link Action} executes a list of {@link Action Actions} after a specified delay in milliseconds. <br>
 * <b>But only if contained {@link Checker} returns <i>true</i> in this moment. </b> <br>
 */
public class ConfirmedDelayedActions extends DelayedActions {

	static final Logger logger = LoggerFactory.getLogger(ConfirmedDelayedActions.class);

	/**
	 * The list of {@link Checker Checkers} <br>
	 * Actually inside this list can be <b>only one</b> Checker. <br>
	 * It's wrapped inside ObservableList just for getChildren() method.
	 */
	@JsonProperty("checker")
	private ObservableList<Checker> checker = FXCollections.observableArrayList();
	
	/** The list of Actor's External children needed just for {@link #getChildren()} method. */
	@JsonIgnore
	protected ObservableList<CuteElement> children = generateExternalChildrenList();
	
	public ConfirmedDelayedActions() {
	}

	private ObservableList<CuteElement> generateExternalChildrenList() {
		CuteElementContainer<Action> actionsContainer = new CuteElementContainer<Action>(actions,
				"Actions container", AddableChildrenTypeInfo.ACTION,
				MaxAddableChildrenCount.INFINITY, MinAddableChildrenCount.ONE, "Actions");
		CuteElementContainer<Checker> checkersContainer = new CuteElementContainer<Checker>(checker,
				"Checker container", AddableChildrenTypeInfo.CHECKER, MaxAddableChildrenCount.ONE,
				MinAddableChildrenCount.ONE, "Checker");
		return FXCollections.observableArrayList(checkersContainer, actionsContainer);
	}

	/**
	 * Instantiates a new DelayedActions. <b>Used only during deserialization.</b>
	 *
	 * @param actions
	 *            the list of {@link Action Actions} to execute with delay
	 * @param checker
	 *            the {@link Checker} to confirm before actual execution
	 * @param delay
	 *            the execution delay in milliseconds
	 */
	@JsonCreator
	private ConfirmedDelayedActions(@JsonProperty("actions") ArrayList<Action> actions,
			@JsonProperty("delay") int delay, @JsonProperty("checker") ArrayList<Checker> checker) {
		super(actions, delay);
		this.checker.setAll(checker);
	}

	/**
	 * Instantiates a new DelayedActions.
	 *
	 * @param actions
	 *            the list of {@link Action Actions} to execute with delay
	 * @param checker
	 *            the {@link Checker} to confirm before actual execution
	 * @param delay
	 *            the execution delay in milliseconds
	 */
	public ConfirmedDelayedActions(ArrayList<Action> actions, int delay, Checker checker) {
		super(actions, delay);
		this.checker = FXCollections.observableArrayList(checker);
	}

	@Override
	public ObservableList<CuteElement> getChildren() {
		return children;
	}

	@Override
	public void init() {
		super.init();
	}

	@Override
	protected void onDelayedExecute() {
		if (checker.get(0).check()) {
			super.onDelayedExecute();
		}
	}
	
	@JsonIgnore
	@Override
	public MaxAddableChildrenCount getMaxAddableChildrenCount() {
		return MaxAddableChildrenCount.UNDEFINEDORZERO;
	}
	
}
