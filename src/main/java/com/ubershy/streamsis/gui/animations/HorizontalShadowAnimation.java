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
package com.ubershy.streamsis.gui.animations;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Labeled;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.util.Duration;

public class HorizontalShadowAnimation {
	private Timeline animation = new Timeline();
	private Labeled labeled;
	private Effect originalEffect;
	private DropShadow dropShadow = new DropShadow();
	private ObjectProperty<Effect> effect = new SimpleObjectProperty<>();

	public HorizontalShadowAnimation(Labeled label) {
		if (label == null) {
			throw new NullPointerException();
		}
		labeled = label;
		originalEffect = label.getEffect();

		dropShadow.setBlurType(BlurType.ONE_PASS_BOX);
		
		setInitialShadow(label);

		EventHandler<ActionEvent> onFinished = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				label.effectProperty().unbind();
				labeled.setEffect(originalEffect);
			}
		};
		KeyValue endValue = new KeyValue(dropShadow.widthProperty(), 0, Interpolator.EASE_BOTH);
		KeyFrame endFrame = new KeyFrame(Duration.millis(150), onFinished, endValue);

		animation.getKeyFrames().addAll(endFrame);
		animation.setCycleCount(1);
		animation.setAutoReverse(false);
		
	}

	private void setInitialShadow(Labeled label) {
//		Color c = Color.valueOf(label.getTextFill().toString());
//		dropShadow.setColor(c);
		dropShadow.setWidth(250);
		dropShadow.setHeight(1);
		effect.set(dropShadow);
		label.effectProperty().bind(effect);

	}

	public void play() {
		setInitialShadow(labeled);
		animation.play();
	}

	public void stop() {
		animation.stop();
		labeled.effectProperty().unbind();
		labeled.setEffect(originalEffect);
	}
}
