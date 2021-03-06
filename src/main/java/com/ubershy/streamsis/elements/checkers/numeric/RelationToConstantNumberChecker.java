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
package com.ubershy.streamsis.elements.checkers.numeric;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.elements.checkers.Checker;
import com.ubershy.streamsis.elements.counters.Counter;

/**
 * Relation To Constant Number Checker. <br>
 * This {@link Checker} is working with {@link Counter}. <br>
 * On {@link #check()} it compares a number returned by {@link Counter#count()} with a
 * <b>constant(not in terms of programming) number</b> specified by the user. <br>
 * And based on comparison result, it returns true or false. <br>
 * The user must specify {@link AbstractRelationToNumberChecker#operator operator} that defines type
 * of comparison, e.g. if Counter result must be greater or equal than the constant number.
 */
@SuppressWarnings("unchecked")
public class RelationToConstantNumberChecker extends AbstractRelationToNumberChecker {

	static final Logger logger = LoggerFactory.getLogger(RelationToConstantNumberChecker.class);
	
	/** The description of this CuteElement type. */
	public final static String description = RelationToConstantNumberChecker.class.getSimpleName()
			+ " on check it asks the contained inside Counter to count something.\n"
			+ "Then it compares the count result of the Counter with a constant number specified by"
			+ " user.\n"
			+ "If the count result matches condition specified by user, this "
			+ RelationToConstantNumberChecker.class.getSimpleName()
			+ " returns True. False otherwise.";
	
	static {
		compareNumberDescription = "provided constant number";
	}

	public RelationToConstantNumberChecker() {
	}

	/**
	 * Instantiates a new {@link RelationToConstantNumberChecker} (used by deserializator).
	 *
	 * @param counter
	 *            the ArrayList with single {@link Counter} which result to compare with
	 *            compareNumber
	 * @param operator
	 *            the operator, defines how Counter's result number must relate to compareNumber,
	 *            e.g. EQUAL or GREATER
	 * @param compareNumber
	 *            the constant(not in terms of programming) number always to compare with Counter's
	 *            result
	 */
	@JsonCreator
	public RelationToConstantNumberChecker(@JsonProperty("counter") ArrayList<Counter> counter,
			@JsonProperty("operator") BooleanNumberOperator operator,
			@JsonProperty("compareNumber") int constantNumber) {
		this.operator.set(operator);
		this.counter.setAll(counter);
		this.compareNumber.set(constantNumber);
	}

	/**
	 * Instantiates a new {@link RelationToConstantNumberChecker} normally.
	 *
	 * @param counter
	 *            the {@link Counter} which result to compare with compareNumber
	 * @param operator
	 *            the operator, defines how Counter's result number must relate to compareNumber,
	 *            e.g. EQUAL or GREATER
	 * @param compareNumber
	 *            the constant(not in terms of programming) number always to compare with Counter's
	 *            result
	 */
	public RelationToConstantNumberChecker(Counter counter, BooleanNumberOperator operator,
			int constantNumber) {
		this.operator.set(operator);
		this.counter.setAll(counter);
		this.compareNumber.set(constantNumber);
	}

	@Override
	public boolean check() {
		boolean result = false;
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			int count = counter.get(0).count();
			switch (operator.get()) {
			case GREATER:
				result = (count > compareNumber.get());
				break;
			case LESS:
				result = (count < compareNumber.get());
				break;
			case EQUAL:
				result = (count == compareNumber.get());
				break;
			case NOTEQUAL:
				result = (count != compareNumber.get());
				break;
			case LESSOREQUAL:
				result = (count <= compareNumber.get());
				break;
			case GREATEROREQUAL:
				result = (count >= compareNumber.get());
				break;
			}
			elementInfo.setBooleanResult(result);
		}
		return result;
	}

}
