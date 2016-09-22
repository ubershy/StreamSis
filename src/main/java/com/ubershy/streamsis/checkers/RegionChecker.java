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

import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.project.AbstractCuteNode;

/**
 * Region Checker. <br>
 * This {@link Checker} can scan a region(represented by {@link Coordinates}) on the display screen for the presence of the targetPattern image. <br>
 * Returns true on {@link #check()} when the targetPattern image is contained within the region. <br>
 * The region can be bigger than the targetPattern image we search inside it. <br>
 * <p>
 * The tolerance of how different the searched image can look in region is specified by {@link #similarity} parameter from 0 to 1.0, e.g.: <br>
 * "1.0" - exact image. <br>
 * "0.9" - almost exact image. <br>
 * "0.5" - everything that slightly reminds the targetPattern image. <br>
 * "0" - everything.
 */
@SuppressWarnings("unchecked")
public class RegionChecker extends AbstractCuteNode implements Checker {

	static final Logger logger = LoggerFactory.getLogger(RegionChecker.class);

	/** The {@link Coordinates} of region where to search the image. */
	@JsonProperty
	protected Coordinates coords = new Coordinates(0, 0, 100, 100);

	/** The {@link org.sikuli.script.Region Region} to internally work with. */
	protected Region region;

	/**
	 * The similarity. <br>
	 * A float number from 0 to 1.00 specifying the tolerance of how different the searched image can look in region. <br>
	 * E.g. "1.00f" - exact image. <br>
	 */
	@JsonProperty
	protected float similarity = 1.00f;

	/** The path of target image. */
	@JsonProperty
	protected String targetImagePath = "";

	/** The target {@link org.sikuli.script.Pattern Pattern} to internally work with. */
	protected Pattern targetPattern;

	public RegionChecker() {
	}

	/**
	 * Instantiates a new RegionChecker.
	 *
	 * @param coords
	 *            the {@link Coordinates} of region where to search the image.
	 * @param targetImagePath
	 *            the file path of the target image
	 * @param similarity
	 *            the image {@link #similarity} from 0 to 1.0
	 */
	@JsonCreator
	public RegionChecker(@JsonProperty("coords") Coordinates coords, @JsonProperty("targetImagePath") String targetImagePath,
			@JsonProperty("similarity") float similarity) {
		this.targetImagePath = targetImagePath;
		this.similarity = similarity;
		this.coords = coords;
	}

	@Override
	public boolean check() {
		boolean result = false;
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			// TODO: debug mode variable in config
			// if (Util.debugMode) {
			// highlight();
			// }
			result = (region.exists(targetPattern, 0) != null);
			elementInfo.setBooleanResult(result);
		}
		return result;
	}

	public Coordinates getCoords() {
		return coords;
	}

	public float getSimilarity() {
		return similarity;
	}

	public String getTargetImagePath() {
		return targetImagePath;
	}

	/**
	 * Highlights the region. <br>
	 * Very useful for debugging.
	 */
	public void highlight() {
		region.highlight(1);
		region.wait(1.0);
	}

	@Override
	public void init() {
		elementInfo.setAsReadyAndHealthy();
		region = coords.generateRegion();
		if (!region.isValid()) {
			elementInfo.setAsBroken("Something wrong with the Region. Please enter valid Coordinates");
		}
		if (similarity > 1 || similarity <= 0) {
			elementInfo.setAsBroken("Similarity parameter must be from 0 to 1.00");
		}
		if (!targetImagePath.isEmpty()) {
			if (Util.checkSingleFileExistanceAndExtension(targetImagePath, Util.singleStringAsArrayOfStrings(".png"))) {
				targetPattern = new Pattern(targetImagePath).similar(similarity);
			} else {
				elementInfo.setAsBroken("Can't find or read Target image file: " + targetImagePath);
			}
		} else {
			elementInfo.setAsBroken("Target image file is not defined");
		}
	}

	public void setCoords(Coordinates coords) {
		this.coords = coords;
	}

	public void setSimilarity(float similarity) {
		this.similarity = similarity;
	}

	public void setTargetImagePath(String targetImagePath) {
		this.targetImagePath = targetImagePath;
	}
}
