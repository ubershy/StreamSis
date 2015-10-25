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
package com.ubershy.streamsis.gui.controllers.editspecific;

import java.net.URL;
import java.util.ResourceBundle;

import org.controlsfx.validation.ValidationSupport;

import com.ubershy.streamsis.gui.animations.ThreeDotsAnimation;
import com.ubershy.streamsis.project.CuteElement;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class NoneSelectedController extends AbstractCuteController implements Initializable {

	@FXML
	private Label propertiesPaneDots;

	@FXML
	private VBox root;

	/*
	 * @inheritDoc
	 */
	@Override
	public void apply() {
		// Nothing to apply
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void bindToCuteElement(CuteElement element) {
		// Nothing to set
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public Node getView() {
		return root;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		ThreeDotsAnimation pPaneDotsAnimation = new ThreeDotsAnimation("", propertiesPaneDots, 1);
		pPaneDotsAnimation.play();
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void reset() {
		// Nothing to reset
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void setValidationSupport(ValidationSupport validationSupport) {
		// Nothing to validate
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void unbindFromCuteElement() {
		// nothing to unbind
	}
}
