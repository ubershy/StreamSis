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

import java.util.ArrayList;

import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

/**
 * The text animation that symbolizes some process. <br>
 * It appends a white space and dot to the specified text in each frame. <br>
 * The maximum number of appended dots is three.
 * <p>
 * Example: <br>
 * "Some text" <br>
 * "Some text ." <br>
 * "Some text . ." <br> 
 * "Some text . . ."
 */
public class ThreeDotsAnimation {

	private Timeline animation = new Timeline();
	
	/** The text property to which the animation is applied. */
	private StringProperty textProperty;
	/** The text to be show near dots. */
	private String addText;
	/** The text to restore after animation is stopped. */
	private String originalText;

	/**
	 * {@link ThreeDotsAnimation} constructor.
	 *
	 * @param additionalText
	 *            The text to be show near dots.
	 * @param dotChar
	 *            The char to use as "dot".
	 * @param textProperty
	 *            The text property to apply animation to.
	 * @param minDots
	 *            The minimum amount of dots to show, i.e. 0 means animation will contain frame
	 *            without dots. Affects number of frames in animation.
	 * @param singleDuration
	 *            The duration of a single frame.
	 * @param cycleCount
	 *            The cycle count, -1 means endless loop.
	 */
	public ThreeDotsAnimation(String additionalText, char dotChar, StringProperty textProperty,
			int minDots, double singleDuration, int cycleCount) {
		if (additionalText == null || textProperty == null) {
			throw new NullPointerException();
		}
		if (minDots < 0 || minDots > 2) {
			throw new IllegalArgumentException("The minimum amount of dots must be from 0 to 2");
		}
		this.textProperty = textProperty;
		this.addText = additionalText;
		this.originalText = textProperty.get();
		ArrayList<KeyFrame> frames = new ArrayList<KeyFrame>();
		int numberOfFrames = 4 - minDots;
		for (int i = 0; i < numberOfFrames; i++) {
			frames.add(generateFrame(textProperty, i, singleDuration, dotChar, minDots));
		}
		// Frame that just adds delay, so the result of previous Frame will continue to be visible.
		frames.add(new KeyFrame(Duration.millis(singleDuration * numberOfFrames)));
		animation.getKeyFrames().addAll(frames);
		animation.setCycleCount(cycleCount);
	}

	private KeyFrame generateFrame(StringProperty stringProperty, int frameNumber,
			double singleDuration, char dotChar, int startNumber) {
		KeyFrame frame = new KeyFrame(Duration.millis(singleDuration * frameNumber),
				event -> {
					stringProperty.set(
							addText + generateStringWithDots(frameNumber + startNumber, dotChar));
				});
		return frame;
	}

	private String generateStringWithDots(int numberOfDots, char dotChar) {
		String dots = "";
		for (int i = 0; i < numberOfDots; i++) {
			dots = dots + " " + dotChar;
		}
		return dots;
	}

	/**
	 * Remembers the current value of {@link #textProperty} and plays the animation from start.
	 */
	public void play() {
		originalText = textProperty.get();
		animation.playFromStart();
	}

	/**
	 * Stops the animation, restores the original value of {@link #textProperty}.
	 */
	public void stop() {
		animation.stop();
		textProperty.set(originalText);
	}
	
	public void setOnFinished(EventHandler<ActionEvent> value) {
		animation.setOnFinished(value);
	}
	
	/**
	 * Tells if this {@link ThreeDotsAnimation} is currently running.
	 */
	public boolean isRunning() {
		return Status.RUNNING.equals(animation.getStatus());
	}
	
	/**
	 * Gets the {@link #addText}.
	 */
	public String getAdditionalText() {
		return addText;
	}
	
	/**
	 * Sets the {@link #addText}.
	 */
	public void setAdditionalText(String text) {
		addText = text;
	}
}
