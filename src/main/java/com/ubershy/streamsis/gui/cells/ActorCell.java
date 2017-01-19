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
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
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
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
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

	/** The text field for editing the {@link Actor}'s name. (not working, unused) */
	private TextField textField;

	/** The graphic to use when {@link Actor}'s heart is broken. =( */
	private Node brokenHeartGraphic;

	/** The graphic to use when {@link Actor}'s health is ok and it's checkinterval. */
	private Text normalHeartGraphic;

	/** How long the heart beating animation should be played. */
	private final static int animationDuration = 100;
	
	/** The size of a font used to render the heart. */
	private final static int graphicFontSize = 18;

	/**
	 * The relative coordinates on axis X that will allow the heart to be rendered more beautiful.
	 */
	private final static double graphicTranslateX = -3.0;
	
	/** The gap between the heart graphic and text. */
	private final static double graphicTextGap = 1.0;

	/** The default heart size. */
	private final static double heartDefaultScale = 0.85;

	/** The size of heart when it's beating. */
	private final static double heartBeatScale = 1.0;

	/** The color to use for the heart graphic when the {@link Actor} is normal. */
	private final static Color heartDefaultColor = Color.HOTPINK;

	/** The color to use for the heart graphic when the {@link Actor} is switched on. */
	private final static Color heartSwitchedOnColor = Color.DEEPSKYBLUE;

	/** The color to use for the heart graphic when the {@link Actor} is broken. */
	private final static Color heartBrokenColor = Color.INDIGO;

	/** The color to use for the heart graphic when the {@link Actor} is sick. */
	private final static Color heartSickColor = Color.YELLOW;
	
	/** Defines if {@link #heart} animates. */
	private boolean animationOn = true;

	/** The property linked to the {@link CuteElement}'s state. */
	private ObjectProperty<ElementState> elementStateProperty = new SimpleObjectProperty<>(
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

	/** The boolean property that tells if Actor is started. */
	private boolean actorStarted = false;

	/** Simple beating heart animation. */
	private ScaleTransition heartBeatTransition;
	
	/**
	 * Instantiates a new Actor Cell. Sets the default values, listeners and animation.
	 */
	public ActorCell(ActorCellType actorCellType) {
		this.actorCellType = actorCellType;
		setMaxHeight(getHeight());
		setGraphicTextGap(graphicTextGap);
		brokenHeartGraphic = generateBrokenHeartGraphic();
		normalHeartGraphic = generateNormalHeartGraphic();
		heartBeatTransition = new ScaleTransition(Duration.millis(animationDuration),
				normalHeartGraphic);
		heartBeatTransition.setToX(heartDefaultScale);
		heartBeatTransition.setToY(heartDefaultScale);
		heartBeatTransition.setFromX(heartBeatScale);
		heartBeatTransition.setFromY(heartBeatScale);
		heartBeatTransition.setAutoReverse(false);
		heartBeatTransition.interpolatorProperty().setValue(Interpolator.DISCRETE);
		heartBeatTransition.setCycleCount(1);
		// Let's set heart's initial view
		refreshHeartLook(actorStarted, isSwitchOnProperty.get(), checkIntervalProperty.get(),
				elementHealthProperty.get());
		ChangeListener<ElementState> elementStateListener = (observableValue, oldElementState,
				newElementState) -> Platform.runLater(() -> {
					boolean isActorStartedNow = findIfActorStarted(newElementState);
					if (actorStarted != isActorStartedNow) {
						// Things have changed, need to update heart look.
						actorStarted = isActorStartedNow;
						synchronized (normalHeartGraphic) {
							refreshHeartLook(actorStarted, isSwitchOnProperty.get(),
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
					synchronized (normalHeartGraphic) {
						refreshHeartLook(actorStarted, isSwitchOnProperty.get(),
								checkIntervalProperty.get(), newElementHealth);
					}
				});
		ChangeListener<? super Number> checkIntervalListener = (observable, oldValue,
				newValue) -> Platform.runLater(() -> {
					synchronized (normalHeartGraphic) {
						refreshHeartLook(actorStarted, isSwitchOnProperty.get(),
								newValue.intValue(), elementHealthProperty.get());
					}
				});
		ChangeListener<? super Boolean> isSwitchOnListener = (observable, oldValue,
				newValue) -> Platform.runLater(() -> {
					synchronized (normalHeartGraphic) {
						refreshHeartLook(actorStarted, newValue, checkIntervalProperty.get(),
								elementHealthProperty.get());
					}
				});
		elementStateProperty.addListener(elementStateListener);
		elementHealthProperty.addListener(elementHealthListener);
		checkIntervalProperty.addListener(checkIntervalListener);
		isSwitchOnProperty.addListener(isSwitchOnListener);
		setOnMouseClicked(event -> GUIManager.elementEditor.setCurrentElement(getItem()));
	}

	private Text generateNormalHeartGraphic() {
		FontAwesomeIconView generated = new FontAwesomeIconView(FontAwesomeIcon.HEART);
		generated.setGlyphSize(graphicFontSize);
		generated.setFill(heartDefaultColor);
		generated.setTranslateX(graphicTranslateX);
		scaleNodeToSizeIfNeeded(generated, heartDefaultScale, heartDefaultScale);
		generated.setSmooth(false);
		generated.setCache(true);
		generated.setCacheHint(CacheHint.SPEED);
		return generated;
	}

	private Node generateBrokenHeartGraphic() {
		// FIXME: The "crack" on heart is just white, it is not transparent. So violet shadow isn't
		// there, when it's supposed to be there. Also when StreamSis will have skins, it will just
		// look ugly on dark backgrounds. I tried to use Shape.substract(), but it's hard to align
		// images and borders end up strange.
		// FIXME: Need to render picture once and reuse it later in imageviews.
		FontAwesomeIconView newHeart = new FontAwesomeIconView(FontAwesomeIcon.HEART);
		newHeart.setGlyphSize(graphicFontSize);
		newHeart.setSmooth(false);
		
		FontAwesomeIconView newBolt = new FontAwesomeIconView(FontAwesomeIcon.BOLT);
		newBolt.setGlyphSize(graphicFontSize);
		newBolt.setSmooth(false);
		newBolt.setScaleX(0.6);
		newBolt.setScaleY(0.6);
		newBolt.setX(5.0);
		newHeart.setFill(heartBrokenColor);
		newBolt.setFill(Color.WHITE);
		StackPane generated = new StackPane(
				newHeart,
				newBolt
		);
		StackPane.setAlignment(newBolt, Pos.TOP_CENTER);
		StackPane.setAlignment(newHeart, Pos.BOTTOM_CENTER);
		generated.setBlendMode(BlendMode.MULTIPLY);

		generated.setTranslateX(graphicTranslateX);
		scaleNodeToSizeIfNeeded(generated, heartDefaultScale, heartDefaultScale);
		generated.setStyle("-fx-effect: dropshadow(gaussian, "
				+ GUIUtil.getNameOfColorInstance(heartDefaultColor) + ", 10,0.25,0,0);");
		generated.setCache(true);
		generated.setCacheHint(CacheHint.SPEED);
		return generated;
	}

	private boolean findIfActorStarted(ElementState newElementState) {
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
			elementStateProperty.unbind();
			elementHealthProperty.unbind();
			checkIntervalProperty.unbind();
			isSwitchOnProperty.unbind();
			actorStarted = false;
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
				// Lets set initial heart style
				refreshHeartLook(
						findIfActorStarted(item.getElementInfo().elementStateProperty().get()),
						item.isSwitchOnProperty().get(), item.checkIntervalProperty().get(),
						item.getElementInfo().elementHealthProperty().get());
				checkIntervalProperty.bind(item.checkIntervalProperty());
				elementStateProperty.bind(item.getElementInfo().elementStateProperty());
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
	 * Refresh the heart's style. <br>
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
	private final void refreshHeartLook(boolean isStarted, boolean isSwitchOn, int checkInterval,
			ElementHealth elementHealth) {
		if (getItem() == null)
			return;
		Color heartColorModifier = null;
		boolean actorIsAbleToWork;
		Node graphicToUse = null;
		switch (elementHealth) {
		case BROKEN:
			actorIsAbleToWork = false;
			graphicToUse = brokenHeartGraphic;
			break;
		case HEALTHY:
			actorIsAbleToWork = true;
			heartColorModifier = heartDefaultColor;
			break;
		case SICK:
			actorIsAbleToWork = true;
			heartColorModifier = heartSickColor;
			break;
		default:
			throw new RuntimeException("What is this status?");
		}
		if (actorIsAbleToWork) {
			if (isStarted && checkInterval < animationDuration) {
				// No need to animate in that case, because it will just consume CPU cycles.
				// Let's just make the heart big.
				scaleNodeToSizeIfNeeded(normalHeartGraphic, heartBeatScale, heartBeatScale);
				animationOn = false;
			} else {
				animationOn = true;
			}
			if (!isStarted) {
				// When the Actor is not started, but able to work, let's reset its size to default,
				// because it might end up with wrong size after stopped animation.
				scaleNodeToSizeIfNeeded(normalHeartGraphic, heartDefaultScale, heartDefaultScale);
			}
			// When the Actor is switched On, lets paint the heart with special color.
			if (isSwitchOn) {
				fillHeartTextByColorIfNeeded(normalHeartGraphic, heartSwitchedOnColor);
			} else {
				fillHeartTextByColorIfNeeded(normalHeartGraphic, heartColorModifier);
			}
			graphicToUse = normalHeartGraphic;
		}
		setGraphicIfNeeded(graphicToUse);
	}

	/**
	 * Scale the heart to the chosen size. <br>
	 * Checks if heart already have the proper size.
	 *
	 * @param size
	 *            the size to set.<br>
	 *            1.0 means 100%
	 */
	private static void scaleNodeToSizeIfNeeded(Node heart, double scaleX, double scaleY) {
		if (heart.getScaleX() != scaleX)
			heart.setScaleX(scaleX);
		if (heart.getScaleY() != scaleY)
			heart.setScaleY(scaleY);
	}

	/**
	 * Fill the heart by the chosen color. <br>
	 * Checks if heart already have the proper color.
	 *
	 * @param color
	 *            the color to use to fill the heart
	 */
	private static void fillHeartTextByColorIfNeeded(Text heart, Color color) {
		if (heart.getFill() != color)
			heart.setFill(color);
	}

	private void setGraphicIfNeeded(Node newGraphic) {
		if (getGraphic() == null || !getGraphic().equals(newGraphic))
			setGraphic(newGraphic);
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
