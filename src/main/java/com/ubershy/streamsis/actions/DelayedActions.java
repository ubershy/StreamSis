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
import com.ubershy.streamsis.project.AbstractCuteNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Delayed Actions. <br>
 * This {@link Action} executes a list of {@link Action Actions} after a specified delay in milliseconds.
 */
public class DelayedActions extends AbstractCuteNode implements Action {

	static final Logger logger = LoggerFactory.getLogger(DelayedActions.class);

	/** The list of {@link Action Actions} to execute with delay. */
	@JsonProperty("actions")
	protected ObservableList<Action> actions = FXCollections.observableArrayList();

	/** The execution delay. */
	@JsonProperty("delay")
	protected int delay = 0;

	public DelayedActions() {
	}

	/**
	 * Instantiates a new DelayedActions.
	 *
	 * @param actions
	 *            the list of {@link Action Actions} to execute with delay
	 * @param delay
	 *            the execution delay
	 */
	@JsonCreator
	public DelayedActions(@JsonProperty("actions") ArrayList<Action> actions, @JsonProperty("delay") int delay) {
		this.actions.setAll(actions);
		this.delay = delay;
	}

	@Override
	public void execute() {
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						elementInfo.setBooleanResult(false);
						return;
					}
					onDelayedExecute();
					elementInfo.setBooleanResult(true);
				}
			}).start();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ObservableList getChildren() {
		return actions;
	}

	@JsonIgnore
	@Override
	public AddableChildrenTypeInfo getAddableChildrenTypeInfo() {
		return AddableChildrenTypeInfo.ACTION;
	}
	
	@JsonIgnore
	@Override
	public MaxAddableChildrenCount getMaxAddableChildrenCount() {
		return MaxAddableChildrenCount.INFINITY;
	}

	public int getDelay() {
		return delay;
	}

	@Override
	public void init() {
		elementInfo.setAsReadyAndHealthy();
		if (actions != null) {
			if (!actions.isEmpty()) {
				for (Action action : actions) {
					if (action != null) {
						action.init();
						if (action.getElementInfo().isBroken()) {
							elementInfo.setAsBroken("Contained Action " + action.getClass().getSimpleName() + " is broken");
						}
					} else {
						elementInfo.setAsBroken("Contained Action is not defined");
					}
				}
			} else {
				elementInfo.setAsBroken("No Actions are assigned");
			}
		} else {
			elementInfo.setAsBroken("Contained list of Actions is not defined");
		}
	}

	/**
	 * The method for executing list of {@link Action Actions}. <br>
	 * It is also used by subclasses of {@link DelayedActions}.
	 */
	protected void onDelayedExecute() {
		for (Action action : actions) {
			action.execute();
		}
	}
	
	public void setDelay(int delay) {
		this.delay = delay;
	}

}
