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
package com.ubershy.streamsis.gui.controllers.editor;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import org.controlsfx.control.SegmentedButton;
import org.controlsfx.tools.ValueExtractor;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.controlsfx.validation.decoration.CompoundValidationDecoration;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.ElementInfo;
import com.ubershy.streamsis.elements.SisScene;
import com.ubershy.streamsis.elements.actors.Actor;
import com.ubershy.streamsis.gui.StreamSisAppFactory;
import com.ubershy.streamsis.gui.controllers.ElementEditorController;
import com.ubershy.streamsis.gui.helperclasses.CuteButtonsStatesManager;
import com.ubershy.streamsis.gui.helperclasses.CuteGraphicValidationDecoration;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.project.ProjectManager;

import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;

/**
 * The CommonElementFieldsController. <br>
 * This controller allows editing some {@link CuteElement}'s fields which are shared between all
 * types of CuteElements such as their names. <br>
 * Meant to be stored inside {@link ElementEditorController}.
 * Contains one of the {@link CuteElementController}s inside which allow to edit fields specific to
 * concrete CuteElements.
 */
public class CommonElementFieldsController implements Initializable {
	
	static final Logger logger = LoggerFactory.getLogger(CommonElementFieldsController.class);

	private CuteButtonsStatesManager buttonStateManager;

	private CuteElementController currentCuteElementController;

	private CuteElement elementWorkingCopy;
	
	@FXML
	private TextField nameTextField;

	private String originalName;
	
	@FXML
	private GridPane root;

	private ValidationSupport validationSupport;
	
	private boolean inputAllowed = true;

	private boolean allowEmptyName = false;
	
	public Node getView() {
		return root;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Set custom value extractors for validations of specific Controls.
		ValueExtractor.addObservableValueExtractor(c -> c instanceof SegmentedButton,
				c -> ((SegmentedButton) c).getToggleGroup().selectedToggleProperty());

		root.sceneProperty().addListener((InvalidationListener) o -> {
			if (root.getScene() != null) {
				String url = CommonElementFieldsController.class.getResource("/css/validation.css")
						.toExternalForm();
				root.getScene().getStylesheets().add(url);
			}
		});
		recreateNameTextField();
	}

	public void setCuteButtonsStatesManager(CuteButtonsStatesManager buttonStateManager) {
		this.buttonStateManager = buttonStateManager;
	}

	public void connectToCuteElement(CuteElement editableCopyOfCE, CuteElement origCE) {
		disconnectFromConnectedCuteElement();
		// nameTextField should be recreated before invoking recreateValidationSupport() method.
		recreateNameTextField();
		// Let's remember the reference to the CuteElement's copy
		elementWorkingCopy = editableCopyOfCE;
		// Let's remember original name of the CuteElement. At this point editableCopyOfCE should
		// be equal to origCE. So it doesn't matter from where to get the original value of name.
		ElementInfo newInfoOfCopy = editableCopyOfCE.getElementInfo();
		originalName = newInfoOfCopy.getName();
		// Let's get the new controller specific to CuteElement's type
		currentCuteElementController = StreamSisAppFactory
				.buildCuteElementControllerBasedOnCuteElement(editableCopyOfCE);
		// Let's set the new controller and view for the cell
		currentCuteElementController.setCuteButtonsStatesManager(buttonStateManager);
		// Bind element to CuteElementController's View from which the user can edit
		// subtype-specific variables of the CuteElement's copy. Modified CuteElement's copy will be
		// used to spread changes to original CuteElement.
		currentCuteElementController.bindToCuteElement(editableCopyOfCE, origCE);
		// Let's bind nameTextField to CuteElement's name before invoking
		// recreateValidationSupport() method. 
		nameTextField.setText(newInfoOfCopy.getName());
		nameTextField.textProperty().bindBidirectional(newInfoOfCopy.nameProperty());
		// This will update validationSupport and validationListener variables.
		recreateValidationSupport();
		// Give new validationSupport to currentCuteElementController, so it can validate input in
		// fields.
		currentCuteElementController.setValidationSupport(validationSupport);
		validationSupport.initInitialDecoration();
		root.add(currentCuteElementController.getView(), 0, 1);
		if (buttonStateManager.needToScheduleCuteElementReinitProperty().get())
			editableCopyOfCE.init();
			buttonStateManager.setCuteElementAsInitialized();
	}

	/**
	 * Replaces {@link #nameTextField} responsible for editing name with new one. <br>
	 * It's needed because when we do {@link #recreateValidationSupport()}, the old
	 * ValidationSupport object still have registered validator to nameTextField. So to garbage
	 * collect old ValidationSupport object also we have to dump nameTextField with it.
	 */
	private void recreateNameTextField() {
		TextField newTextField = new TextField();
		newTextField.setMaxWidth(250.0);
		newTextField.setPrefWidth(250.0);
		GUIUtil.replaceChildInPane(nameTextField, newTextField);
		nameTextField = newTextField;
		nameTextField.setDisable(!inputAllowed);
		// Let's not allow very long names
		UnaryOperator<Change> filter = c -> {
			if (c.getControlNewText().length() > 35) {
				logger.debug("The CuteElement's name is too long to pass the input filter.");
				return null;
			}
			return c;
		};
		nameTextField.setTextFormatter(new TextFormatter<>(filter));
		// Reset nameTextField on ESC key
		nameTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent t) {
				if (t.getCode() == KeyCode.ESCAPE) {
					nameTextField.setText(originalName);
					// When root's parent is focused, second hit to Escape key will close panel
					root.getParent().requestFocus();
				}
			}
		});
	}

	private void disconnectFromConnectedCuteElement() {
		// Let's unbind name text field from previous CuteElement's name
		if (elementWorkingCopy != null) {
			nameTextField.textProperty()
					.unbindBidirectional(elementWorkingCopy.getElementInfo().nameProperty());
		}
		// Let's unbind currentCuteElementController's fields from previous CuteElement's properties
		if (currentCuteElementController != null) {
			currentCuteElementController.unbindFromCuteElement();
		}
		// Let's clean the cell from the previous view, if it exists
		if (currentCuteElementController != null) {
			root.getChildren().remove(currentCuteElementController.getView());
		}
	}

	private boolean validateNameAlreadyExistence(Control c, String newValue) {
		if (newValue.equals(originalName))
			return false;
		if (elementWorkingCopy instanceof SisScene) {
			SisScene existingSisScene = ProjectManager.getProject().getSisSceneByName(newValue);
			if (existingSisScene != null) {
				if (existingSisScene != elementWorkingCopy) {
					// So SisScene with such name already exists, it's bad
					return true;
				}
			}
		}
		if (elementWorkingCopy instanceof Actor) {
			Actor existingActor = ProjectManager.getProject().getActorByName(newValue);
			if (existingActor != null) {
				if (existingActor != elementWorkingCopy) {
					// So Actor with such name already exists, it's bad
					return true;
				}
			}
		}
		// Elements of other types can have same names, so no error
		return false;
	}
	
	/**
	 * Recreates {@link #validationSupport}, so the old validationSupport which is bound to unwanted
	 * fields can be garbage collected.
	 */
	private void recreateValidationSupport() {
		validationSupport = new ValidationSupport();
		CuteGraphicValidationDecoration cuteGVD = new CuteGraphicValidationDecoration(
				validationSupport);
		CompoundValidationDecoration validationDecor = new CompoundValidationDecoration(
				// These styles are located in /StreamSis/src/main/resources/css/validation.css
				new StyleClassValidationDecoration("cuteerror", "cutewarning"), cuteGVD);
		validationSupport.setValidationDecorator(validationDecor);
		Validator<String> nameTextFieldValidator = (c, newValue) -> {
			ValidationResult alreadyExistanceResult = ValidationResult.fromErrorIf(c,
					"The name is already taken. Please choose another one",
					validateNameAlreadyExistence(c, newValue));
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"Please choose a name for the element", !allowEmptyName && newValue.equals(""));
			ValidationResult finalResult = ValidationResult.fromResults(emptyResult,
					alreadyExistanceResult);
			buttonStateManager.reportNewValueOfControl(originalName, newValue, c, finalResult);
			return finalResult;
		};
		validationSupport.registerValidator(nameTextField, nameTextFieldValidator);
	}

	/**
	 * Allows or restricts input in this view and children views.
	 * 
	 * @param allowInput True to allow input, False to restrict input.
	 */
	public void setInputAllowed(boolean allowInput) {
		inputAllowed = allowInput;
		nameTextField.setDisable(!allowInput);
		if (currentCuteElementController != null) {
			currentCuteElementController.setInputAllowed(allowInput);
		}
	}

	/**
	 * Allows or restricts input of empty name in {@link #nameTextField}.
	 * 
	 * @param allowEmptyName
	 *            True to allow input of empty name, False to restrict input of empty name.
	 */
	public void setEmptyNameAllowed(boolean allowEmptyName) {
		this.allowEmptyName  = allowEmptyName;
	}

}
