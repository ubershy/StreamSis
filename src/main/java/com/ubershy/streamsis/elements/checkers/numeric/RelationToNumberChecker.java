/** 
 * StreamSis
 * Copyright (C) 2016 Eva Balycheva
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

import com.ubershy.streamsis.elements.checkers.numeric.AbstractRelationToNumberChecker.BooleanNumberOperator;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * The interface of every Checker that checks the relation of two numbers. One of the numbers is
 * a contained Counter's result.
 */
public interface RelationToNumberChecker {

	/**
	 * Depending on the class which implements {@link RelationToNumberChecker} interface, the
	 * {@link BooleanNumberOperator} instance can have different description. This method helps to
	 * get the description of operator in current class's context.
	 * 
	 * @param operator
	 *            The {@link BooleanNumberOperator} to get description.
	 * @return Description of operator in current class's context.
	 */
	String getFullDescriptionOfOperator(BooleanNumberOperator operator);

	/** The Boolean Number Operator, e.g. EQUAL or GREATER. */
	public SimpleObjectProperty<BooleanNumberOperator> operatorProperty();
	public BooleanNumberOperator getOperator();
	public void setOperator(BooleanNumberOperator operator);
	
	/** The number always to compare with the contained Counter's result number. */
	public IntegerProperty compareNumberProperty();
	public int getCompareNumber();
	public void setCompareNumber(int compareNumber);
}
