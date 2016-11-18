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
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.ubershy.streamsis.gui.controllers.editor.AbstractCuteController;

import javafx.beans.property.FloatProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;

/**
 * SimilarityController, the controller that allows to edit Similarity parameter in Element Editor
 * panel.
 */
public class SimilarityController extends AbstractCuteController {

    @FXML
    private GridPane root;
	
    @FXML
    private Label similarityLabel;

    @FXML
    private Slider similaritySlider;

    @FXML
    private Label similarityDescriptionLabel;
    
	private String similarityLabelOrigText;
    
  	/** The Similarity to edit. */
	protected FloatProperty similarity;

	/** The original Similarity to compare values with {@link #similarity}. */
	protected FloatProperty origSimilarity;

	protected ValidationSupport validationSupport;
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		similarityLabelOrigText = similarityLabel.getText();
		similarityLabel.setText(similarityLabelOrigText + 100 + "%");
	}
	
	/**
	 * Sets the Similarity property to work with and binds view's controls to it.
	 *
	 * @param editableCopyOfSimilarity
	 *            The Similarity to edit. (Actually a copy of the Similarity the user wishes to
	 *            edit. The changes made in the copy will be transferred to original Similarity
	 *            once the user hit "Apply" or "OK" button).
	 * @param origSimilarity
	 *            The Original Similarity. Should not be edited in controllers.
	 */
	public void bindToSimilarity(FloatProperty editableCopyOfSimilarity,
			FloatProperty origSimilarity) {
		this.similarity = editableCopyOfSimilarity;
		this.origSimilarity = origSimilarity;
		similaritySlider.setValue(this.similarity.get() * 100.0);
		bindNormalAndRemember(this.similarity, similaritySlider.valueProperty().divide(100.0));
	}

	public void unbindFromCoordinates() {
		unbindAllRememberedBinds();
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
		Validator<Number> similaritySliderValidator = (c, newValue) -> {
			// Not using newValue.intValue(), because we want rounded value, not truncated.
			int intValue = Math.round(newValue.floatValue());
			ValidationResult zeroResult = ValidationResult.fromErrorIf(c,
					"Zero minimum acceptable similarity means any random image on screen can be "
							+ "matched with Target image. It's pointless. Forget about it.",
					intValue < 1);
			ValidationResult lowResult = ValidationResult.fromWarningIf(c,
					"Minimum acceptable similarity is too low. This Checker will often react like "
					+ "it sees the Target image on screen when something else is on the screen. "
					+ "False positive result.",
					intValue >= 1 && intValue <= 40);
			int originalValue = Math.round(origSimilarity.get()*100);
			buttonStateManager.reportNewValueOfControl(originalValue, intValue, c, zeroResult);
			String similarityComment;
			if (intValue > 98) {
				similarityComment = "Find an exact match. May not recognize even identical image "
						+ "in some cases.";
			} else if (intValue >= 95) {
				similarityComment = "Find a precise match. Recommended to use.";
			} else if (intValue >= 70) {
				similarityComment = "Find a good match or better. Some risk of false positive "
						+ "results.";
			} else if (intValue >= 40) {
				similarityComment = "Find a bad match or better. "
						+ "High risk of false positive results.";
			} else if (intValue >= 30) {
				similarityComment = "Find almost any match. Super high risk of false positive "
						+ "results.";
			} else if (intValue >= 20) {
				similarityComment = "You want false positive results? Because that's how you get "
						+ "false positive results.";
			} else {
				similarityComment = "No. Just no.";
			}
			similarityDescriptionLabel.setText(similarityComment);
			similarityLabel.setText(similarityLabelOrigText + newValue.intValue() + "%");
			ValidationResult finalResult = ValidationResult.fromResults(zeroResult, lowResult);
			return finalResult;
		};
		this.validationSupport.registerValidator(similaritySlider, similaritySliderValidator);
	}
	
}
