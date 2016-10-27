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
import com.ubershy.streamsis.counters.Counter;
import com.ubershy.streamsis.project.AbstractCuteNode;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Relation To Number Checker (abstract). <br>
 * This abstract {@link Checker} is working with {@link Counter}. <br>
 * On {@link #check()} it compares a number returned by {@link Counter#count()} with the
 * {@link #CompareNumber}CompareNumber. <br>
 * And based on comparison result, it returns true or false. <br>
 * The user must specify {@link BooleanNumberOperator} that defines type of comparison, e.g. if
 * Counter result must be greater or equal.
 */
public abstract class AbstractRelationToNumberChecker extends AbstractCuteNode
		implements Checker, RelationToNumberChecker {

	/**
	 * The Enum BooleanNumberOperator, with the values like EQUAL or GREATER or LESSOREQUAL.
	 */
	public enum BooleanNumberOperator {
		/**
		 * Defines if Counter's result number must be equal to
		 * {@link AbstractRelationToNumberChecker#compareNumber compare number}.
		 */
		EQUAL("Makes this checker return True if Counter's result number is equal to"),
		/**
		 * Defines if Counter's result number must be greater than
		 * {@link AbstractRelationToNumberChecker#compareNumber compare number}.
		 */
		GREATER("Makes this checker return True if Counter's result number is greater than"),
		/**
		 * Defines if Counter's result number must not be equal or greater than
		 * {@link AbstractRelationToNumberChecker#compareNumber compare number}.
		 */
		GREATEROREQUAL("Makes this checker return True if Counter's result number is greater or "
				+ "equal to"),
		/**
		 * Defines if Counter's result number must be less than
		 * {@link AbstractRelationToNumberChecker#compareNumber compare number}.
		 */
		LESS("Makes this checker return True if Counter's result number is less than"),
		/**
		 * Defines if Counter's result number must not be equal or less than
		 * {@link AbstractRelationToNumberChecker#compareNumber compare number}.
		 */
		LESSOREQUAL("Makes this checker return True if Counter's result number is less or equal "
				+ "to"),
		/**
		 * Defines if Counter's result number must not be equal to
		 * {@link AbstractRelationToNumberChecker#compareNumber compare number}.
		 */
		NOTEQUAL("Makes this checker return True if Counter's result number is not equal to");

		/** The message that describes operation that produces boolean result. */
		private final String description;

		/**
		 * Instantiates a new BooleanNumberOperator.
		 *
		 * @param description
		 *            The partial {@link #description} of BooleanNumberOperator.
		 */
		private BooleanNumberOperator(String description) {
			this.description = description;
		}

		@Override
		public String toString() {
			return description;
		}
	}

	static final Logger logger = LoggerFactory.getLogger(AbstractRelationToNumberChecker.class);
	
	static String compareNumberDescription;

	@JsonProperty("compareNumber")
	protected IntegerProperty compareNumber = new SimpleIntegerProperty(0);
	@Override
	public IntegerProperty compareNumberProperty() {return compareNumber;}
	@Override
	public int getCompareNumber() {return compareNumber.get();}
	@Override
	public void setCompareNumber(int compareNumber) {this.compareNumber.set(compareNumber);}

	/**
	 * Contained {@link Counter}. Actually in this ObservableList can only be one Counter. It's just
	 * for getChildren() method.
	 */
	@JsonProperty("counter")
	protected final ObservableList<Counter> counter = FXCollections.observableArrayList();

	@JsonProperty("operator")
	protected SimpleObjectProperty<BooleanNumberOperator> operator = new SimpleObjectProperty<BooleanNumberOperator>(
			BooleanNumberOperator.EQUAL);
	@Override
	public SimpleObjectProperty<BooleanNumberOperator> operatorProperty() {return operator;}
	@Override
	public BooleanNumberOperator getOperator() {return operator.get();}
	@Override
	public void setOperator(BooleanNumberOperator operator) {this.operator.set(operator);}
	
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
				elementInfo.setAsBroken("Contained " + counter.get(0).getClass().getSimpleName()
						+ " is broken. Please fix it first");
			}
		} else {
			elementInfo.setAsBroken("No Counter is assigned to this Checker");
		}
	}
	
	@JsonIgnore
	@Override
	public String getFullDescriptionOfOperator(BooleanNumberOperator operator) {
		return operator.toString() + " " + compareNumberDescription + ".";
	}

}
