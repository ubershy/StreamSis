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

import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.CuteElementContainer;
import com.ubershy.streamsis.elements.ElementInfo;
import com.ubershy.streamsis.elements.ElementInfo.ElementHealth;
import com.ubershy.streamsis.elements.ElementInfo.Result;
import com.ubershy.streamsis.elements.actions.Action;
import com.ubershy.streamsis.elements.checkers.Checker;
import com.ubershy.streamsis.elements.counters.Counter;
import com.ubershy.streamsis.elements.parts.TargetImageWithActions;
import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.gui.contextmenu.TreeContextMenuBuilder;
import com.ubershy.streamsis.gui.helperclasses.CuteColor;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * A Tree Cell that contains {@link CuteElement}. <br>
 * Is responsive to CuteElement's properties and changes it's view accordingly.
 */
public class CuteTreeCell extends TreeCell<CuteElement> {

	/** The text field for editing the {@link CuteElement}'s name. */
	private TextField textField;

	/** The property linked to the {@link CuteElement}'s health. */
	private ObjectProperty<ElementHealth> elementHealthProperty = new SimpleObjectProperty<>(
			ElementHealth.HEALTHY);
	
	/**
	 * The variable with unknown (empty) {@link Result} value. Only exist to not instantiate Result
	 * each time.
	 */
	private ElementInfo.Result<Void> unknownResult = new ElementInfo.Result<Void>(null);
	
	/** The Color of {@link #paneGraphic}, when the {@link Result} is unknown. */
	private Color unknownResultColor = defaultColor;

	/** The property linked to the {@link CuteElement}'s last result of working. */
	private ObjectProperty<ElementInfo.Result<?>> lastResultProperty = new SimpleObjectProperty<>(
			unknownResult);

	/** The FAIL icon for {@link Checker} used in {@link #paneGraphic}. */
	private FontAwesomeIconView resultFailIconForChecker = new FontAwesomeIconView(
			FontAwesomeIcon.MINUS_SQUARE);

	/** The SUCCESS icon for {@link Checker} used in {@link #paneGraphic}. */
	private FontAwesomeIconView resultSuccessIconForChecker = new FontAwesomeIconView(
			FontAwesomeIcon.CHECK_SQUARE);

	/** The FAIL icon for {@link Action} used in {@link #paneGraphic}. */
	private FontAwesomeIconView resultFailIconForAction = new FontAwesomeIconView(
			FontAwesomeIcon.MINUS_CIRCLE);

	/** The SUCCESS icon for {@link Action} used in {@link #paneGraphic}. */
	private FontAwesomeIconView resultSuccessIconForAction = new FontAwesomeIconView(
			FontAwesomeIcon.CHECK_CIRCLE);

	/** The PROCESSING icon for {@link Action} used in {@link #paneGraphic}. */
	private FontAwesomeIconView resultUnknownIconForAction = new FontAwesomeIconView(
			FontAwesomeIcon.CIRCLE);

	/** The UNKNOWN icon for {@link Checker} used in {@link #paneGraphic}. */
	private FontAwesomeIconView resultUnknownIconForChecker = new FontAwesomeIconView(
			FontAwesomeIcon.SQUARE);

	/** The UNKNOWN icon for {@link Counter} used in {@link #paneGraphic}. */
	private FontAwesomeIconView resultUnknownIconForCounter = new FontAwesomeIconView(
			FontAwesomeIcon.TH_LARGE);

	/** The UNKNOWN icon for {@link CuteElementContainer} used in {@link #paneGraphic}. */
	private FontAwesomeIconView resultUnknownIconForContainer = new FontAwesomeIconView(
			FontAwesomeIcon.CUBES);
	
	/** The UNKNOWN icon for {@link TargetImageWithActions} used in {@link #paneGraphic}. */
	private FontAwesomeIconView resultUnknownIconForTIWA = new FontAwesomeIconView(
			FontAwesomeIcon.IMAGE);

	/** The text with number used in {@link #paneGraphic} for {@link Counter}. */
	private Text numberResultText = new Text("0");

	/** The color to use for the graphic when the {@link CuteElement}'s result is not available. */
	private static final Color defaultColor = CuteColor.GENTLEPINK;

	/** The color to use for the graphic when the {@link CuteElement} got success result. */
	private static final Color successColor = CuteColor.GENTLEBLUE;

	/** The color to use for the graphic when the {@link CuteElement} got fail result. */
	private static final Color failColor = CuteColor.GENTLERED;

	/** The color to use for the text when the {@link CuteElement} is broken. */
	private final static Color brokenColor = CuteColor.GENTLEDARKPURPLE;

	/** The color to use for the text when the {@link CuteElement} is sick. */
	private final static Color sickColor = CuteColor.GENTLEDARKYELLOW;
	
	/** The color to use for the text when the {@link CuteElement} is healthy. */
	private final static Color healthyTextColor = Color.BLACK;

	/** Simple animation for Action result icon on success. */
	FillTransition actionSuccessAnimation = new FillTransition(Duration.millis(300),
			resultSuccessIconForAction, unknownResultColor, successColor);
	
	/** Simple animation for Action result icon on fail. */
	FillTransition actionFailAnimation = new FillTransition(Duration.millis(300),
			resultFailIconForAction, unknownResultColor, failColor);

	/**
	 * The Label with icon representing current {@link Result} of {@link CuteElement} inside this
	 * cell.
	 */
	private final StackPane paneGraphic = new StackPane();

	/**
	 * Instantiates a new CuteTree Cell.
	 */
	public CuteTreeCell() {
		setGraphicTextGap(0.0);
		// Set up a pane that will contain the icon.
		paneGraphic.setMinWidth(23);
		// Set up icons for Checkers.
		resultFailIconForChecker.setFill(defaultColor);
		resultSuccessIconForChecker.setFill(successColor);
		resultUnknownIconForChecker.setFill(defaultColor);
		applyCommonStyleToIcon(resultFailIconForChecker);
		applyCommonStyleToIcon(resultSuccessIconForChecker);
		applyCommonStyleToIcon(resultUnknownIconForChecker);
		// Set up icons for Actions.
		resultFailIconForAction.setFill(failColor);
		resultSuccessIconForAction.setFill(successColor);
		resultUnknownIconForAction.setFill(defaultColor);
		applyCommonStyleToIcon(resultFailIconForAction);
		applyCommonStyleToIcon(resultSuccessIconForAction);
		applyCommonStyleToIcon(resultUnknownIconForAction);
		// Set up icons for Counters.
		resultUnknownIconForCounter.setFill(defaultColor);
		applyCommonStyleToIcon(resultUnknownIconForCounter);
		numberResultText.setFill(defaultColor);
		numberResultText.setFont(Font.font(null, FontWeight.BOLD, 15));
		// Set up icon for Containers.
		resultUnknownIconForContainer.setFill(defaultColor);
		applyCommonStyleToIcon(resultUnknownIconForContainer);
		// Set up icon for TargetImageWithActions.
		resultUnknownIconForTIWA.setFill(defaultColor);
		applyCommonStyleToIcon(resultUnknownIconForTIWA);
		
		actionSuccessAnimation.setInterpolator(Interpolator.DISCRETE);
		actionFailAnimation.setInterpolator(Interpolator.DISCRETE);
		
		ChangeListener<ElementHealth> elementHealthListener = (observableValue, oldElementHealth,
				newElementHealth) -> Platform.runLater(() -> {
					refreshTextColorBasedOnHealth(newElementHealth);
				});
		ChangeListener<Result<?>> lastResultListener = (observableValue, oldResult,
				newResult) -> Platform.runLater(() -> {
					refreshResultIconBasedOnResult(newResult);
				});
		elementHealthProperty.addListener(elementHealthListener);
		lastResultProperty.addListener(lastResultListener);
		setOnMouseClicked(event -> GUIManager.elementEditor.setCurrentElement(getItem()));
	}

	private void applyCommonStyleToIcon(FontAwesomeIconView icon) {
		icon.setGlyphSize(15);
//		icon.setCache(true);
//		icon.setCacheHint(CacheHint.SPEED);
	}

	@Override
	public void updateItem(CuteElement item, boolean empty) {
		super.updateItem(item, empty);
		if (empty || item == null) {
			lastResultProperty.unbind();
			elementHealthProperty.unbind();
			textProperty().unbind();
			setText(null);
			CellUtils.setGraphicIfNotAlready(this, null);
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
				CellUtils.setGraphicIfNotAlready(this, textField);
			} else {
				refreshResultIconBasedOnResult(item.getElementInfo().lastResultProperty().get());
				refreshTextColorBasedOnHealth(item.getElementInfo().elementHealthProperty().get());
				lastResultProperty.bind(item.getElementInfo().lastResultProperty());
				elementHealthProperty.bind(item.getElementInfo().elementHealthProperty());
				CellUtils.setGraphicIfNotAlready(this, paneGraphic);
				textProperty().bind(item.getElementInfo().nameProperty());
				Tooltip tooltip = new Tooltip(item.getElementInfo().getUnhealthyMessage());
				setTooltip(tooltip);
				// if (getTreeItem() != null)
				// setGraphicIfNeeded(getTreeItem().getGraphic());
				setContextMenu(TreeContextMenuBuilder.createCuteTreeCellContextMenu(this));
			}
		}
	}

	/**
	 * Set coloring behavior for the result icon based on {@link CuteElement}'s Health.
	 * 
	 * @param color
	 */
	private void refreshTextColorBasedOnHealth(ElementHealth currentHealth) {
		switch (currentHealth) {
		case BROKEN:
			CellUtils.setLabeledTextFillIfNotAlready(this, brokenColor);
			return;
		case SICK:
			CellUtils.setLabeledTextFillIfNotAlready(this, sickColor);
			return;
		case HEALTHY:
			CellUtils.setLabeledTextFillIfNotAlready(this, healthyTextColor);
			return;
		default:
			throw new RuntimeException("Unknown Health value");
		}
	}

	/**
	 * Process result to change {@link #paneGraphic} view.
	 *
	 * @param newResult the new result
	 */
	private void refreshResultIconBasedOnResult(Result<?> newResult) {
		CuteElement currentElement = getItem();
		if (currentElement instanceof Checker) {
			processResultForChecker(newResult);
		} else if (currentElement instanceof Counter) {
			processResultForCounter(newResult);
		} else if (currentElement instanceof Action) {
			processResultForAction(newResult);
		} else if (currentElement instanceof CuteElementContainer) {
			processResultForContainer(newResult);
		} else if (currentElement instanceof TargetImageWithActions) {
			processResultForTIWA(newResult);
		} else if (currentElement == null) {
			return;
		} else {
			throw new RuntimeException("Unknown CuteElement type");
		}
	}

	/**
	 * Process result for Checker to change {@link #paneGraphic} view.
	 *
	 * @param newResult the new result
	 */
	private void processResultForChecker(Result<?> newResult) {
		Object object = newResult.get();
		if (object == null) {
			CellUtils.setSingleChildIfNotAlready(paneGraphic, resultUnknownIconForChecker);
			return;
		}
		if (object instanceof Boolean) {
			Boolean bool = (Boolean) object;
			if (bool) {
				CellUtils.setSingleChildIfNotAlready(paneGraphic, resultSuccessIconForChecker);
			} else {
				CellUtils.setSingleChildIfNotAlready(paneGraphic, resultFailIconForChecker);
			}
		}
	}

	/**
	 * Process result for Counter to change {@link #paneGraphic} view.
	 *
	 * @param newResult the new result
	 */
	private void processResultForCounter(Result<?> newResult) {
		Object object = newResult.get();
		if (object == null) {
			CellUtils.setSingleChildIfNotAlready(paneGraphic, resultUnknownIconForCounter);
			return;
		}
		if (object instanceof Integer) {
			Integer number = (Integer) object;
			numberResultText.setText(number.toString());
			CellUtils.setSingleChildIfNotAlready(paneGraphic, numberResultText);
		}
	}

	/**
	 * Process result for Action to change {@link #paneGraphic} view.
	 *
	 * @param newResult the new result
	 */
	private void processResultForAction(Result<?> newResult) {
		Object object = newResult.get();
		if (object == null) {
			CellUtils.setSingleChildIfNotAlready(paneGraphic, resultUnknownIconForAction);
			return;
		}
		if (object instanceof Boolean) {
			Boolean bool = (Boolean) object;
			// Actions are usually rarely and quickly executed, so let's play animation to make
			// it easy for the user to notice the change in execution state.
			if (bool) {
				CellUtils.setSingleChildIfNotAlready(paneGraphic, resultSuccessIconForAction);
				actionSuccessAnimation.playFromStart();
			} else {
				CellUtils.setSingleChildIfNotAlready(paneGraphic, resultFailIconForAction);
				actionFailAnimation.playFromStart();
			}
		}
	}
	
	/**
	 * Process result for {@link CuteElementContainer} to change {@link #paneGraphic} view.
	 *
	 * @param newResult the new result
	 */
	private void processResultForContainer(Result<?> newResult) {
		Object object = newResult.get();
		if (object == null) {
			CellUtils.setSingleChildIfNotAlready(paneGraphic, resultUnknownIconForContainer);
		}
	}
	
	/**
	 * Process result for {@link TargetImageWithActions} to change {@link #paneGraphic} view.
	 *
	 * @param newResult the new result
	 */
	private void processResultForTIWA(Result<?> newResult) {
		Object object = newResult.get();
		if (object == null) {
			CellUtils.setSingleChildIfNotAlready(paneGraphic, resultUnknownIconForTIWA);
		}
	}

//	@Override
//	public void startEdit() {
//		super.startEdit();
//		if (textField == null) {
//			createTextField();
//		}
//		lastResultProperty.unbind();
//		elementHealthProperty.unbind();
//		textProperty().unbind();
//		setText(null);
//		CellUtils.setGraphicIfNotAlready(this, textField);
//		textField.selectAll();
//	}
//
//	@Override
//	public void cancelEdit() {
//		super.cancelEdit();
//		lastResultProperty.bind(getItem().getElementInfo().lastResultProperty());
//		elementHealthProperty.bind(getItem().getElementInfo().elementHealthProperty());
//		textProperty().bind(getItem().getElementInfo().nameProperty());
//		CellUtils.setGraphicIfNotAlready(this, paneGraphic);
//	}
//
//	private void createTextField() {
//		textField = new TextField(getString());
//		textField.setOnKeyReleased(new EventHandler<KeyEvent>() {
//			@Override
//			public void handle(KeyEvent t) {
//				if (t.getCode() == KeyCode.ENTER) {
//					commitEdit(getItem());
//				} else if (t.getCode() == KeyCode.ESCAPE) {
//					cancelEdit();
//				}
//			}
//		});
//	}

	/**
	 * Gets the string to show as cell's text.
	 *
	 * @return the string to show as cell's text
	 */
	private String getString() {
		CuteElement item = getItem();
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

}
