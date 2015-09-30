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
package com.ubershy.streamsis.gui.helperclasses;

import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.gui.contextmenu.SisSceneContextMenuBuilder;
import com.ubershy.streamsis.project.SisScene;
import com.ubershy.streamsis.project.ProjectManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * SisScene Cell. <br>
 * A Cell with {@link SisScene} inside.
 */
public class SisSceneCell extends ListCell<SisScene> {

	/** The text field for editing the SisScene's name. */
	private TextField textField;

	private StringProperty defaultSisSceneName = new SimpleStringProperty();

	public SisSceneCell() {
		defaultSisSceneName.bind(ProjectManager.getProject().defaultSisSceneNameProperty());

		ChangeListener<String> defaultSisSceneListener = new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue,
					String newValue) {
				checkIfDefaultAndApplyStyle(newValue);
			}
		};
		defaultSisSceneName.addListener(defaultSisSceneListener);
	}

	@Override
	public void updateItem(SisScene item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			textProperty().unbind();
			setText(null);
			setGraphic(null);
			setContextMenu(null);
			setStyle("-fx-font-weight: normal;");
		} else {
			if (isEditing()) {
				if (textField != null) {
					textField.setText(getName());
				}
				textProperty().unbind();
				setText(null);
				setGraphic(textField);
			} else {
				textProperty().unbind();
				textProperty().bind(item.getElementInfo().nameProperty());
				checkIfDefaultAndApplyStyle(ProjectManager.getProject().getDefaultSisSceneName());
				int contextMenuOption = 0;
				if (getListView().getItems().size() != 1) {
					if (getIndex() == 0)
						contextMenuOption = 1;
					if (getIndex() == ProjectManager.getProject().getSisScenes().size() - 1)
						contextMenuOption = 2;
				} else {
					contextMenuOption = 3;
				}
				setContextMenu(SisSceneContextMenuBuilder
						.createSisSceneItemContextMenu(contextMenuOption));
				// if (getIndex() == GUIManager.getSisSceneToRenameIndex()) {
				// startEdit();
				// }
				setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						GUIManager.elementEditor.lastFocusedProperty.set(item);
					}
				});
			}
		}
	}

	@Override
	public void startEdit() {
		if (!isEditable() || !getListView().isEditable()
				|| ProjectManager.getProject().isStarted()) {
			return;
		}
		super.startEdit();

		if (textField == null) {
			createTextField();
		}
		textProperty().unbind();
		setText(null);
		textField.setText(getName());
		setGraphic(textField);
		textField.requestFocus();
		textField.selectAll();
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		// GUIManager.setSisSceneToRenameIndex(-1);
		textProperty().bind(getItem().getElementInfo().nameProperty());
		setGraphic(null);
	}

	/**
	 * Creates the text field for editing SisScene's name.
	 */
	private void createTextField() {
		textField = new TextField(getName());
		textField.setOnKeyReleased(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent t) {
				if (t.getCode() == KeyCode.ENTER) {
					Tooltip tp = new Tooltip();
					tp.setAutoHide(true);
					SisScene sisScene = getItem();
					Point2D p = textField.localToScreen(textField.getLayoutBounds().getMaxX(),
							textField.getLayoutBounds().getMaxY());
					boolean actorWithNameDontExists = ProjectManager.getProject()
							.getSisSceneByName(textField.getText()) == null;
					boolean hasOldName = textField.getText()
							.equals(sisScene.getElementInfo().getName());
					if (actorWithNameDontExists || hasOldName) {
						if (!textField.getText().isEmpty()) {
							ProjectManager.getProject().setCuteElementNameSafely(sisScene,
									textField.getText());
							tp.hide();
							// GUIManager.setSisSceneToRenameIndex(-1);
							commitEdit(sisScene);
						}
					} else {
						tp.setText(
								"SisScene with this name already exists.\nDear user, please choose another name. =)");
						tp.show(textField, p.getX(), p.getY());
					}
				} else if (t.getCode() == KeyCode.ESCAPE) {
					cancelEdit();
				}
			}
		});
	}

	/**
	 * Gets the string to show as cell's text.
	 *
	 * @return the string to show as cell's text
	 */
	private String getName() {
		return getItem() == null ? "" : getItem().getElementInfo().getName();
	}

	private void checkIfDefaultAndApplyStyle(String nameOfDefaultSisScene) {
		if (nameOfDefaultSisScene != null) {
			if (getItem() != null) {
				if (nameOfDefaultSisScene.equals(getItem().getElementInfo().getName())) {
					setStyle("-fx-font-weight: bold;");
				} else {
					setStyle("-fx-font-weight: normal;");
				}
			}
		}
	}
}
