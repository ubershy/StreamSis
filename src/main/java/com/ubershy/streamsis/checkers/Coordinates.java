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
package com.ubershy.streamsis.checkers;

import org.sikuli.script.Region;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.project.ElementInfo;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * This class represents the coordinates of the rectangular region on screen.
 * 
 * @see {@link RegionChecker}
 * @see {@link MultiTargetRegionChecker}
 */
public class Coordinates {
	
	/**
	 * The X coordinate on screen where region starts. <br>
	 * The coordinate grid starts from upper left corner of upper left screen.
	 */
	public final IntegerProperty xProperty() {return x;}
	public final void setX(int x) {this.x.set(x);}
	public final int getX() {return this.x.get();}
	@JsonProperty
	private IntegerProperty x = new SimpleIntegerProperty(0);
	/**
	 * The Y coordinate on screen where region starts. <br>
	 * The coordinate grid starts from upper left corner of upper left screen.
	 */
	public final IntegerProperty yProperty() {return y;}
	public final void setY(int y) {this.y.set(y);}
	public final int getY() {return this.y.get();}
	@JsonProperty
	private IntegerProperty y = new SimpleIntegerProperty(0);
	/**
	 * The Width of the region on screen.
	 */
	public final IntegerProperty wProperty() {return w;}
	public final void setW(int w) {this.w.set(w);}
	public final int getW() {return this.w.get();}
	@JsonProperty
	private IntegerProperty w = new SimpleIntegerProperty(0);
	/**
	 * The Height of the region on screen.
	 */
	public final IntegerProperty hProperty() {return h;}
	public final void setH(int h) {this.h.set(h);}
	public final int getH() {return this.h.get();}
	@JsonProperty
	private IntegerProperty h = new SimpleIntegerProperty(0);
	/**
	 * The id number of screen (0 is main screen).
	 */
	public final IntegerProperty screenNumberProperty() {return screenNumber;}
	public final void setScreenNumber(int screenNumber) {this.screenNumber.set(screenNumber);}
	public final int getScreenNumber() {return this.screenNumber.get();}
	@JsonProperty
	private IntegerProperty screenNumber = new SimpleIntegerProperty(0);

	/** 
	 * The {@link org.sikuli.script.Region Region} to generate from Coordinates. 
	 */
	@JsonIgnore
	public Region getRegion() {return region;}
	@JsonIgnore
	private Region region;
	
	/**
	 * Instantiates new Coordinates for a region.
	 *
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 * @param w
	 *            the weight
	 * @param h
	 *            the height
	 * @param screenNumber
	 *            the screen number (0 is main screen)
	 */
	public Coordinates(@JsonProperty("x") int x, @JsonProperty("y") int y, @JsonProperty("w") int w,
			@JsonProperty("h") int h, @JsonProperty("screenNumber") int screenNumber) {
		this.x.set(x);
		this.y.set(y);
		this.w.set(w);
		this.h.set(h);
		this.screenNumber.set(screenNumber);
	}

	/**
	 * Instantiates a new coordinates for a region, assuming it's located on screen 0 (main screen).
	 *
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 * @param w
	 *            the weight
	 * @param h
	 *            the height
	 */
	public Coordinates(int x, int y, int w, int h) {
		this(x, y, w, h, 0);
	}

	/**
	 * This method generates a new {@link #getRegion()} based on current Coordinates, might set the
	 * provided elementInfo as broken.
	 *
	 * @param elementInfo
	 *            The ElementInfo of CuteElement that contains this Coordinates.
	 * @return The Region.
	 */
	public void initRegion(ElementInfo elementInfo) {
		Region potentialRegion = new Region(x.get(), y.get(), w.get(), h.get(), screenNumber.get());
		if (potentialRegion.isValid()) {
			potentialRegion.setAutoWaitTimeout(0.0);
			region = potentialRegion;
		} else {
			region = null;
			elementInfo
					.setAsBroken("Something wrong with the Region. Please enter valid Coordinates");
		}
	}

	/**
	 * Highlights the region with red rectangle on computer screen. <br>
	 * Very useful for debugging.
	 */
	public void highlightRegion() {
		// Executing in sub thread as this operation may hang the current thread.
		Thread thread = new Thread(() -> {
			if (region != null) {
				region.highlight(1, "#FF69B4"); // Using "Hot Pink" color
			}
		});
		thread.start();
	}
	
}