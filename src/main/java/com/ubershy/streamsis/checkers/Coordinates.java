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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents the coordinates of the region on display screen.
 * 
 * @see {@link RegionChecker}
 * @see {@link MultiTargetRegionChecker}
 */
public class Coordinates {

	/** The screen number (0 is main screen). */
	@JsonProperty
	private int x, y, w, h, screenNumber;

	/**
	 * Instantiates new coordinates for a region.
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
	public Coordinates(@JsonProperty("x") int x, @JsonProperty("y") int y, @JsonProperty("w") int w, @JsonProperty("h") int h,
			@JsonProperty("screenNumber") int screenNumber) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.screenNumber = screenNumber;
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
	 * This method generates a new {@link org.sikuli.script.Region Region} region based on Coordinates.
	 *
	 * @return the region
	 */
	public Region generateRegion() {
		return new Region(x, y, w, h, screenNumber);
	}

}