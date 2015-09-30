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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ubershy.streamsis.counters.Counter;

/**
 * Relation To Constant Number Checker. <br>
 * This {@link Checker} is working with {@link Counter}. <br>
 * On {@link #check()} it compares a number returned by {@link Counter#count()} with a <b>constant(not in terms of programming) number</b> specified by the
 * user. <br>
 * And based on comparison result, it returns true or false. <br>
 * The user must specify {@link AbstractRelationToNumberChecker#operator operator} that defines type of comparison, e.g. if Counter result must be greater or
 * equal than the constant number.
 */
@SuppressWarnings("unchecked")
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "chkrType")
public class RelationToConstantNumberChecker extends AbstractRelationToNumberChecker {

	static final Logger logger = LoggerFactory.getLogger(RelationToConstantNumberChecker.class);

	public RelationToConstantNumberChecker() {
	}

	/**
	 * Instantiates a new {@link RelationToConstantNumberChecker} (used by deserializator).
	 *
	 * @param counter
	 *            the ArrayList with single {@link Counter} which result to compare with compareNumber
	 * @param operator
	 *            the operator, defines how Counter's result number must relate to compareNumber, e.g. EQUAL or GREATER
	 * @param compareNumber
	 *            the constant(not in terms of programming) number always to compare with Counter's result
	 */
	@JsonCreator
	public RelationToConstantNumberChecker(@JsonProperty("counter") ArrayList<Counter> counter, @JsonProperty("operator") BooleanNumberOperator operator,
			@JsonProperty("compareNumber") int constantNumber) {
		this.operator = operator;
		this.counter.setAll(counter);
		this.compareNumber = constantNumber;
	}

	/**
	 * Instantiates a new {@link RelationToConstantNumberChecker} normally.
	 *
	 * @param counter
	 *            the {@link Counter} which result to compare with compareNumber
	 * @param operator
	 *            the operator, defines how Counter's result number must relate to compareNumber, e.g. EQUAL or GREATER
	 * @param compareNumber
	 *            the constant(not in terms of programming) number always to compare with Counter's result
	 */
	public RelationToConstantNumberChecker(Counter counter, BooleanNumberOperator operator, int constantNumber) {
		this.operator = operator;
		this.counter.setAll(counter);
		this.compareNumber = constantNumber;
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
			if (result)
				elementInfo.setSuccessfulResult();
			else
				elementInfo.setFailedResult();
		}
		return result;
	}

}
