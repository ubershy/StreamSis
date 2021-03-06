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
package com.ubershy.streamsis.elements.counters;

import java.util.ArrayList;

//This class counts how many checkers are true
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.elements.AbstractCuteElement;
import com.ubershy.streamsis.elements.checkers.Checker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * True Checker Counter. <br>
 * This {@link Counter} on {@link #count()} performs {@link Checker#check() check()} on contained
 * {@link Checker Checkers}. <br>
 * Then it counts how many of them have returned true.
 */
public class TrueCheckerCounter extends AbstractCuteElement implements Counter {

	static final Logger logger = LoggerFactory.getLogger(TrueCheckerCounter.class);
	
	/** The description of this CuteElement type. */
	public final static String description = TrueCheckerCounter.class.getSimpleName()
			+ " on count performs checks on contained inside Checkers. Then it returns a number of"
			+ " how many of them have returned True result.";

	/** The checkers to {@link Checker#check() check()} and count their true results. */
	@JsonProperty
	private final ObservableList<Checker> checkers = FXCollections.observableArrayList();

	public TrueCheckerCounter() {
	}

	/**
	 * Instantiates a new TrueCheckerCounter.
	 *
	 * @param checkers
	 *            the checkers to {@link Checker#check() check()} and count their true results
	 */
	@JsonCreator
	public TrueCheckerCounter(@JsonProperty("checkers") ArrayList<Checker> checkers) {
		this.checkers.setAll(checkers);
	}

	@Override
	public void init() {
		super.init();
	}

	@Override
	public int count() {
		int result = 0;
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			for (Checker checker : checkers) {
				if (checker.check())
					result++;
			}
			elementInfo.setNumericResult(result);
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ObservableList getChildren() {
		return checkers;
	}

	@JsonIgnore
	@Override
	public AddableChildrenTypeInfo getAddableChildrenTypeInfo() {
		return AddableChildrenTypeInfo.CHECKER;
	}

	@JsonIgnore
	@Override
	public MaxAddableChildrenCount getMaxAddableChildrenCount() {
		return MaxAddableChildrenCount.INFINITY;
	}
}
