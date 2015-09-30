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
package com.ubershy.streamsis.actions;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ubershy.streamsis.checkers.Checker;
import com.ubershy.streamsis.project.CuteNode;
import com.ubershy.streamsis.project.CuteNodeContainer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Confirmed Delayed Actions. <br>
 * This {@link Action} executes a list of {@link Action Actions} after a specified delay in milliseconds. <br>
 * <b>But only if contained {@link Checker} returns <i>true</i> in this moment. </b> <br>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "actnType")
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
	protected ObservableList<CuteNode> children = generateExternalChildrenList();
	
	public ConfirmedDelayedActions() {
	}

	/**
	 * @return
	 */
	private ObservableList<CuteNode> generateExternalChildrenList() {
		CuteNodeContainer actionsContainer = new CuteNodeContainer(actions, "Actions", AddableChildrenTypeInfo.ACTION, MaxAddableChildrenCount.INFINITY);
		CuteNodeContainer checkersContainer = new CuteNodeContainer(checker, "Checker", AddableChildrenTypeInfo.CHECKER, MaxAddableChildrenCount.ONE);
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
	private ConfirmedDelayedActions(@JsonProperty("actions") ArrayList<Action> actions, @JsonProperty("delay") int delay,
			@JsonProperty("checker") ArrayList<Checker> checker) {
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
	public ObservableList<CuteNode> getChildren() {
		return children;
	}

	@Override
	public void init() {
		super.init();
		if (checker != null) {
			if (!checker.isEmpty()) {
				if (checker.get(0) != null) {
					checker.get(0).init();
					if (checker.get(0).getElementInfo().isBroken()) {
						elementInfo.setAsBroken("Contained " + checker.get(0).getClass().getSimpleName() + " is broken");
					}
				} else {
					elementInfo.setAsBroken("Checker is not defined");
				}
			} else {
				elementInfo.setAsBroken("Checker is not assigned");
			}
		} else {
			elementInfo.setAsBroken("Contained list of Checkers is not defined");
		}

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
