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
import com.ubershy.streamsis.elements.AbstractCuteElement;
import com.ubershy.streamsis.elements.CuteElement;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Delayed Actions. <br>
 * This {@link Action} executes a list of {@link Action Actions} after a specified delay in
 * milliseconds.
 */
public class DelayedActions extends AbstractCuteElement implements Action {

	static final Logger logger = LoggerFactory.getLogger(DelayedActions.class);
	
	/** The description of this CuteElement type. */
	public final static String description = DelayedActions.class.getSimpleName()
			+ " can have other Actions inside.\n"
			+ "On execution it runs these Actions after a specified delay in milliseconds.";

	/** The list of {@link Action Actions} to execute with delay. */
	@JsonProperty("actions")
	protected ObservableList<Action> actions = FXCollections.observableArrayList();

	/** The execution delay. */
	@JsonIgnore
	protected IntegerProperty delay = new SimpleIntegerProperty(0);
	
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
	public DelayedActions(@JsonProperty("actions") ArrayList<Action> actions,
			@JsonProperty("delay") int delay) {
		this.actions.setAll(actions);
		this.delay.set(delay);
		;
	}

	@Override
	public void execute() {
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(delay.get());
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

	@Override
	public ObservableList<? extends CuteElement> getChildren() {
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
	
	@JsonIgnore
	@Override
	public MinAddableChildrenCount getMinAddableChildrenCount() {
		return MinAddableChildrenCount.ONE;
	}

	@JsonProperty("delay")
	public int getDelay() {
		return this.delay.get();
	}
	
	@JsonProperty("delay")
	public void setDelay(int delay) {
		this.delay.set(delay);
	}
	
	public IntegerProperty delayProperty() {
		return delay;
	}

	@Override
	public void init() {
		super.init();
		if (delay.get() < 0) {
			elementInfo.setAsBroken("Delay can't be less than 0.");
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
	
}
