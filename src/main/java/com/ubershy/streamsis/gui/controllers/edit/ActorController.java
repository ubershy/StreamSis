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
package com.ubershy.streamsis.gui.controllers.edit;

import java.net.URL;
import java.util.ResourceBundle;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.ConstsAndVars;
import com.ubershy.streamsis.actors.Actor;
import com.ubershy.streamsis.gui.controllers.edit.AbstractCuteController;
import com.ubershy.streamsis.gui.helperclasses.IntegerTextField;
import com.ubershy.streamsis.project.CuteElement;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

/**
 * ActorController, the controller that allows to edit {@link Actor} in a panel.
 */
public class ActorController extends AbstractCuteController {

    /** The root node. */
    @FXML
    private GridPane root;

    /** The container for {@link #checkIntervalIntegerTextField}. */
    @FXML
    private Pane checkIntervalFieldContainer;

    /** The container for {@link #repeatIntervalIntegerTextField}. */
    @FXML
    private Pane repeatIntervalFieldContainer;

    /** The CheckBox for {@link Actor#doOnRepeatProperty()}. */
    @FXML
    private CheckBox repeatOnActionsCheckBox;

    /** The CheckBox for {@link Actor#doOffRepeatProperty()}. */
    @FXML
    private CheckBox repeatOffActionsCheckBox;

    /** The IntegerTextField for editing {@link Actor#checkIntervalProperty()}. */
    private IntegerTextField checkIntervalIntegerTextField = new IntegerTextField(10000000, false);
    
    /** The IntegerTextField for editing {@link Actor#repeatIntervalProperty()}. */
    private IntegerTextField repeatIntervalIntegerTextField = new IntegerTextField(10000000, true);
	
    /** The {@link Actor} to edit. */
    private Actor actor;

    /** Saved original value of {@link Actor#doOnRepeatProperty()}. */
    private boolean origRepeatOnActions;
	
    /** Saved original value of {@link Actor#doOffRepeatProperty()}. */
    private boolean origRepeatOffActions;
	
    /** Saved original value of {@link Actor#checkIntervalProperty()}. */
    private int origCheckInterval;
	
    /** Saved original value of {@link Actor#repeatIntervalProperty()}. */
    private int origRepeatInterval;
	
    /** The Validation Support for this controller. */
    private ValidationSupport validationSupport;

	/*
	 * @inheritDoc
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		repeatIntervalIntegerTextField.disableProperty().bind((repeatOffActionsCheckBox
				.selectedProperty().or(repeatOnActionsCheckBox.selectedProperty())).not());
		checkIntervalFieldContainer.getChildren().add(checkIntervalIntegerTextField);
		repeatIntervalFieldContainer.getChildren().add(repeatIntervalIntegerTextField);
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public Node getView() {
		return root;
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void bindToCuteElement(CuteElement element) {
		actor = (Actor) element;
		// Remember original values of the new Actor's properties.
		origRepeatOnActions = actor.getDoOnRepeat();
		origRepeatOffActions = actor.getDoOffRepeat();
		origCheckInterval = actor.getCheckInterval();
		origRepeatInterval = actor.getRepeatInterval();
		// Bind to the new Actor.
		bindBidirectionalAndRemember(checkIntervalIntegerTextField.numberProperty(), actor.checkIntervalProperty());
		bindBidirectionalAndRemember(repeatIntervalIntegerTextField.numberProperty(), actor.repeatIntervalProperty());
		bindBidirectionalAndRemember(repeatOnActionsCheckBox.selectedProperty(), actor.doOnRepeatProperty());
		bindBidirectionalAndRemember(repeatOffActionsCheckBox.selectedProperty(), actor.doOffRepeatProperty());
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void unbindFromCuteElement() {
		unbindAllRememberedBinds();
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void setValidationSupport(ValidationSupport validationSupport) {
		this.validationSupport = validationSupport;
		this.validationSupport.registerValidator(checkIntervalIntegerTextField,
				generateValidatorForIntervalTextField(origCheckInterval));
		this.validationSupport.registerValidator(repeatIntervalIntegerTextField,
				generateValidatorForIntervalTextField(origRepeatInterval));
		Validator<Boolean> RepeanOnActionsCheckBoxValidator = (c, newValue) -> {
			ValidationResult finalResult = ValidationResult.fromErrorIf(c,
					"Always successful result", false);
			buttonStateManager.reportNewValueOfControl(origRepeatOnActions, newValue, c,
					finalResult);
			return finalResult;
		};
		Validator<Boolean> RepeanOffActionsCheckBoxValidator = (c, newValue) -> {
			ValidationResult finalResult = ValidationResult.fromErrorIf(c,
					"Always successful result", false);
			buttonStateManager.reportNewValueOfControl(origRepeatOffActions, newValue, c,
					finalResult);
			return finalResult;
		};
		this.validationSupport.registerValidator(repeatOnActionsCheckBox,
				RepeanOnActionsCheckBoxValidator);
		this.validationSupport.registerValidator(repeatOffActionsCheckBox,
				RepeanOffActionsCheckBoxValidator);
	}
	
	
	/**
	 * Checks if interval is less than {@link ConstsAndVars#minimumCheckInterval}, which is bad.
	 *
	 * @param interval The interval to check.
	 * @return True, if interval is less than {@link ConstsAndVars#minimumCheckInterval}.
	 */
	private boolean checkIfIntervalBigEnough(int interval) {
		if (interval < ConstsAndVars.minimumCheckInterval) {
			return true;
		}
		return false;
	}
	
	/**
	 * Generate {@link Validator} for IntegerTextField containing time interval.
	 *
	 * @param originalInterval
	 *            The original value of IntegerTextField.
	 * @return the Validator.
	 */
	private Validator<String> generateValidatorForIntervalTextField(int originalInterval) {
		Validator<String> intervalFieldValidator = (c, newValue) -> {
			IntegerTextField tf = (IntegerTextField) c;
			int number = tf.numberProperty().get();
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"This field can't be empty.", newValue.isEmpty());
			ValidationResult justMinusResult = ValidationResult.fromErrorIf(c,
					"Oh, come on, you can't input just minus!", newValue.equals("-"));
			ValidationResult notBigEnoughResult = ValidationResult.fromErrorIf(c,
					"The number can't be smaller than " + ConstsAndVars.minimumCheckInterval + ".",
					checkIfIntervalBigEnough(number));
			ValidationResult finalResult = ValidationResult.fromResults(emptyResult,
					notBigEnoughResult, justMinusResult);
			buttonStateManager.reportNewValueOfControl(originalInterval, number, c,
					finalResult);
			return finalResult;
		};
		return intervalFieldValidator;
	}

}
