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
package com.ubershy.streamsis.gui.helperclasses;

import java.util.HashMap;
import java.util.Map;

import org.controlsfx.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.actions.Action;
import com.ubershy.streamsis.checkers.Checker;
import com.ubershy.streamsis.counters.Counter;
import com.ubershy.streamsis.gui.controllers.ElementEditorController;
import com.ubershy.streamsis.project.CuteElement;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Control;

/**
 * Can be used to determine enabled states of "OK", "Apply", "Cancel" and "Perform Test" buttons in
 * {@link ElementEditorController} based on reports from controls (user editable fields) and
 * {@link CuteElement}'s initialization status.<br>
 * Accepts reports by {@link #reportNewValueOfControl(Object, Object, Control, ValidationResult)}
 * method.
 */
public class CuteButtonsStatesManager {
	
	static final Logger logger = LoggerFactory.getLogger(CuteButtonsStatesManager.class);

	// Private variables.
	
	/**
	 * Holds information if particular {@link Control} has it's value changed from it's original
	 * value.<br>
	 * (The user has provided new value for {@link Control} different from the original value)
	 */
	private Map<Control, Boolean> changesMadeMap = new HashMap<Control, Boolean>();

	/** Holds information if particular {@link Control} has invalid value. */
	private Map<Control, Boolean> errorExistMap = new HashMap<Control, Boolean>();

	/**
	 * Tells if the user has provided new parameter values for {@link CuteElement} different from
	 * the original values.
	 */
	private BooleanProperty changesMade = new SimpleBooleanProperty(false);

	/** Tells if the user has provided wrong parameters for {@link CuteElement}. */
	private BooleanProperty errorsExist = new SimpleBooleanProperty(false);
	
	/**
	 * Tells if the current {@link CuteElement} needs to be reinitialized.<br>
	 * It usually happens on user input after all fields were successfully validated.<br>
	 * When CuteElement needs initialization, "Apply", "OK" and "Perform Test" buttons are turned
	 * off.<br>
	 * View controller should listen to this property to initialize CuteElement when it's needed.
	 * After initialization of the CuteElement is completed and even if the CuteElement seems
	 * broken, the controller should call the method {@link #setCuteElementAsInitialized()} to reset
	 * the variable to default value (false).
	 */
	private BooleanProperty needToReinitCuteElement = new SimpleBooleanProperty(false);
	
	/**
	 * Tells if {@link #needToReinitCuteElement} should be set to True after new report. After
	 * processing the report it should be reset back to False.
	 * <p>
	 * Explanation: {@link #needToReinitCuteElement} is bound with the View controller. This local
	 * variable helps to notify the View controller only once per report.
	 */
	private boolean currentReportAsksForReinit = false;

	/**
	 * Tells if fields ({@link Control}s) in {@link ElementEditorController} have different values
	 * from original ones and have passed field validation. I.e. User input seems valid. If so, we
	 * can try to initialize new {@link CuteElement} based on values of these fields for testing
	 * purposes. <br>
	 * Internally it depends on {@link #changesMade} and {@link #errorsExist} properties.
	 */
	private BooleanProperty isInputDiffersAndValid = new SimpleBooleanProperty(false);
	
	/**
	 * Tells if "Apply" button in {@link ElementEditorController} should be enabled. It can be
	 * enabled only if {@link CuteElement} in {@link ElementEditorController} was changed by user
	 * and has passed full validation. I.e. CuteElement was able to initialize without errors after
	 * the user has input new parameters.
	 */
	private BooleanProperty applyButtonOn = new SimpleBooleanProperty(false);

	/**
	 * Tells if "OK" button should be enabled. <br>
	 * It can be enabled only if {@link CuteElement} in {@link ElementEditorController} has passed
	 * full validation. I.e. CuteElement was able to initialize without errors. <br>
	 * Used also to calculate {@link #performTestButtonOn}.
	 */
	private BooleanProperty okButtonOn = new SimpleBooleanProperty(false);
	
	/**
	 * Tells if "Perform Test" button should be enabled. <br>
	 * It can be enabled only if: <br>
	 * 1. {@link CuteElement} in {@link ElementEditorController} has passed full validation. I.e.
	 * CuteElement was able to initialize without errors. <br>
	 * 2. CuteElement is not of type {@link Actor} or {@link SisScene}. <br>
	 * 3. "Perform Test" button has finished it's work if it was recently pressed. <br>
	 */
	private BooleanProperty performTestButtonOn = new SimpleBooleanProperty(false);
	
	/**
	 * Tells if "Perform Test" button is able to be enabled or not. <br>
	 * Used to calculate {@link #performTestButtonOn}.
	 */
	private BooleanProperty performTestButtonAllowed = new SimpleBooleanProperty(false);
	
	/**
	 * Tells if {@link CuteElement} is under testing invoked by "Perform Test" button. <br>
	 * Used to calculate {@link #performTestButtonOn}.
	 */
	private BooleanProperty currentlyTesting = new SimpleBooleanProperty(false);
			
	// Constructor
	public CuteButtonsStatesManager(){
		// Bind properties.
		okButtonOn.bind(needToReinitCuteElement.not().and(errorsExist.not()));
		applyButtonOn.bind(needToReinitCuteElement.not().and(isInputDiffersAndValid));
		performTestButtonOn
				.bind(okButtonOn.and(performTestButtonAllowed).and(currentlyTesting.not()));
	}
	
	// Private methods.
	
	/**
	 * Accepts information about if one of controls in {@link ElementEditorController} has different
	 * value from it's original value. Based on the input the {@link #isInputDiffersAndValid}
	 * property will be set.
	 *
	 * @param c
	 *            the {@link Control} (User editable field)
	 * @param b
	 *            tells if {@link Control}'s value different from it's original value<br>
	 *            (The user has provided new value for the {@link Control} different from the
	 *            original value)
	 */
	private void setChangedStateForControl(Control c, boolean b) {
		reportStateOfControl(c, b, changesMadeMap, changesMade);
	}

	/**
	 * Accepts information about if one of controls in {@link ElementEditorController} has wrong
	 * value or not. Based on the input the {@link #isInputDiffersAndValid} property will be set.
	 *
	 * @param c
	 *            the {@link Control} (User editable field)
	 * @param b
	 *            tells if {@link Control}'s value is wrong
	 */
	private void setErrorStateForControl(Control c, boolean b) {
		reportStateOfControl(c, b, errorExistMap, errorsExist);
	}

	/**
	 * Accepts the report of {@link Control} (user editable field) and it's state. <br>
	 * The state of the Control is a boolean variable and tells about something important, something
	 * that affects the provided property. <br>
	 * Keeps tracks of Controls and their states in the provided map. If the Control-State pair is
	 * different from the pair stored in the map of states, recalculates
	 * {@link #isInputDiffersAndValid} property based on all information that was acquired in the
	 * map so far.
	 *
	 * @param control
	 *            the control
	 * @param state
	 *            the state
	 * @param mapOfStates
	 *            the map of states
	 * @param affectedProperty
	 *            the affected property
	 */
	private void reportStateOfControl(Control control, boolean state,
			Map<Control, Boolean> mapOfStates, BooleanProperty affectedProperty) {
		boolean errorExistBeforeReporting = errorsExist.get();
		boolean needToRecalculateProperty = putControlBooleanPairInMap(control, state, mapOfStates);
		if (needToRecalculateProperty) {
			boolean needToRecalculateWholeInputStatus = updatePropertyBasedOnMapValues(mapOfStates,
					affectedProperty);
			if (needToRecalculateWholeInputStatus) {
				isInputDiffersAndValid.set(calculateIfInputDiffersAndValid());
				boolean isInputSameButChangedBackFromInvalidToValid = errorExistBeforeReporting
						&& !errorsExist.get() && !changesMade.get();
				if (isInputDiffersAndValid.get() || isInputSameButChangedBackFromInvalidToValid){
					currentReportAsksForReinit = true;
				}
			}
		}
	}

	/**
	 * Update the property based on the map.
	 *
	 * @param mapToScan
	 *            the map to scan
	 * @param propertyToUpdate
	 *            the property to update
	 * @return true, if property was updated<br>
	 *         false, if property wasn't updated
	 */
	private boolean updatePropertyBasedOnMapValues(Map<Control, Boolean> mapToScan,
			BooleanProperty propertyToUpdate) {
		boolean newPotentialPropertyValue = performLogicalOROnMapValues(mapToScan);
		if (newPotentialPropertyValue != propertyToUpdate.get()) {
			// Changes are significant, so let's update the property
			propertyToUpdate.set(newPotentialPropertyValue);
			return true;
		}
		return false;
	}

	/**
	 * Puts new pair of {@link Control} and boolean to the specified map.
	 *
	 * @param c
	 *            the {@link Control} to store in the map
	 * @param valueToPut
	 *            the boolean value corresponding to the provided {@link Control}
	 * @param mapWhereToPut
	 *            the map where to put the pair of {@link Control} and boolean
	 * @return true, if the provided pair do not exist in the map<br>
	 *         false, if the provided pair is already in the map
	 */
	private boolean putControlBooleanPairInMap(Control c, boolean valueToPut,
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

	/**
	 * Perform logical operation OR on all values on the specified map.
	 *
	 * @param map
	 *            the map which values are used
	 * @return the result of logical operation OR on all values on the map
	 */
	private boolean performLogicalOROnMapValues(Map<?, Boolean> map) {
		boolean result = false;
		for (Boolean bo : map.values()) {
			result = result || bo;
			if (result)
				break;
		}
		return result;
	}

	/**
	 * Calculate if fields have valid values and they are different from original values based on
	 * {@link #changesMade} and {@link #errorsExist} properties.
	 *
	 * @return true, if the values in all fields are different from original values and still valid.
	 */
	private boolean calculateIfInputDiffersAndValid() {
		return changesMade.get() && !(errorsExist.get());
	}

	// Public properties.
	
	/**
	 * See {@link #isInputDiffersAndValid}.
	 *
	 * @return the boolean property
	 */
	public BooleanProperty isInputDiffersAndValidProperty() {
		return isInputDiffersAndValid;
	}

	/**
	 * The getter for {@link #errorsExist}.
	 *
	 * @return the boolean property
	 */
	public BooleanProperty errorsExistProperty() {
		return errorsExist;
	}

	/**
	 * Clean up all Maps and Properties, so {@link #CuteButtonsStatesManager()} can be used on the
	 * new {@link CuteElement}.
	 */
	public void reset() {
		changesMade.set(false);
		errorsExist.set(false);
		isInputDiffersAndValid.set(false);
		needToReinitCuteElement.set(false);
		changesMadeMap.clear();
		errorExistMap.clear();
	}

	/**
	 * Tells if "OK" button should be enabled.
	 *
	 * @return the boolean property
	 */
	public BooleanProperty okButtonOnProperty() {
		return okButtonOn;
	}
	
	/**
	 * Tells if "Perform Test" button should be enabled.
	 *
	 * @return the boolean property
	 */
	public BooleanProperty performTestButtonOnProperty() {
		return performTestButtonOn;
	}

	/**
	 * Tells if "Apply" button should be enabled.
	 *
	 * @return the boolean property
	 */
	public BooleanProperty applyButtonOnProperty() {
		return applyButtonOn;
	}

	/**
	 * Tells if the current {@link CuteElement} should be reinitialized.
	 *
	 * @return the boolean property
	 */
	public BooleanProperty needToReinitCuteElementProperty() {
		return needToReinitCuteElement;
	}
	
	// Public methods.

	/**
	 * This method reports to {@link CuteButtonsStatesManager} about existing errors and changes of
	 * value in chosen {@link Control} (user editable field). <br>
	 * The information from this and other reports will be used to determine if "OK", "Apply",
	 * "Cancel" and "Perform Test" buttons in {@link ElementEditorController} will be enabled or
	 * not.
	 * 
	 * @Note "OK", "Apply", "Cancel" and "Perform Test" buttons also depend on current 
	 * {@link CuteElement}'s initialization status.
	 * 
	 * @param <T>
	 *
	 * @param originalValue
	 *            the original value of {@link Control}
	 * @param newValue
	 *            the new value of {@link Control}
	 * @param c
	 *            the {@link Control} (user editable field)
	 * @param finalValidationResult
	 *            the {@link ValidationResult} (provides errors in validation), can be Null
	 */
	public <T> void reportNewValueOfControl(T originalValue, T newValue, Control c,
			ValidationResult finalValidationResult) {
		// Let's tell CuteButtonStateManager if changes on this Control are made or not.
		// Let's get information about change by comparing original value of control with new one.
		setChangedStateForControl(c, !newValue.equals(originalValue));
		// Let's tell CuteButtonStateManager if errors in this Control exist or not.
		// Let's get information about existing errors from finalValidationResult.
		if (finalValidationResult != null) {
			setErrorStateForControl(c, !finalValidationResult.getErrors().isEmpty());
		}
		// This flag can be set to True by setChangedStateForControl() or setErrorStateForControl()
		if (currentReportAsksForReinit) {
			// Let's allow reinitialization (slow and hard validation) only if there are no errors
			// in GUI validation (fast and easy).
			if (!errorsExist.get())
				needToReinitCuteElement.set(true);
		}
		currentReportAsksForReinit = false;
	}
	
	public void setCuteElementAsInitialized(){
		needToReinitCuteElement.set(false);
	}

	/**
	 * This method lets {@link CuteButtonsStatesManager} to know if it should disable "Perform Test"
	 * button completely or not based on the current {@link CuteElement} under editing.
	 * <p>
	 * Internally it checks {@link CuteElement}'s class. Testing is allowed for {@link Action},
	 * {@link Checker}, {@link Counter} classes.
	 * 
	 * @param elementCopy
	 */
	public void allowOrNotPerformTestButtonBasedOnElementClass(CuteElement elementCopy) {
		if (elementCopy instanceof Action || elementCopy instanceof Checker
				|| elementCopy instanceof Counter) {
			performTestButtonAllowed.set(true);
		} else {
			performTestButtonAllowed.set(false);
		}
	}

	/**
	 * Report to {@link CuteButtonsStatesManager} about start of a test invoked by "Perform Test"
	 * button.
	 */
	public void reportStartOfTest() {
		currentlyTesting.set(true);		
	}
	
	/**
	 * Report to {@link CuteButtonsStatesManager} about end of a test invoked by "Perform Test"
	 * button.
	 */
	public void reportEndOfTest() {
		currentlyTesting.set(false);		
	}
}
