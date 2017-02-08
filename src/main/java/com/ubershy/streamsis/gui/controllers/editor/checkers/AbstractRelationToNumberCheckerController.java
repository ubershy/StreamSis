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
package com.ubershy.streamsis.gui.controllers.editor.checkers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.controlsfx.control.SegmentedButton;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.checkers.numeric.RelationToNumberChecker;
import com.ubershy.streamsis.elements.checkers.numeric.AbstractRelationToNumberChecker.BooleanNumberOperator;
import com.ubershy.streamsis.gui.controllers.editor.AbstractCuteController;
import com.ubershy.streamsis.gui.controllers.editor.CuteElementController;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.gui.helperclasses.IntegerTextField;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public abstract class AbstractRelationToNumberCheckerController extends AbstractCuteController
		implements CuteElementController {

    @FXML
    private GridPane root;

    @FXML
    private Pane numberPane;

    @FXML
    private Pane operatorPane;
    
    @FXML
    private Label operatorHintLabel;

	/** The {@link RelationToNumberChecker} to edit. */
	protected RelationToNumberChecker relationChecker;

	/**
	 * The original {@link RelationToNumberChecker} to compare values with {@link #relationChecker}.
	 */
	protected RelationToNumberChecker origRelationChecker;
	
    /** The IntegerTextField for editing {@link RelationToNumberChecker#compareNumberProperty()}. */
    private IntegerTextField numberTextField = new IntegerTextField(100000000, false);
	
	protected ValidationSupport validationSupport;
	
	protected ToggleButton equalButton = new ToggleButton("EQUAL");
	protected ToggleButton notEqualButton = new ToggleButton("NOTEQUAL");
	protected ToggleButton greaterOrEqualButton = new ToggleButton("GREATEROREQUAL");
	protected ToggleButton greaterButton = new ToggleButton("GREATER");
	protected ToggleButton lessOrEqualButton = new ToggleButton("LESSOREQUAL");
	protected ToggleButton lessButton = new ToggleButton("LESS");
	protected SegmentedButton segmentedButton = new SegmentedButton(equalButton, greaterButton,
			lessButton, greaterOrEqualButton, lessOrEqualButton, notEqualButton);
	protected TreeMap<String, ToggleButton> mapWithButtons = new TreeMap<String, ToggleButton>();
	protected ChangeListener<? super Toggle> toggleListener = (o, oldVal, newVal) -> {
		if (relationChecker != null) {
			if (newVal != null) {
				String nameOfOperator = ((ToggleButton) newVal).getText();
				BooleanNumberOperator operator = BooleanNumberOperator.valueOf(nameOfOperator);
				relationChecker.setOperator(operator);
				buttonStateManager.reportNewValueOfControl(origRelationChecker.getOperator(),
						operator, segmentedButton, null);
				setOperatorHintBasedOnOperator(operator);
			} else {
				// Let's not allow having all toggles unselected.
				oldVal.setSelected(true);
			}
		}
	};
	
	protected String originalHint;
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		for (ToggleButton button: segmentedButton.getButtons()) {
			// Add button to map to find it conveniently later.
			mapWithButtons.put(button.getText(), button);
		}
		if (BooleanNumberOperator.values().length != segmentedButton.getButtons().size()) {
			throw new RuntimeException(
					"Operator buttons do not represent all avaliable operators");
		}
		operatorPane.getChildren().add(segmentedButton);
		numberPane.getChildren().add(numberTextField);
		originalHint = operatorHintLabel.getText();
	}

	private void setOperatorHintBasedOnOperator(BooleanNumberOperator operator) {
		String hintImportantPart = "";
		switch (operator) {
		case EQUAL:
			hintImportantPart = "equal to";
			break;
		case GREATER:
			hintImportantPart = "greater than";
			break;
		case GREATEROREQUAL:
			hintImportantPart = "greater or equal to";
			break;
		case LESS:
			hintImportantPart = "less than";
			break;
		case LESSOREQUAL:
			hintImportantPart = "less or equal to";
			break;
		case NOTEQUAL:
			hintImportantPart = "not equal to";
			break;
		default:
			break;
		}
		operatorHintLabel.setText(hintImportantPart);
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void bindToCuteElement(CuteElement editableCopyOfCE, CuteElement origCE) {
		relationChecker = (RelationToNumberChecker) editableCopyOfCE;
		origRelationChecker = (RelationToNumberChecker) origCE;
		
		BooleanNumberOperator operator = relationChecker.getOperator();
		setOperatorHintBasedOnOperator(operator);
		
		ToggleButton t = mapWithButtons.get(operator.name());
		t.setSelected(true);
		ToggleGroup tg = segmentedButton.getToggleGroup();
		tg.selectedToggleProperty().addListener(toggleListener);
		
		numberTextField.numberProperty().bindBidirectional(relationChecker.compareNumberProperty());
		
		// Let's set descriptions for operator popovers.
		for (ToggleButton button: segmentedButton.getButtons()) {
			BooleanNumberOperator ButtonOperator = BooleanNumberOperator.valueOf(button.getText());
			String description = relationChecker.getFullDescriptionOfOperator(ButtonOperator);
			// Create popover (kind of tooltip) with description for each button.
			Label popOverLabel = new Label(description);
			popOverLabel.setPadding(new Insets(5,5,5,5));
			GUIUtil.setPopOverTooltipToNode(button, popOverLabel, -7);
		}
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void unbindFromCuteElement() {
		unbindAllRememberedBinds();
		ToggleGroup tg = segmentedButton.getToggleGroup();
		tg.selectedToggleProperty().removeListener(toggleListener);
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
	public void setValidationSupport(ValidationSupport validationSupport) {
		this.validationSupport = validationSupport;
		Validator<String> numberValidator = (c, newValue) -> {
			IntegerTextField tf = (IntegerTextField) c;
			int number = tf.numberProperty().get();
			ValidationResult emptyResult = ValidationResult.fromErrorIf(c,
					"This field can't be empty.", newValue.isEmpty());
			buttonStateManager.reportNewValueOfControl(origRelationChecker.getCompareNumber(),
					number, c, emptyResult);
			return emptyResult;
		};
		this.validationSupport.registerValidator(numberTextField, numberValidator);
	}

}
