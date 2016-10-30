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
import org.controlsfx.validation.ValidationSupport;
import com.ubershy.streamsis.gui.controllers.CuteElementController;
import com.ubershy.streamsis.gui.controllers.edit.AbstractCuteController;
import com.ubershy.streamsis.project.CuteElement;
import com.ubershy.streamsis.project.CuteElementContainer;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * CuteElementContainerController, the controller that allows to view {@link CuteElementContainer}'s
 * information in Element Editor panel. That's right. No editing.
 */
public class CuteElementContainerController extends AbstractCuteController
		implements CuteElementController {

	/** The root node. */
	@FXML
	private GridPane root;

    @FXML
    private Label typeLabel;

    @FXML
    private Label maxQuantityLabel;

    @FXML
    private Label currentQuantityLabel;

	/** The {@link CuteElementContainer}.. */
	private CuteElementContainer<?> container;
	
	private ListChangeListener<CuteElement> listener;

	/*
	 * @inheritDoc
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		listener = new ListChangeListener<CuteElement>() {
			public void onChanged(ListChangeListener.Change<? extends CuteElement> c) {
				currentQuantityLabel.setText(String.valueOf(c.getList().size()));
			}
		};
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
	public void bindToCuteElement(CuteElement editableCopyOfCE, CuteElement origCE) {
		container = (CuteElementContainer<?>) editableCopyOfCE;
		CuteElementContainer<?> origContainer = (CuteElementContainer<?>) origCE;
		// Set.
		typeLabel.setText(container.getAddableChildrenTypeInfo().toString());
		maxQuantityLabel.setText(container.getMaxAddableChildrenCount().toString());
		currentQuantityLabel.setText(String.valueOf(origContainer.getChildren().size()));
		// Bind.
		origContainer.getChildren().addListener(listener);
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void unbindFromCuteElement() {
		unbindAllRememberedBinds();
		container.getChildren().removeListener(listener);
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void setValidationSupport(ValidationSupport validationSupport) {
		// Do nothing since no input.
	}
	
	/*
	 * @inheritDoc
	 */
	@Override
	public void setInputAllowed(boolean allowInput) {
		// Let's not disable components in this view like in the default behavior as there's no
		// input fields, but only the viewable important information. Let's not allow this
		// information to be grayed out.
		// That basically means... do nothing. =)
	}

}
