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
package com.ubershy.streamsis.gui.helperclasses;

import java.util.Arrays;
import java.util.Collection;

import org.controlsfx.control.decoration.Decoration;
import org.controlsfx.control.decoration.GraphicDecoration;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationMessage;
import org.controlsfx.validation.decoration.GraphicValidationDecoration;

import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * The CuteGraphicValidationDecoration. It's like {@link GraphicValidationDecoration} but cute. <br>
 * The styles of validation tooltips and graphic indicators are changed here.
 */
public class CuteGraphicValidationDecoration extends GraphicValidationDecoration {
	
	protected ValidationMessage lastMessage;
	
	protected ScaleTransition animation = new ScaleTransition(Duration.millis(200));

	/** The common style elements shared between "error" and "warning" validation tooltips. */
	private static final String TP_COMMON_STYLE = "-fx-border-color: white;"
			+ " -fx-background-color: white; -fx-font-weight: bold; -fx-padding: 3;";

	/** The style for "error" validation tooltip. */
	private static final String ERROR_TP_STYLE = TP_COMMON_STYLE + "-fx-text-fill: darkorchid;"
			+ " -fx-effect: dropshadow(three-pass-box, hotpink, 10, 0, 0, 0);";

	/** The style for "warning" validation tooltip. */
	private static final String WARNING_TP_STYLE = TP_COMMON_STYLE + "-fx-text-fill: goldenrod;"
			+ " -fx-effect: dropshadow(three-pass-box, goldenrod, 10, 0, 0, 0);";

	/** The image used for "required" indicator. */
	private static final Image REQUIRED_IMAGE = new Image(GraphicValidationDecoration.class
			.getResource("/impl/org/controlsfx/control/validation/required-indicator.png")
			.toExternalForm());

    /**
     * Instantiates CuteGraphicValidationDecoration.
     */
    public CuteGraphicValidationDecoration() {
    	animation.setCycleCount(1);
    	animation.setAutoReverse(false);
    	animation.setFromX(0.0);
		animation.setFromY(0.0);
		animation.setToX(1.0);
		animation.setToY(1.0);
    }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Node createErrorNode() {
		Text icon = GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.CLOSE)
				.build();
		icon.setFill(Color.INDIGO);
		icon.setScaleX(1.5);
		icon.setScaleY(1.5);
		return icon;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Node createWarningNode() {
		Text icon = GlyphsDude.createIcon(FontAwesomeIcon.SQUARE);
		icon.setFill(Color.LEMONCHIFFON);
		icon.setScaleX(0.8);
		icon.setScaleY(0.8);
		return icon;
	}

	/**
	 * {@inheritDoc}
	 */
	private Node createDecorationNode(ValidationMessage message) {
		Node icon = Severity.ERROR == message.getSeverity() ? createErrorNode()
				: createWarningNode();
		Label label = new Label();
		label.setGraphic(icon);
		Tooltip tp = createTooltip(message);
		label.setTooltip(tp);
		label.setAlignment(Pos.CENTER);
		// Play animation only when message text differs from previous message.
		if (lastMessage == null || !lastMessage.getText().equals(message.getText())) {
			animation.setNode(label);
			animation.playFromStart();
		}
		lastMessage = message;
		return label;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Tooltip createTooltip(ValidationMessage message) {
		Tooltip tp = new Tooltip(message.getText());
		tp.setAutoFix(true);
		tp.setAutoHide(true);
		tp.setConsumeAutoHidingEvents(false);
		tp.setStyle(Severity.ERROR == message.getSeverity() ? ERROR_TP_STYLE : WARNING_TP_STYLE);
		return tp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<Decoration> createValidationDecorations(ValidationMessage message) {
		return Arrays.asList(new GraphicDecoration(createDecorationNode(message), Pos.BOTTOM_LEFT));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<Decoration> createRequiredDecorations(Control target) {
		ImageView imageView = new ImageView(REQUIRED_IMAGE);
		// The following code will transform REQUIRED_IMAGE from red to blue.
		ColorAdjust adjust = new ColorAdjust();
		adjust.setSaturation(-1);
		Blend bluemaker = new Blend(BlendMode.SRC_ATOP, adjust,
				new ColorInput(0, 0, imageView.getImage().getWidth(),
						imageView.getImage().getHeight(), Color.DEEPSKYBLUE));
		imageView.setEffect(bluemaker);
		return Arrays.asList(new GraphicDecoration(imageView, Pos.TOP_LEFT,
				REQUIRED_IMAGE.getWidth() / 2, REQUIRED_IMAGE.getHeight() / 2));
	}

}
