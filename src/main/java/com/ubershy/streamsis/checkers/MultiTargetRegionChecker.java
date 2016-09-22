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

import java.io.File;
import java.util.ArrayList;

import org.sikuli.script.Finder;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.project.AbstractCuteNode;

/**
 * MultiTarget Region Checker. <br>
 * This {@link Checker} is similar to {@link RegionChecker}, but instead of finding one targetPattern image within a region, it tries to find many targets. <br>
 * When it's {@link #useANDOperator} is <b>true</b> this {@link Checker} will return true on {@link #check()} only if <b>all</b> targets are found within the
 * region. <br>
 * When it's {@link #useANDOperator} is <b>false</b> this {@link Checker} will return true on {@link #check()} if <b>at least one</b> of the targets is found
 * within the region. <br>
 */
@SuppressWarnings("unchecked")
public class MultiTargetRegionChecker extends AbstractCuteNode implements Checker {

	static final Logger logger = LoggerFactory.getLogger(MultiTargetRegionChecker.class);

	/** The {@link Coordinates} of region where to search the image. */
	@JsonProperty
	protected Coordinates coords = new Coordinates(0, 0, 100, 100);

	/** The {@link org.sikuli.script.Region Region} to internally work with. */
	protected Region region;

	/** The {@link org.sikuli.script.Screen} to work with. */
	protected Screen screen;

	/**
	 * The similarity. <br>
	 * A float number from 0 to 1.00 specifying the tolerance of how different the searched image can look in region. <br>
	 * E.g. "1.00f" - exact image. <br>
	 */
	@JsonProperty
	protected float similarity = 1.00f;

	/** The targetPatterns {@link org.sikuli.script.Pattern Patterns} to internally work with. */
	protected ArrayList<Pattern> targets;

	/** The path of directory containing targetPattern images. */
	@JsonProperty
	protected String targetsPath = "";

	/** If true, MultiTargetRegionChecker will use AND operator, else - OR operator */
	@JsonProperty
	protected boolean useANDOperator = false;

	public MultiTargetRegionChecker() {
	}

	/**
	 * Instantiates a new multi targetPattern region checker.
	 *
	 * @param coords
	 *            the coords
	 * @param targetsDirectoryPath
	 *            the targets directory path
	 * @param similarity
	 *            the similarity
	 * @param useANDOperator
	 *            the and condition
	 */
	@JsonCreator
	public MultiTargetRegionChecker(@JsonProperty("coords") Coordinates coords, @JsonProperty("targetsPath") String targetsDirectoryPath,
			@JsonProperty("similarity") float similarity, @JsonProperty("useANDOperator") boolean useANDOperator) {
		this.coords = coords;
		this.targetsPath = targetsDirectoryPath;
		this.similarity = similarity;
		this.useANDOperator = useANDOperator;
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
			ScreenImage screenImage = screen.capture(region);
			Finder finder = new Finder(screenImage);
			if (useANDOperator == true) { // AND operator
				result = true;
				for (Pattern p : targets) {
					finder.find(p);
					result = result && finder.hasNext();
					if (result == false)
						break;
				}
			} else { // OR operator
				for (Pattern p : targets) {
					finder.find(p);
					result = result || finder.hasNext();
					if (result == true)
						break;
				}
			}
			finder.destroy();
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

	public String getTargetsPath() {
		return targetsPath;
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
		this.region = coords.generateRegion();
		this.screen = (Screen) region.getScreen();
		if (!this.region.isValid()) {
			elementInfo.setAsBroken("Something wrong with Region. Please enter valid Coordinates");
			return;
		}
		if (similarity > 1 || similarity <= 0) {
			elementInfo.setAsBroken("Similarity parameter must be from 0 to 1.00");
			return;
		}
		if (targetsPath.isEmpty()) {
			elementInfo.setAsBroken("Target images directory is not defined");
			return;
		}
		File[] targetsList = Util.findFilesInDirectory(targetsPath, Util.singleStringAsArrayOfStrings(".png"));
		if (targetsList != null) {
			if (targetsList.length == 0) {
				elementInfo.setAsBroken("No '.png' image files are found in the Target images directory: " + targetsPath);
				return;
			}
		} else {
			elementInfo.setAsBroken("Something is wrong with the Target images directory: " + targetsPath);
			return;
		}
		if (!elementInfo.isBroken()) {
			targets = new ArrayList<Pattern>(targetsList.length);
			for (File f : targetsList) {
				targets.add(new Pattern(f.toString()).similar(similarity));
			}
		}
	}

	public boolean isUseANDOperator() {
		return useANDOperator;
	}

	public void setCoords(Coordinates coords) {
		this.coords = coords;
	}

	public void setSimilarity(float similarity) {
		this.similarity = similarity;
	}

	public void setTargetsPath(String targetsPath) {
		this.targetsPath = targetsPath;
	}

	public void setUseANDOperator(boolean useANDOperator) {
		this.useANDOperator = useANDOperator;
	}

}
