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
package com.ubershy.streamsis.gui.controllers.edit.checkers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.checkers.Checker;
import com.ubershy.streamsis.checkers.LogicalChecker;
import com.ubershy.streamsis.checkers.LogicalChecker.BooleanOperator;
import com.ubershy.streamsis.gui.controllers.CuteElementController;
import com.ubershy.streamsis.gui.controllers.edit.AbstractCuteController;
import com.ubershy.streamsis.project.CuteElement;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class LogicalCheckerController extends AbstractCuteController
		implements CuteElementController {

	@FXML
	private GridPane root;
    
    @FXML
    private HBox operatorsHBox;

    /** The {@link LogicalChecker} to edit. */
	protected LogicalChecker logicalChecker;
	
	/** The original {@link LogicalChecker} to compare values with {@link #logicalChecker}. */
	protected LogicalChecker origLogicalChecker;
	
	protected ValidationSupport validationSupport;
	
	protected ToggleButton notButton = new ToggleButton("NOT");
	protected ToggleButton xorButton = new ToggleButton("XOR");
	protected ToggleButton andButton = new ToggleButton("AND");
	protected ToggleButton orButton = new ToggleButton("OR");
	protected SegmentedButton segmentedButton = new SegmentedButton(notButton, xorButton,
			andButton, orButton);
	protected TreeMap<String, ToggleButton> mapWithButtons = new TreeMap<String, ToggleButton>();
	protected ChangeListener<? super Toggle> toggleListener = (o, oldVal, newVal) -> {
		if (logicalChecker != null) {
			if (newVal != null) {
				String nameOfOperator = ((ToggleButton) newVal).getText();
				logicalChecker.setOperator(BooleanOperator.valueOf(nameOfOperator));
			} else {
				// Let's not allow having all toggles unselected.
				oldVal.setSelected(true);
			}
		}
	};
	
	ListChangeListener<Checker> childrenListener = new ListChangeListener<Checker>() {
		@Override
		public void onChanged(ListChangeListener.Change<? extends Checker> c) {
			// Need to do revalidation. It can be done by reselecting toggle...
			Toggle currentTogle = segmentedButton.getToggleGroup().getSelectedToggle();
			if (currentTogle != null) {
				currentTogle.setSelected(false);
				currentTogle.setSelected(true);
			}
		}
	};

	/*
	 * @inheritDoc
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		for (ToggleButton button: segmentedButton.getButtons()) {
			// Add button to map to find it conveniently later.
			mapWithButtons.put(button.getText(), button);
			// Get description of logical operation.
			BooleanOperator operator = BooleanOperator.valueOf(button.getText());
			String description = operator.toString();
			// Create popover (kind of tooltip) with description for each button.
			PopOver over = new PopOver();
			over.setConsumeAutoHidingEvents(false);
			over.setAutoHide(false);
			over.setDetachable(true);
			over.setArrowLocation(ArrowLocation.BOTTOM_CENTER);
			Label popOverLabel = new Label(description);
			popOverLabel.setPadding(new Insets(5,5,5,5));
			over.setContentNode(popOverLabel);
			// This tooltip's behavior of showing will be used to show popover at right time.
			Tooltip invisibleTooltip = new Tooltip();
			invisibleTooltip.setAutoHide(false);
			invisibleTooltip.setConsumeAutoHidingEvents(false);
			invisibleTooltip.setOpacity(0.0);
			invisibleTooltip.setOnShown((e) -> {
				over.show(button, -7);
			});
			invisibleTooltip.setOnHidden((e) -> {
				over.hide();
			});
			button.setTooltip(invisibleTooltip);
		}
		if (LogicalChecker.BooleanOperator.values().length != segmentedButton.getButtons().size()) {
			throw new RuntimeException(
					"Operation buttons do not represent avaliable logical operations");
		}
		operatorsHBox.getChildren().add(segmentedButton);
	}

	/*
	 * @inheritDoc
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void bindToCuteElement(CuteElement editableCopyOfCE, CuteElement origCE) {
		logicalChecker = (LogicalChecker) editableCopyOfCE;
		origLogicalChecker = (LogicalChecker) origCE;
		// Set same list of children to element's copy. This will help to count children and to show
		// proper message during init() of copy. It's safe since this we will not modify element's
		// list of children, but just use it.
		logicalChecker.setChildren(origLogicalChecker.getChildren());
		ToggleButton t = mapWithButtons.get(logicalChecker.getOperator().name());
		t.setSelected(true);
		ToggleGroup tg = segmentedButton.getToggleGroup();
		tg.selectedToggleProperty().addListener(toggleListener);
		logicalChecker.getChildren().addListener(childrenListener);
	}
	
	/*
	 * @inheritDoc
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void unbindFromCuteElement() {
		unbindAllRememberedBinds();
		ToggleGroup tg = segmentedButton.getToggleGroup();
		tg.selectedToggleProperty().removeListener(toggleListener);
		logicalChecker.getChildren().removeListener(childrenListener);
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
		Validator<Toggle> operationValidator = (c, newValue) -> {
			String whyBad = null;
			if (newValue != null) {
				String nameOfOperator = ((ToggleButton) newValue).getText();
				BooleanOperator operator = BooleanOperator.valueOf(nameOfOperator);
				if (logicalChecker != null) {
					whyBad = logicalChecker
							.whyOperatorCantBeAppliedToCurrentAmountOfCheckers(operator);
				}
			}
			ValidationResult wrongResult = ValidationResult.fromErrorIf(c,
					whyBad, whyBad != null);
			if (newValue != null) {
				ToggleButton origTB = mapWithButtons.get(origLogicalChecker.getOperator().name());
				buttonStateManager.reportNewValueOfControl(origTB, newValue, c, wrongResult);
			}
			return wrongResult;
		};
		this.validationSupport.registerValidator(segmentedButton, operationValidator);
	}

}
