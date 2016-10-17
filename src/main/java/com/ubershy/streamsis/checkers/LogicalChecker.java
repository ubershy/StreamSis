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

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Logical Checker. <br>
 * This {@link Checker} contains a list of {@link Checker Checkers} inside. <br>
 * On {@link #check()} it performs checks on all contained {@link Checker Checkers}. <br>
 * The true or false <b>results</b> of each <b>of the many</b> {@link Checker Checkers} are then
 * logically combined, <b>producing a single</b> true of false <b>result</b>. <br>
 * The exact logical operation is specified by the user and can be AND, OR, NOT, XOR or SUB.
 * <p>
 * <b>Important:</b> the allowed amount of Checkers depends on chosen {@link BooleanOperator}. <br>
 * If the amount of Checkers is wrong, LogicalChecker will be considered as
 * {@link ElementInfo#isBroken() broken}.
 * 
 * @see {@link BooleanOperator#AND}
 * @see {@link BooleanOperator#OR}
 * @see {@link BooleanOperator#NOT}
 * @see {@link BooleanOperator#XOR}
 */
public class LogicalChecker extends AbstractCuteNode implements Checker {

	/**
	 * Enum of boolean operators (logical operations that produce boolean result).
	 */
	public enum BooleanOperator {
		/** NOT operator. Performs operation only on one Checker. */
		NOT("'Negation' operation. Also known as logical complement.\n"
				+ "Inverts True input to False and False input to True.\n"
				+ "Works only with one Checker."),
		/** XOR operator. Performs operation only on two Checkers. */
		XOR("'Exclusive Or' operation. Also known as logical exclusive disjunction.\n"
				+ "Produces True only if One of the inputs is True. False in all other cases.\n"
				+ "Works only with two Checkers."),
		/** AND operator. Performs operation on two or more Checkers. */
		AND("'And' operation. Also known as logical conjunction.\n"
				+ "Produces True only if all of the inputs are True.\n"
				+ "Works only with two or more Checkers."),
		/** OR operator. Performs operation on two or more Checkers. */
		OR("'Or' operation. Also known as logical disjunction. Also known as logical alternation.\n"
				+ "Produces True if one or more of the inputs are True.\n"
				+ "Works only with two or more Checkers."),;
		
		/** The message that describes Logical Operation that produces boolean result. */
		private final String description;

		/**
		 * Instantiates a new BooleanOperator.
		 *
		 * @param description
		 *            the {@link #description}
		 */
		private BooleanOperator(String description) {
			this.description = description;
		}

		@Override
		public String toString() {
			return description;
		}
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
	private ObservableList<Checker> checkers = FXCollections.observableArrayList();

	/** The Boolean Operator. e.g. AND or XOR */
	@JsonProperty
	private SimpleObjectProperty<BooleanOperator> operator = new SimpleObjectProperty<LogicalChecker.BooleanOperator>(
			BooleanOperator.NOT);

	public LogicalChecker() {
	}

	/**
	 * Instantiates a new LogicalChecker. <br>
	 * <p>
	 * <b>Important:</b> the allowed amount of Checkers depends on chosen {@link BooleanOperator}.
	 * <br>
	 * If the amount of Checkers is wrong, LogicalChecker will be considered as
	 * {@link ElementInfo#isBroken() broken}.
	 * 
	 * @see {@link BooleanOperator#AND}
	 * @see {@link BooleanOperator#OR}
	 * @see {@link BooleanOperator#NOT}
	 * @see {@link BooleanOperator#XOR}
	 * 
	 * @param checkers
	 *            the list of Checkers to work with
	 * @param operator
	 *            the {@link BooleanOperator} enum instance. Can be AND, OR, NOT, XOR or SUB
	 */
	@JsonCreator
	public LogicalChecker(@JsonProperty("checkers") ArrayList<Checker> checkers,
			@JsonProperty("operator") BooleanOperator operator) {
		this.operator.set(operator);;
		this.checkers.setAll(checkers);
	}

	/**
	 * Instantiates a new LogicalChecker. <br>
	 * <p>
	 * <b>Important:</b> the allowed amount of Checkers depends on chosen {@link BooleanOperator}.
	 * <br>
	 * If the amount of Checkers is wrong, LogicalChecker will be considered as
	 * {@link ElementInfo#isBroken() broken}.
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
		this((checkers == null) ? new ArrayList<Checker>()
				: new ArrayList<Checker>(Arrays.asList(checkers)), operator);
	}

	@Override
	public boolean check() {
		boolean result = false;
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			switch (operator.get()) {
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ObservableList getChildren() {
		return checkers;
	}
	
	public void setChildren(ObservableList<Checker> checkers) {
		this.checkers = checkers;
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
		if (checkers.size() == 0) {
			elementInfo.setAsBroken("No Checkers are assigned to this Checker");
			return;
		}
		String operatorIsBrokenMessage = whyOperatorCantBeAppliedToCurrentAmountOfCheckers(
				operator.get());
		if (operatorIsBrokenMessage != null) {
			elementInfo.setAsBroken(operatorIsBrokenMessage);
			return;
		}
		for (Checker checker : checkers) {
			checker.init();
			if (checker.getElementInfo().isBroken()) {
				elementInfo.setAsBroken("Contained Checker " + checker.getClass().getSimpleName()
						+ " is broken. Please fix it first");
				return;
			}
		}
	}

	/**
	 * Helps to determine if Logical Operator can be applied.
	 * 
	 * @param booleanOperator
	 *            The Operator to check if it can work.
	 * @return Explanation why Operator can't work. <br>
	 *         null, if it can work.
	 */
	public String whyOperatorCantBeAppliedToCurrentAmountOfCheckers(
			BooleanOperator booleanOperator) {
		String brokenMessage = null;
		if (checkers.size() != 0) {
			if (checkers.size() != 2 && (booleanOperator == BooleanOperator.XOR)) {
				brokenMessage = "XOR operator can work only with TWO Checkers."
						+ " You have " + checkers.size() + " Checker(s)";
			}
			if (checkers.size() < 2 && booleanOperator != BooleanOperator.NOT) {
				brokenMessage = "Only NOT operator can work with one Checker. You have "
						+ checkers.size() + " Checker(s)";
			}
			if (checkers.size() != 1 && booleanOperator == BooleanOperator.NOT) {
				brokenMessage = "NOT operator can work only with one checkers. You have "
						+ checkers.size() + " Checker(s)";
			}
		}
		return brokenMessage;
	}

	public BooleanOperator getOperator() {
		return operator.get();
	}
	
	public void setOperator(BooleanOperator operator) {
		this.operator.set(operator);;
	}

}
