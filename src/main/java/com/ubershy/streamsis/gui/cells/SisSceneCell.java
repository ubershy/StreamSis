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
package com.ubershy.streamsis.gui.cells;

import com.ubershy.streamsis.elements.SisScene;
import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.gui.contextmenu.PossibleMoves;
import com.ubershy.streamsis.gui.contextmenu.SisSceneContextMenuBuilder;
import com.ubershy.streamsis.gui.helperclasses.CuteColor;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import com.ubershy.streamsis.project.ProjectManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Effect;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

/**
 * SisScene Cell. <br>
 * A Cell with {@link SisScene} inside.
 */
public class SisSceneCell extends ListCell<SisScene> {

	/** The text field for editing the SisScene's name. (currently not in use) */
	private TextField textField;

	private StringProperty primarySisSceneName = new SimpleStringProperty();

	/** The gap between the graphic and text. */
	private final static double graphicTextGap = 3.0;

	/** Defines how many pixels graphic should be shifted on X axis. */
	private final static double graphicTranslateX = -2.0;

	/** The size of a font used to render the graphic. */
	private final static int graphicFontSize = 16;

	/** The videoCameraGraphic icon for {@link SisScene}. */
	private FontAwesomeIconView videoCameraGraphic = new FontAwesomeIconView(
			FontAwesomeIcon.VIDEO_CAMERA);
	
//	/** The shadow to apply to graphic when {@link SisScene} is selected. */
//	private DropShadow shadowForSelected = new DropShadow();

	public SisSceneCell() {
		setGraphicTextGap(graphicTextGap);
		videoCameraGraphic.setGlyphSize(graphicFontSize);
		videoCameraGraphic.setTranslateX(graphicTranslateX);
//        shadowForSelected.setColor(Color.BLACK);
//        shadowForSelected.setSpread(0.5);
//        shadowForSelected.setRadius(1);
		setGraphicIfNeeded(videoCameraGraphic);
		primarySisSceneName.bind(ProjectManager.getProject().primarySisSceneNameProperty());
		ChangeListener<String> defaultSisSceneListener = (observable, oldValue,
				newValue) -> refreshSisSceneLook(newValue, this.selectedProperty().get());
		primarySisSceneName.addListener(defaultSisSceneListener);
//		ChangeListener<? super Boolean> selectedListener = (observable, oldValue,
//				newValue) -> refreshSisSceneLook(primarySisSceneName.get(), newValue);
//		this.selectedProperty().addListener(selectedListener);
		setOnMouseClicked(event -> GUIManager.elementEditor.setCurrentElement(getItem()));
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
				setGraphicIfNeeded(textField);
			} else {
				setGraphicIfNeeded(videoCameraGraphic);
				textProperty().bind(item.getElementInfo().nameProperty());
				refreshSisSceneLook(ProjectManager.getProject().getPrimarySisSceneName(), 
						selectedProperty().get());
				PossibleMoves possibleMoves = PossibleMoves.UPORDOWN;
				if (getListView().getItems().size() != 1) {
					if (getIndex() == 0)
						possibleMoves = PossibleMoves.ONLYDOWN;
					if (getIndex() == ProjectManager.getProject().getSisScenesUnmodifiable().size() - 1)
						possibleMoves = PossibleMoves.ONLYUP;
				} else {
					possibleMoves = PossibleMoves.NOWHERE;
				}
				setContextMenu(
						SisSceneContextMenuBuilder.createSisSceneItemContextMenu(possibleMoves));
				// if (getIndex() == GUIManager.getSisSceneToRenameIndex()) {
				// startEdit();
				// }
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
		setGraphicIfNeeded(textField);
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
	 * Creates the text field for editing SisScene's name. (Not working).
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

	private void refreshSisSceneLook(String primarySisSceneName, boolean isSelected) {
		if (primarySisSceneName != null) {
			if (getItem() != null) {
				Effect effect = null;
				String style;
				Color finalColor;
				boolean isPrimary = primarySisSceneName
						.equals(getItem().getElementInfo().getName());
//				if (isSelected) {
//					effect = shadowForSelected;
//				} else {
//					effect = null;
//				}
				if (isPrimary) {
					style = "-fx-font-weight: bold;";
					finalColor = Color.HOTPINK;
				} else {
					style = "-fx-font-weight: normal;";
					finalColor = CuteColor.GENTLEPINK;
				}
				applySisSceneLook(effect, style, finalColor);
			}
		}
	}

	private void applySisSceneLook(Effect effect, String style, Color finalColor) {
		if (!getStyle().equals(style))
			setStyle(style);
		if (videoCameraGraphic.getEffect() == null
				|| !videoCameraGraphic.getEffect().equals(effect))
			videoCameraGraphic.setEffect(effect);
		if (!finalColor.equals(videoCameraGraphic.getFill()))
			videoCameraGraphic.setFill(finalColor);
	}

	private void setGraphicIfNeeded(Node newGraphic) {
		if (getGraphic() == null || !getGraphic().equals(newGraphic))
			setGraphic(newGraphic);
	}

}
