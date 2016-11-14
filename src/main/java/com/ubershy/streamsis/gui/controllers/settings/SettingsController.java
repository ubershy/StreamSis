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
package com.ubershy.streamsis.gui.controllers.settings;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.decoration.CompoundValidationDecoration;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;

import com.ubershy.streamsis.CuteConfig;
import com.ubershy.streamsis.HotkeyManager;
import com.ubershy.streamsis.HotkeyManager.Hotkey;
import com.ubershy.streamsis.gui.helperclasses.CuteGraphicValidationDecoration;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.CheckBox;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SettingsController implements Initializable {

    @FXML
    private AnchorPane root;
    @FXML
    private VBox contentVBox;
    @FXML
    private CheckBox projectAutoLoadCheckBox;
    @FXML
    private CheckBox projectAutoStartCheckBox;
    @FXML
    private GridPane hotkeysGridPane;
    @FXML
    private ButtonBar buttonBar;
    private ValidationSupport validationSupport;
    private ArrayList<HotkeyRow> allHotkeyRows = new ArrayList<>();

	public AnchorPane getView() {
		return root;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		createValidationSupport();
		
		root.sceneProperty().addListener((InvalidationListener) o -> {
			if (root.getScene() != null) {
				String url = SettingsController.class.getResource("/css/streamsis.css")
						.toExternalForm();
				root.getScene().getStylesheets().add(url);
			}
		});

		projectAutoLoadCheckBox.selectedProperty().addListener((o, oldVal, newVal) -> {
			if (!newVal) {
				projectAutoStartCheckBox.setDisable(true);
				projectAutoStartCheckBox.setSelected(false);
			} else {
				projectAutoStartCheckBox.setDisable(false);
			}
		});
		
		fillHotkeysTab();
		
		initializeBottomButtons();

		setInitialValuesOfControls();
	}

	/**
	 * Creates {@link ValidationSupport} with special style applied.
	 */
	private void createValidationSupport() {
		validationSupport = new ValidationSupport();
		CuteGraphicValidationDecoration cuteGVD = new CuteGraphicValidationDecoration(
				validationSupport);
		CompoundValidationDecoration validationDecor = new CompoundValidationDecoration(
				// These styles are located in /StreamSis/src/main/resources/css/validation.css
				new StyleClassValidationDecoration("cuteerror", "cutewarning"), cuteGVD);
		validationSupport.setValidationDecorator(validationDecor);
	}

	/**
	 * Fills Hotkeys tab with fields and values to edit KeyCodeCombinations of Hotkeys.
	 */
	private void fillHotkeysTab() {
		int rowToFill = 1; // Start filling from second row, because first row is column names.
		for (Entry<Hotkey, ObjectProperty<KeyCodeCombination>> entry : HotkeyManager
				.getRegisteredHotkeys().entrySet()) {
			Hotkey hk = entry.getKey();
			KeyCodeCombination kcc = entry.getValue().get();
			HotkeyRow hkRow = new HotkeyRow(hk);
			allHotkeyRows.add(hkRow);
			hkRow.setCurrentKeyCodeCombination(kcc);
			hotkeysGridPane.addRow(rowToFill, hkRow.getDescriptionLabel(),
					hkRow.getKeyTextField(), hkRow.getModifiersTextField(),
					hkRow.getRestoreDefaultButton());
			hkRow.setValidationSupport(validationSupport);
			rowToFill++;
		}
	}

	/**
	 * Initializes "OK" and "Cancel" buttons.
	 */
	private void initializeBottomButtons() {
		Button okButton = new Button("OK");
		ButtonBar.setButtonData(okButton, ButtonData.OK_DONE);
		Button cancelButton = new Button("Cancel");
		ButtonBar.setButtonData(cancelButton, ButtonData.CANCEL_CLOSE);
		buttonBar.getButtons().addAll(okButton, cancelButton);
		okButton.addEventFilter(ActionEvent.ACTION, (event) -> {
			applySettings();
			((Stage) okButton.getScene().getWindow()).close();
		});
		cancelButton.addEventFilter(ActionEvent.ACTION, (event) -> {
			((Stage) okButton.getScene().getWindow()).close();
		});
		validationSupport.validationResultProperty().addListener((o, oldVal, newVal) -> {
			if (newVal.getErrors().isEmpty()) {
				// If no errors, enable okButton.
				okButton.setDisable(false);
			} else {
				// If errors, disable okButton.
				okButton.setDisable(true);
			}
		});
	}

	/**
	 * Sets the initial values of controls.
	 */
	private void setInitialValuesOfControls() {
		boolean projectAutoLoad = CuteConfig.getBoolean(CuteConfig.CUTE, "ProjectAutoLoad");
		projectAutoLoadCheckBox.setSelected(projectAutoLoad);
		if (projectAutoLoad) {
			projectAutoStartCheckBox
					.setSelected(CuteConfig.getBoolean(CuteConfig.CUTE, "ProjectAutoStart"));
		}
	}

	/**
	 * Applies new settings.
	 */
	private void applySettings() {
		CuteConfig.setBoolean(CuteConfig.CUTE, "ProjectAutoLoad",
				projectAutoLoadCheckBox.isSelected());
		CuteConfig.setBoolean(CuteConfig.CUTE, "ProjectAutoStart",
				projectAutoStartCheckBox.isSelected());
		for (HotkeyRow hr : allHotkeyRows) {
			Hotkey hk = hr.getHotkey();
			ObjectProperty<KeyCodeCombination> kccp = HotkeyManager
					.getKeyCodeCombinationPropertyOfHotkey(hk);
			KeyCodeCombination newKCC = hr.getCurrentKeyCodeCombination();
			kccp.set(newKCC);
			String toWriteToConfig;
			if (newKCC == null) {
				toWriteToConfig = "";
			} else {
				toWriteToConfig = newKCC.getName();
			}
			CuteConfig.setString(CuteConfig.HOTKEYS, hk.name(), toWriteToConfig);
		}
		CuteConfig.saveConfig();
	}

    @FXML
    void onRestoreDefaults(ActionEvent event) {
    	for (HotkeyRow hr : allHotkeyRows) {
    		hr.restoreDefaultValueOfKeyCodeCombination();
    	}
    }
}
