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

import com.ubershy.streamsis.actions.Action;
import com.ubershy.streamsis.checkers.Checker;
import com.ubershy.streamsis.counters.Counter;
import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.gui.contextmenu.TreeContextMenuBuilder;
import com.ubershy.streamsis.project.CuteNode;
import com.ubershy.streamsis.project.ElementInfo;
import com.ubershy.streamsis.project.ElementInfo.ElementHealth;
import com.ubershy.streamsis.project.ElementInfo.Result;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * A Tree Cell that contains {@link CuteNode}. <br>
 * Is responsive to CuteNode's properties and changes it's view accordingly.
 */
public class CuteTreeCell extends TreeCell<CuteNode> {

	/** The text field for editing the {@link CuteNode}'s name. */
	private TextField textField;

	/** The property linked to the {@link CuteNode}'s health. */
	private ObjectProperty<ElementHealth> elementHealthProperty = new SimpleObjectProperty<>(
			ElementHealth.HEALTHY);
	
	/**
	 * The variable with unknown (empty) {@link Result} value. Only exist to not instantiate Result
	 * each time.
	 */
	private ElementInfo.Result<Void> unknownResult = new ElementInfo.Result<Void>(null);
	
	/** The Color of {@link #resultLabel}, when the {@link Result} is unknown. */
	private Color unknownResultColor = Color.HOTPINK;

	/** The property linked to the {@link CuteNode}'s last result of working. */
	private ObjectProperty<ElementInfo.Result<?>> lastResultProperty = new SimpleObjectProperty<>(
			unknownResult);

	/** The FAIL icon for {@link Checker} used in {@link #resultLabel}. */
	private Text resultFailTextForChecker = GlyphsDude.createIcon(FontAwesomeIcon.MINUS_SQUARE);

	/** The SUCCESS icon for {@link Checker} used in {@link #resultLabel}. */
	private Text resultSuccessTextForChecker = GlyphsDude.createIcon(FontAwesomeIcon.CHECK_SQUARE);

	/** The FAIL icon for {@link Action} used in {@link #resultLabel}. */
	private Text resultFailTextForAction = GlyphsDude.createIcon(FontAwesomeIcon.MINUS_CIRCLE);

	/** The SUCCESS icon for {@link Action} used in {@link #resultLabel}. */
	private Text resultSuccessTextForAction = GlyphsDude.createIcon(FontAwesomeIcon.CHECK_CIRCLE);

	/** The PROCESSING icon for {@link Action} used in {@link #resultLabel}. */
	private Text resultUnknownTextForAction = GlyphsDude.createIcon(FontAwesomeIcon.CIRCLE);

	/** The UNKNOWN icon for {@link Checker} used in {@link #resultLabel}. */
	private Text resultUnknownTextForChecker = GlyphsDude.createIcon(FontAwesomeIcon.SQUARE);

	/** The UNKNOWN icon for {@link Counter} used in {@link #resultLabel}. */
	private Text resultUnknownTextForCounter = GlyphsDude.createIcon(FontAwesomeIcon.TH_LARGE);

	/** The text with number used in {@link #resultLabel} for {@link Counter}. */
	private Text numberResultText = new Text("0");

	/** Simple animation for Action result icon for changing color from pink to green on success. */
	FillTransition actionSuccessAnimation = new FillTransition(Duration.millis(300),
			resultSuccessTextForAction, unknownResultColor, Color.DEEPSKYBLUE);
	
	/** Simple animation for Action result icon for changing color from pink to red on fail. */
	FillTransition actionFailAnimation = new FillTransition(Duration.millis(300),
			resultFailTextForAction, unknownResultColor, Color.RED);

	/**
	 * The Label with icon representing current {@link Result} of {@link CuteNode} inside this cell.
	 */
	private final Label resultLabel = new Label("");

	/**
	 * Instantiates a new CuteTree Cell.
	 */
	public CuteTreeCell() {
		setGraphicTextGap(1);
		resultLabel.setScaleX(1.25);
		resultLabel.setScaleY(1.25);
		resultFailTextForChecker.setFill(Color.HOTPINK);
		resultSuccessTextForChecker.setFill(Color.DEEPSKYBLUE);
		resultUnknownTextForChecker.setFill(Color.HOTPINK);
		resultFailTextForAction.setFill(Color.RED);
		resultSuccessTextForAction.setFill(Color.DEEPSKYBLUE);
		resultUnknownTextForAction.setFill(Color.HOTPINK);
		resultUnknownTextForCounter.setFill(Color.HOTPINK);
		actionSuccessAnimation.setInterpolator(Interpolator.DISCRETE);
		actionFailAnimation.setInterpolator(Interpolator.DISCRETE);
		numberResultText.setFill(Color.DEEPPINK);
		numberResultText.setFont(Font.font(null, FontWeight.BOLD, 10));
		resultLabel.setMinWidth(14);
		ChangeListener<ElementHealth> elementHealthListener = (observableValue, oldElementHealth,
				newElementHealth) -> Platform.runLater(() -> {
					refreshResultIconBasedOnHealth(newElementHealth);
				});
		ChangeListener<Result<?>> lastResultListener = (observableValue, oldResult,
				newResult) -> Platform.runLater(() -> {
					refreshResultIconBasedOnResult(newResult);
				});
		elementHealthProperty.addListener(elementHealthListener);
		lastResultProperty.addListener(lastResultListener);
	}

	@Override
	public void updateItem(CuteNode item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			lastResultProperty.unbind();
			elementHealthProperty.unbind();
			textProperty().unbind();
			setText(null);
			setGraphic(null);
			setContextMenu(null);
			setTooltip(null);
		} else {
			if (isEditing()) {
				lastResultProperty.unbind();
				elementHealthProperty.unbind();
				if (textField != null) {
					textField.setText(getString());
				}
				textProperty().unbind();
				setText(null);
				setGraphic(textField);
			} else {
				refreshResultIconBasedOnResult(item.getElementInfo().lastResultProperty().get());
				refreshResultIconBasedOnHealth(item.getElementInfo().elementHealthProperty().get());
				lastResultProperty.bind(item.getElementInfo().lastResultProperty());
				elementHealthProperty.bind(item.getElementInfo().elementHealthProperty());
				setGraphic(resultLabel);
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

	/**
	 * Set coloring behavior for the result icon based on {@link CuteNode}'s Health.
	 * 
	 * @param color
	 */
	private void refreshResultIconBasedOnHealth(ElementHealth currentHealth) {
		switch (currentHealth) {
		case BROKEN:
			setTextColorForUnknownResult(Color.INDIGO);
			refreshResultIconBasedOnResult(unknownResult);
			return;
		case SICK:
			setTextColorForUnknownResult(Color.GOLDENROD);
			refreshResultIconBasedOnResult(unknownResult);
			return;
		case HEALTHY:
			setTextColorForUnknownResult(Color.HOTPINK);
			return;
		default:
			throw new RuntimeException("Unknown Health value");
		}
	}

	/**
	 * Set Color for the result icon when the result of {@link CuteNode} is unknown.
	 * 
	 * @param color
	 */
	private void setTextColorForUnknownResult(Color color) {
		unknownResultColor = color;
		actionSuccessAnimation.setFromValue(unknownResultColor);
		actionFailAnimation.setFromValue(unknownResultColor);
		if (!resultUnknownTextForCounter.getFill().equals(color))
			resultUnknownTextForCounter.setFill(color);
		if (!resultUnknownTextForChecker.getFill().equals(color))
			resultUnknownTextForChecker.setFill(color);
		if (!resultUnknownTextForAction.getFill().equals(color))
			resultUnknownTextForAction.setFill(color);
	}
	
	/**
	 * Process result to change {@link #resultLabel} view.
	 *
	 * @param newResult the new result
	 */
	private void refreshResultIconBasedOnResult(Result<?> newResult) {
		CuteNode currentElement = getItem();
		if (currentElement instanceof Checker) {
			processResultForChecker(newResult);
		} else if (currentElement instanceof Counter) {
			processResultForCounter(newResult);
		} else if (currentElement instanceof Action) {
			processResultForAction(newResult);
		} else if (currentElement == null) {
			return;
		} else {
			throw new RuntimeException("Unknown CuteNode type");
		}
	}

	/**
	 * Process result for Checker to change {@link #resultLabel} view.
	 *
	 * @param newResult the new result
	 */
	private void processResultForChecker(Result<?> newResult) {
		Object object = newResult.get();
		if (object == null) {
			resultLabel.setGraphic(resultUnknownTextForChecker);
			return;
		}
		if (object instanceof Boolean) {
			Boolean bool = (Boolean) object;
			if (bool) {
				resultLabel.setGraphic(resultSuccessTextForChecker);
			} else {
				resultLabel.setGraphic(resultFailTextForChecker);
			}
		}
	}

	/**
	 * Process result for Counter to change {@link #resultLabel} view.
	 *
	 * @param newResult the new result
	 */
	private void processResultForCounter(Result<?> newResult) {
		Object object = newResult.get();
		if (object == null) {
			resultLabel.setGraphic(resultUnknownTextForCounter);
			return;
		}
		if (object instanceof Integer) {
			Integer number = (Integer) object;
			numberResultText.setText(number.toString());
			resultLabel.setGraphic(numberResultText);
		}
	}

	/**
	 * Process result for Action to change {@link #resultLabel} view.
	 *
	 * @param newResult the new result
	 */
	private void processResultForAction(Result<?> newResult) {
		Object object = newResult.get();
		if (object == null) {
			resultLabel.setGraphic(resultUnknownTextForAction);
			return;
		}
		if (object instanceof Boolean) {
			Boolean bool = (Boolean) object;
			// Actions are usually rarely and quickly executed, so let's play animation to make
			// it easy for the user to notice the change in execution state.
			if (bool) {
				resultLabel.setGraphic(resultSuccessTextForAction);
				actionSuccessAnimation.playFromStart();
			} else {
				resultLabel.setGraphic(resultFailTextForAction);
				actionFailAnimation.playFromStart();
			}
		}
	}

	@Override
	public void startEdit() {
		super.startEdit();
		if (textField == null) {
			createTextField();
		}
		lastResultProperty.unbind();
		elementHealthProperty.unbind();
		textProperty().unbind();
		setText(null);
		setGraphic(textField);
		textField.selectAll();
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		lastResultProperty.bind(getItem().getElementInfo().lastResultProperty());
		elementHealthProperty.bind(getItem().getElementInfo().elementHealthProperty());
		textProperty().bind(getNameBinding(getItem()));
		// setGraphic(getTreeItem().getGraphic());
		setGraphic(resultLabel);
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
