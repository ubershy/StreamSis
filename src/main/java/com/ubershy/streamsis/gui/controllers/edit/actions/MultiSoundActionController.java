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
package com.ubershy.streamsis.gui.controllers.edit.actions;

import java.net.URL;
import java.util.ResourceBundle;

import org.controlsfx.validation.ValidationSupport;
import com.ubershy.streamsis.actions.MultiSoundAction;
import com.ubershy.streamsis.gui.StreamSisAppFactory;
import com.ubershy.streamsis.gui.StreamSisAppFactory.LittleCuteControllerType;
import com.ubershy.streamsis.gui.controllers.CuteElementController;
import com.ubershy.streamsis.gui.controllers.edit.AbstractCuteController;
import com.ubershy.streamsis.gui.controllers.edit.littlethings.MultiSourceFilePickerController;
import com.ubershy.streamsis.gui.helperclasses.CuteButtonsStatesManager;
import com.ubershy.streamsis.project.CuteElement;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class MultiSoundActionController extends AbstractCuteController
		implements CuteElementController {

	@FXML
	private GridPane root;
	
    @FXML
    private Slider volumeSlider;
    
    @FXML
    private Label soundVolumeLabel;
    
    @FXML
    private HBox filePickerHBox;

    /** The {@link MultiSoundAction} to edit. */
	protected MultiSoundAction msoundAction;
	
	/** The original {@link MultiSoundAction} to compare values with {@link #soundAction}. */
	protected MultiSoundAction origmSoundAction;
	
	protected ChangeListener<? super Number> volumeListener = (o, oldVal, newVal) -> {
		buttonStateManager.reportNewValueOfControl(origmSoundAction.getVolume(),
				newVal, volumeSlider, null);
	};
	
	protected MultiSourceFilePickerController MSFPController = 
			(MultiSourceFilePickerController) StreamSisAppFactory
			.buildLittleCuteController(LittleCuteControllerType.MULTISOURCEFILEPICKER);
	
	protected ValidationSupport validationSupport;

	/*
	 * @inheritDoc
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		MSFPController.replaceFileTypeNameTagInLabeledControls("sound");
		filePickerHBox.getChildren().add(MSFPController.getView());
		String soundVolumeLabelOrigText = soundVolumeLabel.getText();
		soundVolumeLabel.setText(soundVolumeLabelOrigText + 100 + "%");
		volumeSlider.valueProperty().addListener((o, oldVal, newVal) -> {
			soundVolumeLabel.setText(soundVolumeLabelOrigText + newVal.intValue() + "%");
		});
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void bindToCuteElement(CuteElement editableCopyOfCE, CuteElement origCE) {
		msoundAction = (MultiSoundAction) editableCopyOfCE;
		origmSoundAction = (MultiSoundAction) origCE;
		volumeSlider.setValue(msoundAction.getVolume()*100.0);
		bindNormalAndRemember(msoundAction.volumeProperty(),
				volumeSlider.valueProperty().divide(100.0));
		volumeSlider.valueProperty().addListener(volumeListener);
		MSFPController.bindToMultiSourceFilePicker(msoundAction.getFilePicker(),
				origmSoundAction.getFilePicker());
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void unbindFromCuteElement() {
		unbindAllRememberedBinds();
		volumeSlider.valueProperty().removeListener(volumeListener);
		MSFPController.unbindFromMultiSourceFilePicker();
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
		MSFPController.setValidationSupport(validationSupport);
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void setCuteButtonsStatesManager(CuteButtonsStatesManager buttonStateManager) {
		super.setCuteButtonsStatesManager(buttonStateManager);
		MSFPController.setCuteButtonsStatesManager(buttonStateManager);
	}

}
