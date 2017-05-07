/** 
 * StreamSis
 * Copyright (C) 2017 Eva Balycheva
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
package com.ubershy.streamsis.gui.controllers.editor.actions;

import java.net.URL;
import java.util.ResourceBundle;

import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.actions.RegionSwitchAction;
import com.ubershy.streamsis.gui.StreamSisAppFactory;
import com.ubershy.streamsis.gui.StreamSisAppFactory.LittleCuteControllerType;
import com.ubershy.streamsis.gui.controllers.editor.AbstractCuteController;
import com.ubershy.streamsis.gui.controllers.editor.CuteElementController;
import com.ubershy.streamsis.gui.controllers.editor.littlethings.CoordinatesController;
import com.ubershy.streamsis.gui.controllers.editor.littlethings.SimilarityController;
import com.ubershy.streamsis.gui.helperclasses.CuteButtonsStatesManager;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class RegionSwitchActionController extends AbstractCuteController
		implements CuteElementController {

    @FXML
    private GridPane root;

    @FXML
    private VBox coordsVBox;

    @FXML
    private VBox similarityVBox;
    
    @FXML
    private CheckBox findBestCheckBox;

    /** The {@link RegionSwitchAction} to edit. */
	protected RegionSwitchAction RSAction;
	
	/** The original {@link RegionSwitchAction} to compare values with {@link #RSAction}. */
	protected RegionSwitchAction origRSAction;
	
	protected ValidationSupport validationSupport;
	
	protected CoordinatesController coordsController = (CoordinatesController) StreamSisAppFactory
			.buildLittleCuteController(LittleCuteControllerType.COORDINATES);
	
	protected SimilarityController simController = (SimilarityController) StreamSisAppFactory
			.buildLittleCuteController(LittleCuteControllerType.SIMILARITY);

	/*
	 * @inheritDoc
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		coordsVBox.getChildren().add(coordsController.getView());
		similarityVBox.getChildren().add(simController.getView());
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void bindToCuteElement(CuteElement editableCopyOfCE, CuteElement origCE) {
		RSAction = (RegionSwitchAction) editableCopyOfCE;
		origRSAction = (RegionSwitchAction) origCE;
		bindBidirectionalAndRemember(findBestCheckBox.selectedProperty(),
				RSAction.findingBestProperty());
		simController.bindToSimilarity(RSAction.similarityProperty(),
				origRSAction.similarityProperty());
		coordsController.bindToCoordinates(RSAction.getCoords(), origRSAction.getCoords());
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void unbindFromCuteElement() {
		unbindAllRememberedBinds();
		coordsController.unbindFromCoordinates();
		simController.unbindFromCoordinates();
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
		coordsController.setValidationSupport(validationSupport);
		simController.setValidationSupport(validationSupport);
		Validator<Boolean> findBestCheckBoxValidator = (c, newValue) -> {
			ValidationResult alwaysSuccessfulResult = GUIUtil.fakeSuccessfulValidationResult(c);
			buttonStateManager.reportNewValueOfControl(origRSAction.isFindingBest(),
					newValue, c, alwaysSuccessfulResult);
			return alwaysSuccessfulResult;
		};
		this.validationSupport.registerValidator(findBestCheckBox, findBestCheckBoxValidator);
		ValidationSupport.setRequired(findBestCheckBox, false);
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void setCuteButtonsStatesManager(CuteButtonsStatesManager buttonStateManager) {
		super.setCuteButtonsStatesManager(buttonStateManager);
		coordsController.setCuteButtonsStatesManager(buttonStateManager);
		simController.setCuteButtonsStatesManager(buttonStateManager);
	}

}
