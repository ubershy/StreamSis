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

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.counters.Counter;

/**
 * Relation To Previous Number Checker. <br>
 * This {@link Checker} is working with {@link Counter}. <br>
 * On {@link #check()} it compares a number returned by {@link Counter#count()} with a <b>previous Counter's result</b>. <br>
 * And based on comparison result, it returns true or false. <br>
 * The user must specify {@link AbstractRelationToNumberChecker#operator operator} that defines type of comparison, e.g. if Counter result must be greater or
 * equal than the previous Counter's result. <br>
 * The user must set initial value for "previous Counter's result".
 */
@SuppressWarnings("unchecked")
public class RelationToPreviousNumberChecker extends AbstractRelationToNumberChecker {
	
	public RelationToPreviousNumberChecker() {
	}

	/**
	 * Instantiates a new {@link RelationToPreviousNumberChecker} (used by deserializator).
	 *
	 * @param counter
	 *            the ArrayList with single {@link Counter} which current result to compare with it's previous result
	 * @param operator
	 *            the operator, defines how Counter's result number must relate to it's previous result, e.g. EQUAL or GREATER
	 * @param compareNumber
	 *            the initial value to set as Counter's previous result
	 */
	@JsonCreator
	public RelationToPreviousNumberChecker(@JsonProperty("counter") ArrayList<Counter> counter, @JsonProperty("operator") BooleanNumberOperator operator,
			@JsonProperty("compareNumber") int previousValue) {
		this.compareNumber = previousValue;
		this.operator = operator;
		this.counter.setAll(counter);
	}

	/**
	 * Instantiates a new {@link RelationToPreviousNumberChecker} normally.
	 *
	 * @param counter
	 *            the {@link Counter} which current result to compare with it's previous result
	 * @param operator
	 *            the operator, defines how Counter's result number must relate to it's previous result, e.g. EQUAL or GREATER
	 * @param compareNumber
	 *            the initial value to set as Counter's previous result
	 */
	public RelationToPreviousNumberChecker(Counter counter, BooleanNumberOperator operator, int previousValue) {
		this.operator = operator;
		this.counter.setAll(counter);
		this.compareNumber = previousValue;
	}

	@Override
	public boolean check() {
		boolean result = false;
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			int count = counter.get(0).count();
			switch (operator) {
			case GREATER:
				result = (count > compareNumber);
				break;
			case LESS:
				result = (count < compareNumber);
				break;
			case EQUAL:
				result = (count == compareNumber);
				break;
			case NOTEQUAL:
				result = (count != compareNumber);
				break;
			case LESSOREQUAL:
				result = (count <= compareNumber);
				break;
			case GREATEROREQUAL:
				result = (count >= compareNumber);
				break;
			}
			compareNumber = count;
			if (result)
				elementInfo.setSuccessfulResult();
			else
				elementInfo.setFailedResult();
		}
		return result;
	}

}
