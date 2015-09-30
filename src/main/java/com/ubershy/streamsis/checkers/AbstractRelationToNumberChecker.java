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
package com.ubershy.streamsis.checkers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ubershy.streamsis.counters.Counter;
import com.ubershy.streamsis.project.AbstractCuteNode;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Relation To Number Checker (abstract). <br>
 * This abstract {@link Checker} is working with {@link Counter}. <br>
 * On {@link #check()} it compares a number returned by {@link Counter#count()} with the {@link #CompareNumber}CompareNumber. <br>
 * And based on comparison result, it returns true or false. <br>
 * The user must specify {@link BooleanNumberOperator} that defines type of comparison, e.g. if Counter result must be greater or equal.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "chkrType")
public abstract class AbstractRelationToNumberChecker extends AbstractCuteNode implements Checker {

	/**
	 * The Enum BooleanNumberOperator, with values like EQUAL or GREATER or LESSOREQUAL.
	 */
	public enum BooleanNumberOperator {

		/** Defines if Counter's result number must be equal to {@link #compareNumber}. */
		EQUAL, /** Defines if Counter's result number must be greater than {@link #compareNumber}. */
		GREATER, /** Defines if Counter's result number must not be equal or greater than {@link #compareNumber}. */
		GREATEROREQUAL, /** Defines if Counter's result number must be less than {@link #compareNumber}. */
		LESS, /** Defines if Counter's result number must not be equal or less than {@link #compareNumber}. */
		LESSOREQUAL, /** Defines if Counter's result number must not be equal to {@link #compareNumber}. */
		NOTEQUAL
	}

	static final Logger logger = LoggerFactory.getLogger(AbstractRelationToNumberChecker.class);

	/** The number always to compare with Counter's result number. */
	@JsonProperty("compareNumber")
	protected int compareNumber = 0;

	/** Contained {@link Counter}. Actually in this ObservableList can only be one Counter. It's just for getChildren() method. */
	@JsonProperty("counter")
	protected final ObservableList<Counter> counter = FXCollections.observableArrayList();

	/** The Boolean Number Operator, e.g. EQUAL or GREATER. */
	@JsonProperty("operator")
	protected BooleanNumberOperator operator = BooleanNumberOperator.EQUAL;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@JsonIgnore
	@Override
	public ObservableList getChildren() {
		return counter;
	}

	@JsonIgnore
	@Override
	public AddableChildrenTypeInfo getAddableChildrenTypeInfo() {
		return AddableChildrenTypeInfo.COUNTER;
	}
	
	@JsonIgnore
	@Override
	public MaxAddableChildrenCount getMaxAddableChildrenCount() {
		return MaxAddableChildrenCount.ONE;
	}
	
	public int getCompareNumber() {
		return compareNumber;
	}

	public BooleanNumberOperator getOperator() {
		return operator;
	}

	@Override
	public void init() {
		elementInfo.setAsReadyAndHealthy();
		if (counter.size() != 0) {
			if (counter.get(0) == null) {
				elementInfo.setAsBroken("Counter is not defined");
				return;
			}
			counter.get(0).init();
			if (counter.get(0).getElementInfo().isBroken()) {
				elementInfo.setAsBroken("Contained " + counter.get(0).getClass().getSimpleName() + " is broken. Please fix it first");
			}
		} else {
			elementInfo.setAsBroken("No Counter is assigned to this Checker");
		}
	}

	public void setCompareNumber(int compareNumber) {
		this.compareNumber = compareNumber;
	}

	public void setOperator(BooleanNumberOperator operator) {
		this.operator = operator;
	}
}
