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

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Labeled;
import javafx.util.Duration;

public class ThreeDotsAnimation {

	private int currentNumOfDots = 1;
	private Timeline animation = new Timeline();
	private Labeled label;
	private String addText;
	private String originalText;

	public ThreeDotsAnimation(String additionalText, Labeled labeled, int minDots) {
		if (additionalText == null || labeled == null) {
			throw new NullPointerException();
		}
		if (minDots < 0 || minDots > 2) {
			throw new IllegalArgumentException("The minimum amount of dots must be from 0 to 2");
		}

		this.label = labeled;
		this.addText = additionalText;
		this.originalText = labeled.getText();
		KeyFrame addNewDotFrame = new KeyFrame(Duration.ZERO, event -> {
			if (currentNumOfDots != 3) {
				currentNumOfDots += 1;
				label.setText(label.getText() + " .");
			} else {
				currentNumOfDots = minDots;
				label.setText(addText);
				for (int i = 0; i < minDots; i++)
					label.setText(label.getText() + " .");
			}
		});
		KeyFrame waitFrame = new KeyFrame(Duration.millis(500));
		animation.getKeyFrames().addAll(addNewDotFrame, waitFrame);
		animation.setCycleCount(Timeline.INDEFINITE);
	}

	public void play() {
		originalText = label.getText();
		label.setText(addText);
		animation.play();
	}

	public void stop() {
		animation.stop();
		label.setText(originalText);
	}
}
