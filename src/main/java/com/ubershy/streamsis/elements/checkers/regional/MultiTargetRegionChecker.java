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
package com.ubershy.streamsis.elements.checkers.regional;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.sikuli.script.Finder;
import org.sikuli.script.Image;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.elements.AbstractCuteElement;
import com.ubershy.streamsis.elements.checkers.Checker;
import com.ubershy.streamsis.elements.helpers.Coordinates;
import com.ubershy.streamsis.elements.helpers.MultiSourceFileLister;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * MultiTarget Region Checker. <br>
 * This {@link Checker} is similar to {@link RegionChecker}, but instead of finding one
 * targetPattern image within a region, it tries to find many targets. <br>
 * When it's {@link #useANDOperator} is <b>true</b> this {@link Checker} will return true on
 * {@link #check()} only if <b>all</b> targets are found within the region. <br>
 * When it's {@link #useANDOperator} is <b>false</b> this {@link Checker} will return true on
 * {@link #check()} if <b>at least one</b> of the targets is found within the region. <br>
 */
public class MultiTargetRegionChecker extends AbstractCuteElement implements Checker {
	
	/** The description of this CuteElement type. */
	public final static String description = MultiTargetRegionChecker.class.getSimpleName()
			+ " on check performs 2D image recognition in the specified region on the screen.\n"
			+ "If it's able to recognize the specified image(s) in this region, returns True."
			+ " False otherwise.\n"
			+ "The user can choose should it return True only if all images are found or when any"
			+ " one of the images are found.";

	static final Logger logger = LoggerFactory.getLogger(MultiTargetRegionChecker.class);

	/** The {@link Coordinates} of region where to search the image. */
	@JsonProperty
	protected ObjectProperty<Coordinates> coords = new SimpleObjectProperty<Coordinates>(
			new Coordinates(0, 0, 100, 100));
	public ObjectProperty<Coordinates> coordsProperty() {return coords;}
	public Coordinates getCoords() {return coords.get();}
	public void setCoords(Coordinates coords) {this.coords.set(coords);}

	/**
	 * The similarity. <br>
	 * A float number from 0 to 1.00 specifying the tolerance of how different the searched image
	 * can look in region. <br>
	 * E.g. "1.00f" - exact image. <br>
	 */
	@JsonProperty
	protected FloatProperty similarity = new SimpleFloatProperty(0.95f);
	public FloatProperty similarityProperty() {return similarity;}
	public float getSimilarity() {return similarity.get();}
	public void setSimilarity(float similarity) {this.similarity.set(similarity);}

	/** The targetPatterns {@link org.sikuli.script.Pattern Patterns} to internally work with. */
	@JsonIgnore
	protected ArrayList<Pattern> targets;

	/**
	 * If true, use AND operator(find all Target Images), else - OR operator (find at least one
	 * Target image) for the positive result.
	 */
	@JsonProperty
	protected BooleanProperty useANDOperator = new SimpleBooleanProperty(false);
	public BooleanProperty useANDOperatorProperty() {return useANDOperator;}
	public boolean isUseANDOperator() {return useANDOperator.get();}
	public void setUseANDOperator(boolean useANDOperator) {this.useANDOperator.set(useANDOperator);}
	
	/** The file lister that provides flexible list of targets. */
	@JsonProperty
	private SimpleObjectProperty<MultiSourceFileLister> fileLister = new SimpleObjectProperty<MultiSourceFileLister>(
			new MultiSourceFileLister());
	public SimpleObjectProperty<MultiSourceFileLister> fileListerProperty() {return fileLister;}
	public MultiSourceFileLister getFileLister() {return fileLister.get();}
	public void setFileLister(MultiSourceFileLister fileLister) {this.fileLister.set(fileLister);}
	
	/** The acceptable extensions of Target image files. */
	@JsonIgnore
	public final static List<String> allowedExtensions = Util.singleItemAsList("*.png");
	
	/** The {@link Finder} instance to use. */
	@JsonIgnore
	private Finder finder = new Finder();

	public MultiTargetRegionChecker() {
		fileLister.get().setAcceptableExtensions(allowedExtensions);
	}

	/**
	 * Instantiates a new MultiTarget Region Checker via directory containing target image files.
	 *
	 * @param coords
	 *            The {@link Coordinates} where to find image.
	 * @param targetsDirectoryPath
	 *            The directory containing Target image files.
	 * @param similarity
	 *            The image {@link #similarity} from 0 to 1.0.
	 * @param useANDOperator
	 *            If true, use AND operator(find all Target Images), else - OR operator (find at
	 *            least one Target image) for the positive result.
	 */
	public MultiTargetRegionChecker(Coordinates coords, String targetsDirectoryPath,
			float similarity, boolean useANDOperator) {
		this();
		this.coords.set(coords);
		this.fileLister.get().setSrcPath(targetsDirectoryPath);
		this.fileLister.get().setFindingSourcesInSrcPath(true);
		this.similarity.set(similarity);
		this.useANDOperator.set(useANDOperator);
	}
	
	/**
	 * Instantiates a new MultiTarget Region Checker via list of target image files.
	 *
	 * @param coords
	 *            The {@link Coordinates} where to find image.
	 * @param persistentSourceFileList
	 *            The list with Target image files.
	 * @param similarity
	 *            The image {@link #similarity} from 0 to 1.0.
	 * @param useANDOperator
	 *            If true, use AND operator(find all Target Images), else - OR operator (find at
	 *            least one Target image) for the positive result.
	 */
	public MultiTargetRegionChecker(Coordinates coords, ArrayList<File> persistentSourceFileList,
			float similarity, boolean useANDOperator) {
		this();
		this.coords.set(coords);
		this.fileLister.get().getPersistentSourceFileList().setAll(persistentSourceFileList);
		this.fileLister.get().setFindingSourcesInSrcPath(false);
		this.similarity.set(similarity);
		this.useANDOperator.set(useANDOperator);
	}
	
	/**
	 * Instantiates a new MultiTarget Region Checker via {@link MultiSourceFileLister}.
	 * Used by deserialization.
	 *
	 * @param coords
	 *            The {@link Coordinates} where to find image.
	 * @param fileLister
	 *            The instance of {@link MultiSourceFileLister}.
	 * @param similarity
	 *            The image {@link #similarity} from 0 to 1.0.
	 * @param useANDOperator
	 *            If true, use AND operator(find all Target Images), else - OR operator (find at
	 *            least one Target image) for the positive result.
	 */
	@JsonCreator
	public MultiTargetRegionChecker(@JsonProperty("coords") Coordinates coords,
			@JsonProperty("fileLister") MultiSourceFileLister fileLister,
			@JsonProperty("similarity") float similarity,
			@JsonProperty("useANDOperator") boolean useANDOperator) {
		this.coords.set(coords);
		this.fileLister.set(fileLister);
		this.fileLister.get().setAcceptableExtensions(allowedExtensions);
		this.similarity.set(similarity);
		this.useANDOperator.set(useANDOperator);
	}

	@Override
	public boolean check() {
		boolean result = false;
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			// TODO: debug mode variable in config
			// if (Util.debugMode) {
			// coords.highlightRegion();
			// }
			Screen screen = (Screen) coords.get().getRegion().getScreen();
			ScreenImage screenImage = screen.capture(coords.get().getRegion());
			finder.resetImage(new Image(screenImage));
			if (useANDOperator.get() == true) { // AND operator
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
			elementInfo.setBooleanResult(result);
		}
		return result;
	}

	@Override
	public void init() {
		super.init();
		coords.get().initRegion(elementInfo);
		if (elementInfo.isBroken()) {
			// already broken by coords.get().initRegion();
			return;
		}
		if (similarity.get() > 1 || similarity.get() <= 0) {
			elementInfo.setAsBroken("Similarity parameter must be from 0 to 1.00");
			return;
		}
		fileLister.get().initTemporaryFileList(elementInfo, "Target image files", null);
		if (elementInfo.isBroken()) {
			// already broken by fileLister.get().initTemporaryFileList() or null extension
			return;
		}
		ReadOnlyListProperty<File> targetsList = fileLister.get().getTemporarySourceFileList();
		targets = new ArrayList<Pattern>(targetsList.size());
		for (File f : targetsList) {
			Image.unCacheBundledImage(f.getAbsolutePath());
			targets.add(new Pattern(f.toString()).similar(similarity.get()));
		}
	}

}
