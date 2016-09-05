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
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.project.AbstractCuteNode;
import com.ubershy.streamsis.project.ElementInfo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Logical Checker. <br>
 * This {@link Checker} contains a list of {@link Checker Checkers} inside. <br>
 * On {@link #check()} it performs checks on all contained {@link Checker Checkers}. <br>
 * The true or false <b>results</b> of each <b>of the many</b> {@link Checker Checkers} are then logically combined, <b>producing a single</b> true of false
 * <b>result</b>. <br>
 * The exact logical operation is specified by the user and can be AND, OR, NOT, XOR or SUB.
 * <p>
 * <b>Important:</b> the allowed amount of Checkers depends on chosen {@link BooleanOperator}. <br>
 * If the amount of Checkers is wrong, LogicalChecker will be considered as {@link ElementInfo#isBroken() broken}.
 * 
 * @see {@link BooleanOperator#AND}
 * @see {@link BooleanOperator#OR}
 * @see {@link BooleanOperator#NOT}
 * @see {@link BooleanOperator#XOR}
 * @see {@link BooleanOperator#SUB}
 */
public class LogicalChecker extends AbstractCuteNode implements Checker {

	/**
	 * Enum of boolean operators.
	 */
	public enum BooleanOperator {

		/** AND operator. Performs operation on two or more Checkers. */
		AND, /** NOT operator. Performs operation only on one Checker. */
		NOT, /** OR operator. Performs operation on two or more Checkers. */
		OR, /** SUB operator. Performs operation only on two Checkers. */
		SUB, /** XOR operator. Performs operation only on two Checkers. */
		XOR
	}

	static final Logger logger = LoggerFactory.getLogger(LogicalChecker.class);

	/**
	 * Creates the LogicalChecker with "AND" boolean operator.
	 *
	 * @param checkers
	 *            the array of Checkers
	 * @return the logical Checker
	 */
	public static LogicalChecker createAnd(Checker[] checkers) {
		return new LogicalChecker(checkers, BooleanOperator.AND);
	}

	/**
	 * Creates the LogicalChecker with "NOT" boolean operator.
	 *
	 * @param checker
	 *            the Checker
	 * @return the logical Checker
	 */
	public static LogicalChecker createNot(Checker checker) {
		return new LogicalChecker(new Checker[] { checker }, BooleanOperator.NOT);
	}

	/**
	 * Creates the LogicalChecker with "OR" boolean operator.
	 *
	 * @param checkers
	 *            the array of Checkers
	 * @return the logical Checker
	 */
	public static LogicalChecker createOr(Checker[] checkers) {
		return new LogicalChecker(checkers, BooleanOperator.OR);
	}

	/**
	 * Creates the LogicalChecker with "SUB" (Subtract) boolean operator.
	 *
	 * @param checkerA
	 *            the Checker a
	 * @param checkerB
	 *            the Checker b
	 * @return the logical checker
	 */
	public static LogicalChecker createSub(Checker checkerA, Checker checkerB) {
		return new LogicalChecker(new Checker[] { checkerA, checkerB }, BooleanOperator.SUB);
	}

	/**
	 * Creates the LogicalChecker with "XOR" boolean operator.
	 *
	 * @param checkerA
	 *            the Checker a
	 * @param checkerB
	 *            the Checker b
	 * @return the logical checker
	 */
	public static LogicalChecker createXor(Checker checkerA, Checker checkerB) {
		return new LogicalChecker(new Checker[] { checkerA, checkerB }, BooleanOperator.XOR);
	}

	/** The checkers to work with. */
	@JsonProperty
	private final ObservableList<Checker> checkers = FXCollections.observableArrayList();

	/** The Boolean Operator. e.g. AND or XOR */
	@JsonProperty
	private BooleanOperator operator = BooleanOperator.XOR;

	public LogicalChecker() {
	}

	/**
	 * Instantiates a new LogicalChecker. <br>
	 * <p>
	 * <b>Important:</b> the allowed amount of Checkers depends on chosen {@link BooleanOperator}. <br>
	 * If the amount of Checkers is wrong, LogicalChecker will be considered as {@link ElementInfo#isBroken() broken}.
	 * 
	 * @see {@link BooleanOperator#AND}
	 * @see {@link BooleanOperator#OR}
	 * @see {@link BooleanOperator#NOT}
	 * @see {@link BooleanOperator#XOR}
	 * @see {@link BooleanOperator#SUB}
	 * 
	 * @param checkers
	 *            the list of Checkers to work with
	 * @param operator
	 *            the {@link BooleanOperator} enum instance. Can be AND, OR, NOT, XOR or SUB
	 */
	@JsonCreator
	public LogicalChecker(@JsonProperty("checkers") ArrayList<Checker> checkers, @JsonProperty("operator") BooleanOperator operator) {
		this.operator = operator;
		this.checkers.setAll(checkers);
	}

	/**
	 * Instantiates a new LogicalChecker. <br>
	 * <p>
	 * <b>Important:</b> the allowed amount of Checkers depends on chosen {@link BooleanOperator}. <br>
	 * If the amount of Checkers is wrong, LogicalChecker will be considered as {@link ElementInfo#isBroken() broken}.
	 * 
	 * @see {@link BooleanOperator#AND}
	 * @see {@link BooleanOperator#OR}
	 * @see {@link BooleanOperator#NOT}
	 * @see {@link BooleanOperator#XOR}
	 * @see {@link BooleanOperator#SUB}
	 *
	 * @param checkers
	 *            the array of Checkers to work with
	 * @param operator
	 *            the {@link BooleanOperator} enum instance. Can be AND, OR, NOT, XOR or SUB
	 */
	public LogicalChecker(Checker[] checkers, BooleanOperator operator) {
		this((checkers == null) ? new ArrayList<Checker>() : new ArrayList<Checker>(Arrays.asList(checkers)), operator);
	}

	@Override
	public boolean check() {
		boolean result = false;
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			switch (this.operator) {
			case NOT:
				result = getNOTResult();
				break;
			case AND:
				result = getANDResult();
				break;
			case OR:
				result = getORResult();
				break;
			case XOR:
				result = getXORResult();
				break;
			case SUB:
				result = getSUBResult();
				break;
			}
			elementInfo.setBooleanResult(result);
		}
		return result;
	}

	/**
	 * Gets AND result.
	 *
	 * @return the AND result
	 */
	private boolean getANDResult() {
		boolean result = false;
		for (Checker checker : checkers) {
			result = checker.check();
			if (!result)
				break;
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
	
	/**
	 * Gets NOT result.
	 *
	 * @return the NOT result
	 */
	private boolean getNOTResult() {
		return !checkers.get(0).check();
	}

	public BooleanOperator getOperator() {
		return operator;
	}

	/**
	 * Gets OR result.
	 *
	 * @return the OR result
	 */
	private boolean getORResult() {
		boolean result = false;
		for (Checker checker : checkers) {
			result = checker.check();
			if (result)
				break;
		}
		return result;
	}

	/**
	 * Gets SUB result.
	 *
	 * @return the SUB result
	 */
	private boolean getSUBResult() {
		return (checkers.get(0).check()) && (!checkers.get(1).check());
	}

	/**
	 * Gets XOR result.
	 *
	 * @return the XOR result
	 */
	private boolean getXORResult() {
		return checkers.get(0).check() ^ checkers.get(1).check();
	}

	@Override
	public void init() {
		elementInfo.setAsReadyAndHealthy();
		if (checkers.size() != 0) {
			if (checkers.size() != 2 && (operator == BooleanOperator.SUB || operator == BooleanOperator.XOR)) {
				// throw new AssertionError("LogicalChecker construction failed: XOR and SUB operators can work only with TWO checkers");
				elementInfo.setAsBroken("XOR and SUB operators can work only with TWO Checkers. But you have '" + checkers.size() + "' Checkers");
			}
			if (checkers.size() < 2 && operator != BooleanOperator.NOT) {
				// throw new AssertionError("LogicalChecker construction failed: only NOT operator can work with less than TWO checkers");
				elementInfo.setAsBroken("only NOT operator can work with one Checker. But you have '" + checkers.size() + "' Checkers");
			}
			if (checkers.size() != 1 && operator == BooleanOperator.NOT) {
				// throw new AssertionError("LogicalChecker construction failed: NOT operator can work only with one checkers");
				elementInfo.setAsBroken("NOT operator can work only with one checkers. But you have '" + checkers.size() + "' Checkers");
			}
		} else {
			elementInfo.setAsBroken("No Checkers are assigned to this Checker");
			return;
		}
		for (Checker checker : checkers) {
			checker.init();
			if (checker.getElementInfo().isBroken()) {
				elementInfo.setAsBroken("Contained Checker " + checker.getClass().getSimpleName() + " is broken. Please fix it first");
				return;
			}
		}
	}

	public void setOperator(BooleanOperator operator) {
		this.operator = operator;
	}

}
