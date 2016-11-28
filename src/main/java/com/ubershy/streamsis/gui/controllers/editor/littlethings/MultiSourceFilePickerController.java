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
package com.ubershy.streamsis.gui.controllers.editor.littlethings;

import java.net.URL;
import java.util.ResourceBundle;

import com.ubershy.streamsis.elements.parts.MultiSourceFileLister;
import com.ubershy.streamsis.elements.parts.MultiSourceFilePicker;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

/**
 * MultiSourceFilePickerController, the controller that allows to edit
 * {@link MultiSourceFilePicker} in Element Editor panel.
 */
public class MultiSourceFilePickerController extends MultiSourceFileListerController {

	@FXML
	private CheckBox chooseFilesRandomlyCheckBox;

	/** The {@link MultiSourceFilePicker} to edit. */
	protected MultiSourceFilePicker picker;

	/** The original {@link MultiSourceFilePicker} to compare values with {@link #picker}. */
	protected MultiSourceFilePicker origPicker;
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);
	}
	
	/**
	 * Sets the {@link MultiSourceFilePicker} to work with and binds view's controls to
	 * MultiSourceFilePicker's properties. Before using this method you should first invoke
	 * {@link #replaceFileTypeNameTagInLabeledControls(String)} method.
	 *
	 * @param editableCopyOfPicker
	 *            The MultiSourceFilePicker to edit. (Actually a copy of the MultiSourceFilePicker
	 *            the user wishes to edit. The changes made in the copy will be transferred to
	 *            original MultiSourceFilePicker once the user hit "Apply" or "OK" button).
	 * @param origPicker
	 *            The Original MultiSourceFilePicker to use as storage of original values of
	 *            CuteElement's attributes. Should not be edited in controllers.
	 * @throws RuntimeException
	 *             In case {@link #replaceFileTypeNameTagInLabeledControls(String)} wasn't used
	 *             before this method.
	 */
	public void bindToMultiSourceFilePicker(MultiSourceFilePicker editableCopyOfPicker,
			MultiSourceFilePicker origPicker) {
		super.bindToMultiSourceFileLister(editableCopyOfPicker, origPicker);
		this.picker = editableCopyOfPicker;
		this.origPicker = origPicker;
		// Set up bidirectional bindings.
		bindBidirectionalAndRemember(chooseFilesRandomlyCheckBox.selectedProperty(),
				editableCopyOfPicker.pickFilesRandomlyProperty());
		chooseFilesRandomlyCheckBox.selectedProperty().addListener((o, oldVal, newVal) -> {
			buttonStateManager.reportNewValueOfControl(origPicker.isPickFilesRandomly(),
					newVal, chooseFilesRandomlyCheckBox, null);
		});
	}
	
	@Override
	public void bindToMultiSourceFileLister(MultiSourceFileLister editableCopyOfLister,
			MultiSourceFileLister origLister) {
		throw new UnsupportedOperationException(
				"Use bindToMultiSourceFilePicker() method instead.");
	}

	@Override
	public void unbindFromMultiSourceFileLister() {
		throw new UnsupportedOperationException(
				"Use unbindFromMultiSourceFilePicker() method instead.");
	}
	
	public void unbindFromMultiSourceFilePicker() {
		super.unbindFromMultiSourceFileLister();
		unbindAllRememberedBinds();
	}
	
}
