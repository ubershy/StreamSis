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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.ElementInfo.ElementHealth;
import com.ubershy.streamsis.elements.ElementInfo.ElementState;
import com.ubershy.streamsis.elements.actors.AbstractActor;
import com.ubershy.streamsis.elements.actors.Actor;
import com.ubershy.streamsis.elements.checkers.Checker;
import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.gui.contextmenu.ActorContextMenuBuilder;
import com.ubershy.streamsis.gui.contextmenu.PossibleMoves;
import com.ubershy.streamsis.gui.controllers.AllActorsController;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * A ListCell that contains {@link Actor}. <br>
 * Is responsive to {@link Actor}'s properties and changes it's view accordingly.
 */
public class ActorCell extends ListCell<Actor> {
	
	/** The type of {@link ActorCell}. */
	public enum ActorCellType {
		/** The type of {@link #ActorCell()} that is contained in Structure view's list. */
		STRUCTUREVIEWCELL,
		/** The type of {@link #ActorCell()} that is contained in {@link AllActorsController}. */
		ALLACTORSVIEWCELL;
	}

	static final Logger logger = LoggerFactory.getLogger(ActorCell.class);
	
	/** This {@link ActorCell}'s type. */
	private ActorCellType actorCellType;

	/** The text field for editing the {@link Actor}'s name. */
	private TextField textField;

	/** The default character used in {@link #heart}. */
	private final static String heartDefaultCharacter = "💛";
	// private final static String heartDefaultCharacter = "♥";
	// private final static String heartDefaultCharacter = "❤";

	/** The character to use when the {@link Actor}'s {@link #heart} is 'broken'. Aww. <3 */
	private final static String heartBrokenCharacter = "💔";

	/**
	 * The character used in {@link #heart} when the {@link Actor} is running and it's check
	 * interval is quick.
	 */
	private final static String heartRunAndQuickCheckCharacter = "💗";

	/** The {@link Actor}'s heart and soul. With fancy animation. */
	private final Text heart = new Text(heartDefaultCharacter);

	/** The default {@link #heart} size. */
	private final static double heartDefaultSize = 1.5;

	/**
	 * The additional heart scale X multiplier {@link #heart} <br>
	 * Used to eliminate size difference between different characters set as heart's text.
	 */
	private double currentHeartCharacterScaleX = 1.0;

	/**
	 * The additional heart scale Y multiplier {@link #heart} <br>
	 * Used to eliminate size difference between different characters set as heart's text.
	 */
	private double currentHeartCharacterScaleY = 1.0;

	private String heartPreviousCharacter = heartDefaultCharacter;

	/** The original bounds of the {@link #heart} with default character. */
	private Bounds originalHeartBounds;

	/** The size of {@link #heart} when it's beating. */
	private final static double heartBeatSize = 2.0;

	/** Defines if {@link #heart} animates. */
	private boolean animationOn = true;

	/** The property linked to the {@link CuteElement}'s state. */
	private ObjectProperty<ElementState> elementElementStateProperty = new SimpleObjectProperty<>(
			ElementState.READY);

	/** The property linked to the {@link CuteElement}'s health. */
	private ObjectProperty<ElementHealth> elementHealthProperty = new SimpleObjectProperty<>(
			ElementHealth.HEALTHY);

	/**
	 * The property in milliseconds linked to the Actor's
	 * {@link AbstractActor#checkIntervalProperty() Check Interval}.
	 */
	private IntegerProperty checkIntervalProperty = new SimpleIntegerProperty(150);

	/**
	 * The boolean property that tells if Actor is switched On, i.e. currently acquired the target.
	 */
	private BooleanProperty isSwitchOnProperty = new SimpleBooleanProperty(false);

	/**
	 * The boolean property that tells if Actor is started.
	 */
	private boolean isActorStarted = false;

	/** Simple beating heart animation. */
	private ScaleTransition heartBeatTransition;
	
	/**
	 * Instantiates a new Actor Cell. Sets the default values, listeners and animation.
	 */
	public ActorCell(ActorCellType actorCellType) {
		this.actorCellType = actorCellType;
		setMaxHeight(getHeight());
		setGraphicTextGap(6.0);
		heart.setSmooth(false);
		heartBeatTransition = new ScaleTransition(Duration.millis(150), heart);
		heartBeatTransition.setToX(heartDefaultSize);
		heartBeatTransition.setToY(heartDefaultSize);
		heartBeatTransition.setFromX(heartBeatSize);
		heartBeatTransition.setFromY(heartBeatSize);
		heartBeatTransition.setAutoReverse(false);
		heartBeatTransition.interpolatorProperty().setValue(Interpolator.DISCRETE);
		heartBeatTransition.setCycleCount(1);
		// Lets make heart's animation a bit easier on CPU (not working -_-)
		// heart.setCache(true);
		// heart.setCacheHint(CacheHint.SCALE);
		// Let's remember original size of heart
		originalHeartBounds = heart.getBoundsInLocal();
		// Let's set heart's initial view
		refreshHeartStyle(isActorStarted, isSwitchOnProperty.get(), checkIntervalProperty.get(),
				elementHealthProperty.get());
		ChangeListener<ElementState> elementStateListener = (observableValue, oldElementState,
				newElementState) -> Platform.runLater(() -> {
					if (isActorStarted != findIfActorStarted(newElementState)) {
						isActorStarted = findIfActorStarted(newElementState);
						synchronized (heart) {
							refreshHeartStyle(isActorStarted, isSwitchOnProperty.get(),
									checkIntervalProperty.get(), elementHealthProperty.get());
						}
					}
					switch (newElementState) {
					case WORKING:
						if (animationOn) {
							heartBeatTransition.playFromStart();
						}
						break;
					default:
						break;
					}
				});
		ChangeListener<ElementHealth> elementHealthListener = (observableValue, oldElementHealth,
				newElementHealth) -> Platform.runLater(() -> {
					synchronized (heart) {
						refreshHeartStyle(isActorStarted, isSwitchOnProperty.get(),
								checkIntervalProperty.get(), newElementHealth);
					}
				});
		ChangeListener<? super Number> checkIntervalListener = (observable, oldValue,
				newValue) -> Platform.runLater(() -> {
					synchronized (heart) {
						refreshHeartStyle(isActorStarted, isSwitchOnProperty.get(),
								newValue.intValue(), elementHealthProperty.get());
					}
				});
		ChangeListener<? super Boolean> isSwitchOnListener = (observable, oldValue,
				newValue) -> Platform.runLater(() -> {
					synchronized (heart) {
						refreshHeartStyle(isActorStarted, newValue, checkIntervalProperty.get(),
								elementHealthProperty.get());
					}
				});
		elementElementStateProperty.addListener(elementStateListener);
		elementHealthProperty.addListener(elementHealthListener);
		checkIntervalProperty.addListener(checkIntervalListener);
		isSwitchOnProperty.addListener(isSwitchOnListener);
		setOnMouseClicked(event -> GUIManager.elementEditor.setCurrentElement(getItem()));
	}

	protected boolean findIfActorStarted(ElementState newElementState) {
		switch (newElementState) {
		case FINISHED:
			// Seems like Actor was started
			return true;
		case WORKING:
			// Seems like Actor was started
			return true;
		default:
			// In other cases Actor might be considered as stopped/not yet started
			return false;
		}
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void updateItem(Actor item, boolean empty) {
		super.updateItem(item, empty);

		if (empty) {
			textProperty().unbind();
			setText(null);
			setGraphic(null);
			setContextMenu(null);
			elementElementStateProperty.unbind();
			elementHealthProperty.unbind();
			checkIntervalProperty.unbind();
			isSwitchOnProperty.unbind();
			isActorStarted = false;
		} else {
			if (isEditing()) {
				if (textField != null) {
					textField.setText(getString());
				}
				textProperty().unbind();
				setText(null);
				setGraphic(textField);
			} else {
				textProperty().unbind();
				setGraphic(heart);
				// Lets set initial heart style
				refreshHeartStyle(
						findIfActorStarted(item.getElementInfo().elementStateProperty().get()),
						item.isSwitchOnProperty().get(), item.checkIntervalProperty().get(),
						item.getElementInfo().elementHealthProperty().get());
				checkIntervalProperty.bind(item.checkIntervalProperty());
				elementElementStateProperty.bind(item.getElementInfo().elementStateProperty());
				textProperty().bind(getTextBinding(item));
				isSwitchOnProperty.bind(item.isSwitchOnProperty());
				elementHealthProperty.bind(item.getElementInfo().elementHealthProperty());
				// refreshHeartStyle();
				PossibleMoves possibleMoves = PossibleMoves.UPORDOWN;
				int sizeOfList = getListView().getItems().size();
				if (sizeOfList != 1) {
					if (getIndex() == 0)
						possibleMoves = PossibleMoves.ONLYDOWN;
					if (getIndex() == sizeOfList - 1)
						possibleMoves = PossibleMoves.ONLYUP;
				} else {
					possibleMoves = PossibleMoves.NOWHERE;
				}
				switch(actorCellType) {
				case ALLACTORSVIEWCELL:
					setContextMenu(ActorContextMenuBuilder
							.createCMForAllActorsViewItem(item,possibleMoves));
					break;
				case STRUCTUREVIEWCELL:
					setContextMenu(ActorContextMenuBuilder
							.createCMForStructureViewItem(item, possibleMoves));
					break;
				default:
					throw new RuntimeException("Unknown ActorCellType");
				}
			}
		}
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void startEdit() {
		super.startEdit();

		if (textField == null) {
			createTextField();
		}
		textProperty().unbind();
		setText(null);
		setGraphic(textField);
		textField.selectAll();
	}

	/*
	 * @inheritDoc
	 */
	@Override
	public void cancelEdit() {
		super.cancelEdit();
		textProperty().bind(getTextBinding(getItem()));
		// setText(getString());
		// setGraphic(getTreeItem().getGraphic());
	}

	/**
	 * Creates the text field for editing {@link Actor}'s name.
	 */
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
		Actor item = getItem();
		if (item != null) {
			int checkInterval = item.getCheckInterval();
			return item.getElementInfo().getName() + " (" + checkInterval + " ms)";
		}
		return "";
	}

	/**
	 * Refresh the {@link #heart}'s style. <br>
	 * Heart can vary it's base look under different circumstances. <br>
	 * Note: this method should not be called too often.
	 *
	 * @param isStarted
	 *            is {@link Actor} started?
	 * @param isSwitchOn
	 *            is Actor currently switched on by True result of his {@link Checker}?
	 * @param checkInterval
	 *            the time interval in ms, how often Actor asks Checker to check.
	 * @param elementHealth
	 *            current Actor's health
	 *
	 */
	private final void refreshHeartStyle(boolean isStarted, boolean isSwitchOn, int checkInterval,
			ElementHealth elementHealth) {
		if (getItem() == null)
			return;
		Color heartColor = null;
		switch (elementHealth) {
		case BROKEN:
			heartColor = Color.INDIGO;
			setHeartText(heartBrokenCharacter);
			setHeartEffect("-fx-effect: dropshadow(gaussian, hotpink, 3,0,0,0);");
			break;
		case HEALTHY:
			heartColor = Color.HOTPINK; // default color for heart of the healthy Actor
			if (isStarted && checkInterval < 150) {
				setHeartText(heartRunAndQuickCheckCharacter);
				// Let's turn animation off, because it's consuming too much CPU with such check
				// interval.
				animationOn = false;
			} else {
				setHeartText(heartDefaultCharacter);
				animationOn = true;
				;
			}
			setHeartEffect("");
			break;
		case SICK:
			heartColor = Color.YELLOW;
			if (isStarted && checkInterval < 150) {
				setHeartText(heartRunAndQuickCheckCharacter);
				// Let's turn animation off, because it's consuming too much CPU with such check
				// interval.
				animationOn = false;
			} else {
				setHeartText(heartDefaultCharacter);
				animationOn = true;
				;
			}
			setHeartEffect("");
			break;
		default:
			break;
		}
		// Resize heart to fit original bounds if new character's size differs
		if (!heart.getText().equals(heartPreviousCharacter)) {
			// Let's stop animation which might interfere in finding new character's size
			heartBeatTransition.stop();
			// With new character set, lets scale heart to default scale size
			scaleHeartToSize(heartDefaultSize, heartDefaultSize);
			// Let's get new bounds and find scale that will allow to fit heart with new character
			// to original bounds
			Bounds newBounds = heart.getBoundsInLocal();
			currentHeartCharacterScaleX = originalHeartBounds.getWidth() / newBounds.getWidth();
			currentHeartCharacterScaleY = originalHeartBounds.getHeight() / newBounds.getHeight();
			heartPreviousCharacter = heart.getText();
		}
		// When the Actor is not started, lets reset it's color and size to defaults.
		if (!isStarted) {
			scaleHeartToSize(heartDefaultSize * currentHeartCharacterScaleX,
					heartDefaultSize * currentHeartCharacterScaleY);
			fillHeartByColor(heartColor);
		}
		// When the Actor is switched On, lets paint the heart to gentle blue
		if (isSwitchOn) {
			fillHeartByColor(Color.DEEPSKYBLUE);
		} else {
			fillHeartByColor(heartColor);
		}
	}

	/**
	 * Scale the {@link #heart} to the chosen size. <br>
	 * Checks if heart already have the proper size.
	 *
	 * @param size
	 *            the size to set.<br>
	 *            1.0 means 100%
	 */
	private void scaleHeartToSize(double scaleX, double scaleY) {
		if (heart.getScaleX() != scaleX)
			heart.setScaleX(scaleX);
		if (heart.getScaleY() != scaleY)
			heart.setScaleY(scaleY);
	}

	/**
	 * Fill the {@link #heart} by the chosen color. <br>
	 * Checks if heart already have the proper color.
	 *
	 * @param color
	 *            the color to use to fill the heart
	 */
	private void fillHeartByColor(Color color) {
		if (heart.getFill() != color)
			heart.setFill(color);
	}

	private void setHeartText(String newText) {
		if (!heart.getText().equals(newText))
			heart.setText(newText);
	}

	private void setHeartEffect(String newEffect) {
		if (!heart.getStyle().equals(newEffect))
			heart.setStyle(newEffect);
	}

	/**
	 * The string binding that represents the text to be shown in the Actor Cell.
	 *
	 * @param actor
	 *            the {@link Actor} bind to
	 * @return the string binding
	 */
	private StringExpression getTextBinding(Actor actor) {
		return Bindings.concat(actor.getElementInfo().nameProperty()).concat(" (")
				.concat(checkIntervalProperty).concat(")");
	}

}
