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
package com.ubershy.streamsis.gui.helperclasses;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Control;

public class ApplyAndOkButtonsStateManager {
	private Map<Control, Boolean> changesMadeMap = new HashMap<Control, Boolean>();

	private Map<Control, Boolean> errorExistMap = new HashMap<Control, Boolean>();

	private BooleanProperty changesMade = new SimpleBooleanProperty(false);
	private BooleanProperty errorsExist = new SimpleBooleanProperty(false);
	private BooleanProperty canApply = new SimpleBooleanProperty(false);

	public void setChangeStateForControl(Control c, boolean b) {
		boolean needToRecalculateProperty = setStateForControlInMap(c, b, changesMadeMap);
		if (needToRecalculateProperty) {
			boolean needToRecalculateCanApply = recalculateProperty(changesMadeMap, changesMade);
			if (needToRecalculateCanApply) {
				canApply.set(calculateCanApply());
			}
		}
	}

	public void setErrorStateForControl(Control c, boolean b) {
		boolean needToRecalculateProperty = setStateForControlInMap(c, b, errorExistMap);
		if (needToRecalculateProperty) {
			boolean needToRecalculateCanApply = recalculateProperty(errorExistMap, errorsExist);
			if (needToRecalculateCanApply) {
				canApply.set(calculateCanApply());
			}
		}
	}

	private boolean recalculateProperty(Map<Control, Boolean> mapToScan,
			BooleanProperty propertyToUpdate) {
		boolean newPotentialPropertyValue = calculateBooleanBasedOnMap(mapToScan);
		if (newPotentialPropertyValue != propertyToUpdate.get()) {
			// Changes are significant, so lets update properties
			propertyToUpdate.set(newPotentialPropertyValue);
			return true;
		}
		return false;
	}

	private boolean setStateForControlInMap(Control c, boolean valueToPut,
			Map<Control, Boolean> mapWhereToPut) {
		Boolean boxedNewValue = new Boolean(valueToPut);
		if (boxedNewValue.equals(mapWhereToPut.get(c))) {
			// This means that the value corresponding to control hasn't changed
			// So nothing to put, nothing to recalculate
			return false;
		}
		mapWhereToPut.put(c, boxedNewValue);
		return true;
	}

	private boolean calculateBooleanBasedOnMap(Map<Control, Boolean> map) {
		boolean result = false;
		for (Boolean bo : map.values()) {
			result = result || bo;
			if (result)
				break;
		}
		return result;
	}

	private boolean calculateCanApply() {
		return changesMade.get() && !(errorsExist.get());
	}

	public BooleanProperty canApplyProperty() {
		return canApply;
	}

	public BooleanProperty errorsExistProperty() {
		return errorsExist;
	}

	public void reset() {
		changesMade.set(false);
		errorsExist.set(false);
		canApply.set(false);
		changesMadeMap.clear();
		errorExistMap.clear();
	}
}
