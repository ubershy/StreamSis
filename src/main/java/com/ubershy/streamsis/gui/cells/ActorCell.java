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
import com.ubershy.streamsis.gui.helperclasses.CuteColor;

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
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.geometry.Rectangle2D;

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

	/** The {@link ImageView} to put inside the {@link #paneGraphic}. */
	private ImageView heartImageView = new ImageView();
	
	/** The graphic to use. */
	private StackPane paneGraphic = new StackPane();

	/** How long the heart beating animation should be played. */
	private final static int animationDuration = 100;
	
	/** The size of a font used to render the heart. */
	private final static int graphicFontSize = 18;
	
	private final static double graphicLogicalSize = 11.0;
	
	private final static double graphicRenderingSize = 25.0;

	/** The gap between the heart graphic and text. */
	private final static double graphicTextGap = 7.0;

	/** The default heart size. */
	private final static double heartDefaultScale = 0.85;

	/** The size of heart when it's beating. */
	private final static double heartBeatScale = 1.0;

	/** The color to use for the heart graphic when the {@link Actor} is normal. */
	private final static Color heartDefaultColor = CuteColor.GENTLEPINK;

	/** The color to use for the heart graphic when the {@link Actor} is switched on. */
	private final static Color heartSwitchedOnColor = CuteColor.GENTLEBLUE;

	/** The color to use when the {@link Actor} is broken. */
	private final static Color brokenColor = CuteColor.GENTLEDARKPURPLE;

	/** The color to use when the {@link Actor} is sick. */
	private final static Color sickColor = CuteColor.GENTLEDARKYELLOW;
	
	/** The color to use when the {@link Actor} is healthy. */
	private final static Color healthyTextColor = Color.BLACK;
	
	private final static Image brokenHeartImage;
	
	private final static Image defaultHeartImage;
	
	private final static Image sickHeartImage;
	
	private final static Image switchedOnHeartImage;
	
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

	static {
		brokenHeartImage = generateBrokenHeartImage();
		defaultHeartImage = generateHeartImage(heartDefaultColor);
		sickHeartImage = generateHeartImage(sickColor);
		switchedOnHeartImage = generateHeartImage(heartSwitchedOnColor);
	}

	/**
	 * Instantiates a new Actor Cell. Sets the default values, listeners and animation.
	 */
	public ActorCell(ActorCellType actorCellType) {
		this.actorCellType = actorCellType;
		heartImageView.setCache(true);
		heartImageView.setCacheHint(CacheHint.SPEED);
		heartImageView.setPreserveRatio(true);
		heartImageView.imageProperty().addListener((o, oldVal, newVal) -> {
			double width = newVal.getWidth();
			double height = newVal.getHeight();
			double halfOfExcessiveWidth = (width - graphicRenderingSize) / 2.0;
			double halfOfExcessiveHeight = (height - graphicRenderingSize) / 2.0;
			Rectangle2D vp = new Rectangle2D(halfOfExcessiveWidth, halfOfExcessiveHeight,
					graphicRenderingSize, graphicRenderingSize);
			heartImageView.setViewport(vp);
		});
		paneGraphic.setMaxHeight(graphicLogicalSize);
		paneGraphic.setMaxWidth(graphicLogicalSize);
		paneGraphic.setMinHeight(graphicLogicalSize);
		paneGraphic.setMinWidth(graphicLogicalSize);
		paneGraphic.getChildren().add(heartImageView);
		setGraphicTextGap(graphicTextGap);
		heartBeatTransition = new ScaleTransition(Duration.millis(animationDuration),
				heartImageView);
		heartBeatTransition.setToX(heartDefaultScale);
		heartBeatTransition.setToY(heartDefaultScale);
		heartBeatTransition.setFromX(heartBeatScale);
		heartBeatTransition.setFromY(heartBeatScale);
		heartBeatTransition.setAutoReverse(false);
		heartBeatTransition.interpolatorProperty().setValue(Interpolator.DISCRETE);
		heartBeatTransition.setCycleCount(1);
		// Let's set heart's initial view
		refreshLook(actorStarted, isSwitchOnProperty.get(), checkIntervalProperty.get(),
				elementHealthProperty.get());
		ChangeListener<ElementState> elementStateListener = (observableValue, oldElementState,
				newElementState) -> Platform.runLater(() -> {
					boolean isActorStartedNow = findIfActorStarted(newElementState);
					if (actorStarted != isActorStartedNow) {
						// Things have changed, need to update heart look.
						actorStarted = isActorStartedNow;
						synchronized (heartImageView) {
							refreshLook(actorStarted, isSwitchOnProperty.get(),
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
					synchronized (heartImageView) {
						refreshLook(actorStarted, isSwitchOnProperty.get(),
								checkIntervalProperty.get(), newElementHealth);
					}
				});
		ChangeListener<? super Number> checkIntervalListener = (observable, oldValue,
				newValue) -> Platform.runLater(() -> {
					synchronized (heartImageView) {
						refreshLook(actorStarted, isSwitchOnProperty.get(),
								newValue.intValue(), elementHealthProperty.get());
					}
				});
		ChangeListener<? super Boolean> isSwitchOnListener = (observable, oldValue,
				newValue) -> Platform.runLater(() -> {
					synchronized (heartImageView) {
						refreshLook(actorStarted, newValue, checkIntervalProperty.get(),
								elementHealthProperty.get());
					}
				});
		elementStateProperty.addListener(elementStateListener);
		elementHealthProperty.addListener(elementHealthListener);
		checkIntervalProperty.addListener(checkIntervalListener);
		isSwitchOnProperty.addListener(isSwitchOnListener);
		setOnMouseClicked(event -> GUIManager.elementEditor.setCurrentElement(getItem()));
	}

	private static Image generateHeartImage(Color color) {
		FontAwesomeIconView generated = new FontAwesomeIconView(FontAwesomeIcon.HEART);
		generated.setGlyphSize(graphicFontSize);
		generated.setFill(color);
		return CellUtils.makeSnapshotWithTransparency(generated);
	}

	private static Image generateBrokenHeartImage() {
		FontAwesomeIconView newHeart = new FontAwesomeIconView(FontAwesomeIcon.HEART);
		newHeart.setGlyphSize(graphicFontSize);
		
		FontAwesomeIconView newBolt = new FontAwesomeIconView(FontAwesomeIcon.BOLT);
		newBolt.setGlyphSize(graphicFontSize);
		newBolt.setScaleX(0.65);
		newBolt.setScaleY(0.65);
		newBolt.setTranslateX(-1.0);
		newHeart.setFill(brokenColor);
		newBolt.setFill(Color.WHITE);
		StackPane generated = new StackPane(
				newHeart,
				newBolt
		);
		StackPane.setAlignment(newBolt, Pos.CENTER);
		StackPane.setAlignment(newHeart, Pos.CENTER);
		generated.setBlendMode(BlendMode.MULTIPLY);
		generated.setEffect(
				new DropShadow(BlurType.GAUSSIAN, heartDefaultColor, 7.0, 0.25, 0.0, 0.0));
		return CellUtils.makeSnapshotWithTransparency(generated);
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
			CellUtils.setGraphicIfNotAlready(this, null);
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
				CellUtils.setGraphicIfNotAlready(this, textField);
			} else {
				textProperty().unbind();
				CellUtils.setGraphicIfNotAlready(this, paneGraphic);
				// Lets set initial style
				refreshLook(findIfActorStarted(item.getElementInfo().elementStateProperty().get()),
						item.isSwitchOnProperty().get(), item.checkIntervalProperty().get(),
						item.getElementInfo().elementHealthProperty().get());
				checkIntervalProperty.bind(item.checkIntervalProperty());
				elementStateProperty.bind(item.getElementInfo().elementStateProperty());
				textProperty().bind(getTextBinding(item));
				isSwitchOnProperty.bind(item.isSwitchOnProperty());
				elementHealthProperty.bind(item.getElementInfo().elementHealthProperty());
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
				switch (actorCellType) {
				case ALLACTORSVIEWCELL:
					setContextMenu(ActorContextMenuBuilder.createCMForAllActorsViewItem(item,
							possibleMoves));
					break;
				case STRUCTUREVIEWCELL:
					setContextMenu(ActorContextMenuBuilder.createCMForStructureViewItem(item,
							possibleMoves));
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
	 * Refresh the heart's style and text style. <br>
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
	private final void refreshLook(boolean isStarted, boolean isSwitchOn, int checkInterval,
			ElementHealth elementHealth) {
		if (getItem() == null)
			return;
		boolean actorIsAbleToWork;
		Image imageToUse = null;
		switch (elementHealth) {
		case BROKEN:
			actorIsAbleToWork = false;
			imageToUse = brokenHeartImage;
			CellUtils.setLabeledTextFillIfNotAlready(this, brokenColor);
			break;
		case HEALTHY:
			actorIsAbleToWork = true;
			imageToUse = defaultHeartImage;
			CellUtils.setLabeledTextFillIfNotAlready(this, healthyTextColor);
			break;
		case SICK:
			actorIsAbleToWork = true;
			imageToUse = sickHeartImage;
			CellUtils.setLabeledTextFillIfNotAlready(this, sickColor);
			break;
		default:
			throw new RuntimeException("What is this status?");
		}
		if (actorIsAbleToWork) {
			if (isStarted && checkInterval < animationDuration) {
				// No need to animate in that case, because it will just consume CPU cycles.
				// Let's just make the heart big.
				CellUtils.scaleNodeToSizeIfNotAlready(heartImageView, heartBeatScale,
						heartBeatScale);
				animationOn = false;
			} else {
				animationOn = true;
			}
			// When the Actor is switched On, need to override with heart of special color.
			if (isSwitchOn) {
				imageToUse = switchedOnHeartImage;
			}
		}
		if (!isStarted) {
			// When the Actor is not started, but able to work, let's reset its size to default,
			// because it might end up with wrong size after stopped animation.
			CellUtils.scaleNodeToSizeIfNotAlready(heartImageView, heartDefaultScale,
					heartDefaultScale);
		}
		CellUtils.setImageIfNotAlready(heartImageView, imageToUse);
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
