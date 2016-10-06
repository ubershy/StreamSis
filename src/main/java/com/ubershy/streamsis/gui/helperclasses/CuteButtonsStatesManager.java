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

import com.ubershy.streamsis.actions.Action;
import com.ubershy.streamsis.checkers.Checker;
import com.ubershy.streamsis.counters.Counter;
import com.ubershy.streamsis.gui.controllers.ElementEditorController;
import com.ubershy.streamsis.project.CuteElement;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
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
	
	// Very private variables and properties.
	
	/**
	 * Holds information if particular {@link Control} has it's value changed from it's original
	 * value.<br>
	 * (The user has provided new value for {@link Control} different from the original value)
	 */
	private final Map<Control, Boolean> changesMadeMap = new HashMap<Control, Boolean>();

	/** Holds information if particular {@link Control} has invalid value. */
	private final Map<Control, Boolean> errorExistMap = new HashMap<Control, Boolean>();

	/**
	 * Tells if the user has input some new parameter values in controls for editing
	 * {@link CuteElement} that are different from the original values.
	 */
	private final BooleanProperty changesMade = new SimpleBooleanProperty(false);

	/** Tells if the user has provided wrong input in controls for editing {@link CuteElement}. */
	private final BooleanProperty errorsExist = new SimpleBooleanProperty(false);
	
	/**
	 * Tells if "Perform Test" button is able to be enabled or not. <br>
	 * Used to calculate {@link #performTestButtonOn}.
	 */
	private final BooleanProperty performTestButtonAllowed = new SimpleBooleanProperty(false);
	
	/**
	 * Tells if {@link CuteElement} is under testing invoked by "Perform Test" button. <br>
	 * Used to calculate {@link #performTestButtonOn}.
	 */
	private final BooleanProperty currentlyTesting = new SimpleBooleanProperty(false);
	
	/**
	 * Tells if fields ({@link Control}s) in {@link ElementEditorController} have different values
	 * from original ones and have passed field validation. I.e. User input seems valid. If so, we
	 * can try to initialize new {@link CuteElement} based on values of these fields for testing
	 * purposes. <br>
	 * Internally it depends on {@link #changesMade} and {@link #errorsExist} properties.
	 */
	private BooleanProperty isInputDiffersAndValid = new SimpleBooleanProperty(false);
	
	// Properties that are read-only outside the class.
	
	/**
	 * Tells if the current {@link CuteElement} reinitialization needs to be scheduled.<br>
	 * It usually happens on user input after all fields were successfully validated.<br>
	 * When CuteElement needs initialization, "Apply", "OK" and "Perform Test" buttons should be
	 * turned off.<br>
	 * View controller should listen to this property to initialize CuteElement when it's needed.
	 * After initialization of the CuteElement is completed and even if the CuteElement seems
	 * broken, the controller should call the method {@link #setCuteElementAsInitialized()} to reset
	 * the variable to default value (false).
	 */
	private final ReadOnlyBooleanWrapper needToScheduleCuteElementReinit = 
			new ReadOnlyBooleanWrapper(this, "needToScheduleCuteElementReinit", false);
	
	/**
	 * Tells if the current {@link CuteElement} scheduled reinitalization needs to be canceled
	 * (if it was scheduled). <br>
	 * View controller should listen to this property to cancel scheduled reinitialization when it's
	 * needed.
	 * After canceling (or doing nothing because it was not scheduled) the view controller should
	 * call the method {@link #setCuteElementReinitSuccessfullyCancelled()} to reset the variable to
	 * default value (false).
	 */
	private final ReadOnlyBooleanWrapper needToCancelScheduledCuteElementReinit = 
			new ReadOnlyBooleanWrapper(this, "needToCancelScheduledCuteElementReinit", false);
	
	/**
	 * Tells if "Apply" button in {@link ElementEditorController} should be enabled. It can be
	 * enabled only if {@link CuteElement} in {@link ElementEditorController} was changed by user
	 * and has passed full validation. I.e. CuteElement was able to initialize without errors after
	 * the user has input new parameters.
	 */
	private final ReadOnlyBooleanWrapper applyButtonOn = new ReadOnlyBooleanWrapper(this,
			"applyButtonOn", false);

	/**
	 * Tells if "OK" button should be enabled. <br>
	 * It can be enabled only if {@link CuteElement} in {@link ElementEditorController} has passed
	 * full validation. I.e. CuteElement was able to initialize without errors. <br>
	 * Used also to calculate {@link #performTestButtonOn}.
	 */
	private final ReadOnlyBooleanWrapper okButtonOn = new ReadOnlyBooleanWrapper(this, "okButtonOn",
			false);
	
	/**
	 * Tells if "Perform Test" button should be enabled. <br>
	 * It can be enabled only if: <br>
	 * 1. {@link CuteElement} in {@link ElementEditorController} has passed full validation. I.e.
	 * CuteElement was able to initialize without errors. <br>
	 * 2. CuteElement is not of type {@link Actor} or {@link SisScene}. <br>
	 * 3. "Perform Test" button has finished it's work if it was recently pressed. <br>
	 */
	private final ReadOnlyBooleanWrapper performTestButtonOn = new ReadOnlyBooleanWrapper(this,
			"performTestButtonOn", false);
	
	
	// Constructor.
	
	public CuteButtonsStatesManager(){
		// Bind properties.
		okButtonOn.bind(needToScheduleCuteElementReinit.not().and(errorsExist.not()));
		applyButtonOn.bind(needToScheduleCuteElementReinit.not().and(isInputDiffersAndValid));
		performTestButtonOn
				.bind(okButtonOn.and(performTestButtonAllowed).and(currentlyTesting.not()));
	}
	
	
	// Internal logic.
	
	/**
	 * Accepts information about if one of controls in {@link ElementEditorController} has different
	 * value from it's original value. Based on the input all related properties will be
	 * recalculated. Based on recalculated values of some of the properties will tell if it thinks
	 * that reinitialization of {@link CuteElement} is needed.
	 *
	 * @param c
	 *            the {@link Control} (User editable field)
	 * @param b
	 *            tells if {@link Control}'s value different from it's original value<br>
	 *            (The user has provided new value for the {@link Control} different from the
	 *            original value)
	 * 
	 * @return True if reinitialization of CuteElement is requested by current report. False
	 *         if reinitialization is not needed.
	 */
	private boolean reportChangeStatusOfControl(Control c, boolean b) {
		boolean changesMadeBeforeRecalculation = changesMade.get();
		recalculateStuffBasedOnStateOfControl(c, b, changesMadeMap, changesMade);
		// Let's reinit when values changed back to original ones. The variable 
		// changesMadeBeforeRecalculation indicates that. Of course there should be no errors.
		if (changesMade.get() || changesMadeBeforeRecalculation){
			return true;
		}
		return false;
	}

	/**
	 * Accepts information about if one of controls in {@link ElementEditorController} has wrong
	 * value or not. Based on the input all related properties will be recalculated. Based on
	 * recalculated {@link #errorsExist} property will tell if it thinks that reinitialization of
	 * {@link CuteElement} is allowed or denied.
	 *
	 * @param c
	 *            the {@link Control} (User editable field)
	 * @param b
	 *            tells if {@link Control}'s value is wrong
	 * 
	 * @return True if reinitialization of CuteElement is allowed by this report. False if
	 *         reinitialization is denied.
	 */
	private boolean reportErrorStatusOfControl(Control c, boolean b) {
		recalculateStuffBasedOnStateOfControl(c, b, errorExistMap, errorsExist);
		return !errorsExist.get();
	}

	/**
	 * Accepts the report of {@link Control} (user editable field) and it's state. <br>
	 * The state of the Control is a boolean variable and tells about something important, something
	 * that affects the provided property. <br>
	 * Keeps tracks of Controls and their states in the provided map. If the Control-State pair is
	 * different from the pair stored in the map of states, recalculates {@link #changesMade}, 
	 * {@link #errorsExist} and {@link #isInputDiffersAndValid} properties based on all information
	 * that was acquired in the map so far.
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
	private void recalculateStuffBasedOnStateOfControl(Control control, boolean state,
			Map<Control, Boolean> mapOfStates, BooleanProperty affectedProperty) {
		boolean needToRecalculateProperty = putControlBooleanPairInMap(control, state, mapOfStates);
		if (needToRecalculateProperty) {
			boolean needToRecalculateWholeInputStatus = updatePropertyBasedOnMapValues(mapOfStates,
					affectedProperty);
			if (needToRecalculateWholeInputStatus) {
				isInputDiffersAndValid.set(calculateIfInputDiffersAndValid());
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

	
	// ReadOnlyProperties made available outside.
	
	/**
	 * Tells if "OK" button should be enabled.
	 *
	 * @return ReadOnlyBooleanProperty
	 */
	public ReadOnlyBooleanProperty okButtonOnProperty() {
		return okButtonOn.getReadOnlyProperty();
	}
	
	/**
	 * Tells if "Perform Test" button should be enabled.
	 *
	 * @return ReadOnlyBooleanProperty
	 */
	public ReadOnlyBooleanProperty performTestButtonOnProperty() {
		return performTestButtonOn.getReadOnlyProperty();
	}

	/**
	 * Tells if "Apply" button should be enabled.
	 *
	 * @return ReadOnlyBooleanProperty
	 */
	public ReadOnlyBooleanProperty applyButtonOnProperty() {
		return applyButtonOn.getReadOnlyProperty();
	}

	/**
	 * Tells if the current {@link CuteElement}'s reinitialization should be scheduled. After
	 * initialization the method {@link #setCuteElementAsInitialized()} should be called.
	 *
	 * @return ReadOnlyBooleanProperty
	 */
	public ReadOnlyBooleanProperty needToScheduleCuteElementReinitProperty() {
		return needToScheduleCuteElementReinit.getReadOnlyProperty();
	}
	
	/**
	 * Tells if the current {@link CuteElement}'s scheduled reinitialization should be cancelled if
	 * it was scheduled.
	 * After making sure the reinitialization isn't scheduled the method
	 * {@link #setCuteElementReinitSuccessfullyCancelled()} should be called.
	 *
	 * @return ReadOnlyBooleanProperty
	 */
	public ReadOnlyBooleanProperty needToCancelScheduledCuteElementReinitProperty() {
		return needToCancelScheduledCuteElementReinit.getReadOnlyProperty();
	}
	
	
	// Getters of values of private properties.
	
	/**
	 * See the description of {@link #changesMade} property.
	 *
	 * @return true, if there are changes.
	 */
	public boolean areChangesMade() {
		return changesMade.get();
	}
	
	
	// Public methods.
	
	/**
	 * Clean up all Maps and Properties, so {@link #CuteButtonsStatesManager} can be used on the
	 * new {@link CuteElement}.
	 */
	public void reset() {
		changesMade.set(false);
		errorsExist.set(false);
		isInputDiffersAndValid.set(false);
		needToScheduleCuteElementReinit.set(false);
		needToCancelScheduledCuteElementReinit.set(false);
		changesMadeMap.clear();
		errorExistMap.clear();
	}

	/**
	 * This method reports to {@link CuteButtonsStatesManager} about existing errors and changes of
	 * value in chosen {@link Control} (user editable field). <br>
	 * The information from this and other reports will be used to determine if "OK", "Apply",
	 * "Cancel" and "Perform Test" buttons in {@link ElementEditorController} will be enabled or
	 * not. And much more - see public methods.
	 * 
	 * @Note "OK", "Apply", "Cancel" and "Perform Test" buttons also depend on current 
	 * {@link CuteElement}'s initialization status.
	 * 
	 * @param <T>
	 *            the generic type
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
		boolean valueDiffersFromOriginal = !newValue.equals(originalValue);
		boolean reinitializationRequestedByReport = 
				reportChangeStatusOfControl(c, valueDiffersFromOriginal);
		// Let's report if errors in this Control exist or not by getting information about existing
		// errors from finalValidationResult.
		boolean reinitializationAllowedByReport = true;
		if (finalValidationResult != null) {
			reinitializationAllowedByReport = reportErrorStatusOfControl(c,
					!finalValidationResult.getErrors().isEmpty());
		}
		if (reinitializationAllowedByReport) {
			// Initialization allowed. That means field validation has passed and we can proceed
			// with slow validation (initialization of CuteElement) if we want. 
			if (reinitializationRequestedByReport) {
				// So we want to do reinitialization. To request it, we need to set
				// needToScheduleCuteElementReinit to true.
				if (needToScheduleCuteElementReinit.get()) {
					// So it's already true. We need to invalidate property by setting it to false,
					// so the InvalidationListener will be notified even when
					// needToScheduleCuteElementReinit is true again.
					// TODO: try to live with shame until finding a better solution.
					needToScheduleCuteElementReinit.set(false);
				}
				needToScheduleCuteElementReinit.set(true);
			} 
		} else {
			// Errors in field validation exist, so initialization is not allowed.
			if (needToScheduleCuteElementReinit.get()) {
				// Oh no! Seems like reinitialization was scheduled before! We need to cancel it.
				if (needToCancelScheduledCuteElementReinit.get()) {
					// So it's already true. We need to invalidate property by setting it to false,
					// so the InvalidationListener will be notified even when
					// needToCancelScheduledCuteElementReinit is true again.
					// TODO: try to live with shame until finding a better solution.
					needToCancelScheduledCuteElementReinit.set(false);
				}
				needToCancelScheduledCuteElementReinit.set(true);
			}
		}
	}
	
	/**
	 * Makes {@link CuteButtonsStatesManager} to know that CuteElement is initialized.<br>
	 * The method should be called each time after
	 * {@link #needToScheduleCuteElementReinitProperty()} is changed to true because if not, the
	 * "OK", "Apply" and "Perform test" buttons will be disabled.
	 * 
	 * For more details see {@link #needToScheduleCuteElementReinit}.
	 */
	public void setCuteElementAsInitialized(){
		needToScheduleCuteElementReinit.set(false);
	}
	
	/**
	 * Makes {@link CuteButtonsStatesManager} to know that CuteElement's scheduled initialization
	 * was canceled. <br>
	 * The method should be called each time after
	 * {@link #needToCancelScheduledCuteElementReinitProperty()} is changed to true.
	 * 
	 * For more details see {@link #needToCancelScheduledCuteElementReinit}.
	 */
	public void setCuteElementReinitSuccessfullyCancelled(){
		needToCancelScheduledCuteElementReinit.set(false);
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
