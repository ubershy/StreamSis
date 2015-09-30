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
import com.ubershy.streamsis.gui.contextmenu.TreeContextMenuBuilder;
import com.ubershy.streamsis.project.CuteNode;
import com.ubershy.streamsis.project.ElementInfo.ElementState;
import com.ubershy.streamsis.project.ElementInfo.Result;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/**
 * A Tree Cell that contains {@link CuteNode}. <br>
 * Is responsive to CuteNode's properties and changes it's view accordingly.
 */
public class CuteTreeCell extends TreeCell<CuteNode> {

	/** The text field for editing the {@link CuteNode}'s name. */
	private TextField textField;

	/** The property linked to the {@link CuteNode}'s elementState. */
	private ObjectProperty<ElementState> elementStateProperty = new SimpleObjectProperty<>(
			ElementState.NEEDINIT);

	/** The property linked to the {@link CuteNode}'s last result of working. */
	private ObjectProperty<Result> lastResultProperty = new SimpleObjectProperty<>(Result.UNKNOWN);

	/** The FAIL text used in {@link #result}. */
	private final static String resultFailText = "F";

	/** The UNKNOWN text used in {@link #result}. */
	private final static String resultUnknownText = "-";

	/** The READY elementState text used in {@link #elementState}. */
	private final static String stateReadyText = "R";

	/** The NEEDINIT elementState text used in {@link #elementState}. */
	private final static String stateNeedInitText = "N";

	/** The SUCCESS text used in {@link #result}. */
	private final static String resultSuccessText = "S";

	/** The WORKING elementState text used in {@link #elementState}. */
	private final static String stateWorkingText = "W";

	/** The Text representing current {@link ElementState} of {@link CuteNode} inside this cell. */
	private final Text elementState = new Text(stateNeedInitText);

	/** The Text representing last {@link Result} of {@link CuteNode} inside this cell. */
	private final Text result = new Text(resultUnknownText);

	/** The Hbox with indicators: {@link #elementState} and {@link #result} inside this cell. */
	private final HBox graphicBox = new HBox(elementState, result);

	/**
	 * Instantiates a new CuteTree Cell.
	 */
	public CuteTreeCell() {
		ChangeListener<ElementState> elementStateListener = new ChangeListener<ElementState>() {
			@Override
			public void changed(ObservableValue<? extends ElementState> observableValue,
					ElementState oldElementState, ElementState newElementState) {
				switch (newElementState) {
				case NEEDINIT:
					elementState.setText(stateNeedInitText);
					break;
				case READY:
					elementState.setText(stateReadyText);
					break;
				case WORKING:
					elementState.setText(stateWorkingText);
					break;
				default:
					break;
				}
			}
		};

		ChangeListener<Result> lastResultListener = new ChangeListener<Result>() {
			@Override
			public void changed(ObservableValue<? extends Result> observableValue, Result oldResult,
					Result newResult) {
				switch (newResult) {
				case FAIL:
					result.setText(resultFailText);
					break;
				case SUCCESS:
					result.setText(resultSuccessText);
					break;
				case UNKNOWN:
					result.setText(resultUnknownText);
					break;
				default:
					break;
				}
			}
		};

		elementStateProperty.addListener(elementStateListener);
		lastResultProperty.addListener(lastResultListener);
	}

	@Override
	public void updateItem(CuteNode item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			elementStateProperty.unbind();
			lastResultProperty.unbind();
			textProperty().unbind();
			setText(null);
			setGraphic(null);
			setContextMenu(null);
			setTooltip(null);
		} else {
			if (isEditing()) {
				elementStateProperty.unbind();
				lastResultProperty.unbind();
				if (textField != null) {
					textField.setText(getString());
				}
				textProperty().unbind();
				setText(null);
				setGraphic(textField);
			} else {
				elementStateProperty.bind(item.getElementInfo().elementStateProperty());
				lastResultProperty.bind(item.getElementInfo().lastResultProperty());
				setGraphic(graphicBox);
				textProperty().bind(getNameBinding(item));
				Tooltip tooltip = new Tooltip(item.getElementInfo().getUnhealthyMessage());
				setTooltip(tooltip);
				// if (getTreeItem() != null)
				// setGraphic(getTreeItem().getGraphic());
				setContextMenu(TreeContextMenuBuilder.createCuteTreeCellContextMenu(this));
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
		super.startEdit();

		if (textField == null) {
			createTextField();
		}
		elementStateProperty.unbind();
		lastResultProperty.unbind();
		textProperty().unbind();
		setText(null);
		setGraphic(textField);
		textField.selectAll();
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		elementStateProperty.bind(getItem().getElementInfo().elementStateProperty());
		lastResultProperty.bind(getItem().getElementInfo().lastResultProperty());
		textProperty().bind(getNameBinding(getItem()));
		// setGraphic(getTreeItem().getGraphic());
		setGraphic(graphicBox);
	}

	private void createTextField() {
		textField = new TextField(getString());
		textField.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent t) {
				if (t.getCode() == KeyCode.ENTER) {
					commitEdit(getItem());
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
	private String getString() {
		CuteNode item = getItem();
		String result = "";
		if (item != null) {
			String className = item.getClass().getSimpleName();
			int numOfChildren = 0;
			if (item.getChildren() != null) {
				numOfChildren = item.getChildren().size();
			}
			result = String.format("%s (%s) (%d)", item.getElementInfo().getName(), className,
					numOfChildren);
		}
		return result;
	}

	private StringExpression getNameBinding(CuteNode node) {
		String className = node.getClass().getSimpleName();
		return Bindings.concat(node.getElementInfo().nameProperty()).concat(" (").concat(className)
				.concat(")");
	}
}
