/** 
 * StreamSis
 * Copyright (C) 2017 Eva Balycheva
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
package com.ubershy.streamsis.elements.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.sikuli.script.Finder;
import org.sikuli.script.Image;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.elements.AbstractCuteElement;
import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.CuteElementContainer;
import com.ubershy.streamsis.elements.helpers.Coordinates;
import com.ubershy.streamsis.elements.parts.TargetImageWithActions;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Region Switch Action.<br>
 * This {@link Action} on execution scans the specified region on screen for expected Target images
 * and runs the Actions associated with the found Target image.<br>
 * If no Target image is found within the region, Default Actions are executed.
 */
public class RegionSwitchAction extends AbstractCuteElement implements Action {

	static final Logger logger = LoggerFactory.getLogger(RegionSwitchAction.class);
	
	/** The description of this CuteElement type. */
	public final static String description = RegionSwitchAction.class.getSimpleName()
			+ " on execution scans the specified region on screen for expected Target images and"
			+ " runs the Actions associated with the found Target image.\n"
			+ "If no Target image is found withing the region, Default Actions are executed.\n"
			+ "Default Actions and Target images should be added inside.";
	
	/**
	 * The similarity. <br>
	 * A float number from 0 to 1.00 specifying the tolerance of how different the searched image
	 * can look in the region. <br>
	 * E.g. "1.00f" - exact image. <br>
	 */
	@JsonProperty("similarity")
	protected FloatProperty similarity = new SimpleFloatProperty(0.95f);
	public FloatProperty similarityProperty() {return similarity;};
	public float getSimilarity() {return similarity.get();}
	public void setSimilarity(float similarity) {this.similarity.set(similarity);}
	
	/** The {@link Coordinates} of region where to search the images. */
	@JsonProperty("coords")
	protected ObjectProperty<Coordinates> coords = new SimpleObjectProperty<Coordinates>(
			new Coordinates(0, 0, 100, 100));
	public ObjectProperty<Coordinates> coordsProperty() {return coords;}
	public Coordinates getCoords() {return coords.get();}
	public void setCoords(Coordinates coords) {this.coords.set(coords);}
	
	
	/**
	 * Determines if {@link RegionSwitchAction} should find the best match or first.
	 */
	@JsonProperty("findingBest")
	protected BooleanProperty findingBest = new SimpleBooleanProperty(false);
	public BooleanProperty findingBestProperty() {return findingBest;}
	public boolean isFindingBest() {return findingBest.get();}
	public void setFindingBest(boolean findingBest) {this.findingBest.set(findingBest);}	
	
	/** The list of {@link TargetImageWithActions}. */
	@JsonProperty("targetImageWithActionsList")
	protected ObservableList<TargetImageWithActions> targetImageWithActionsList = FXCollections
			.observableArrayList();
	
	/** The list of {@link Action Actions} to execute when no target images found. */
	@JsonProperty("defaultActions")
	protected ObservableList<Action> defaultActions = FXCollections.observableArrayList();
	
	/** The targetPatterns {@link org.sikuli.script.Pattern Patterns} to internally work with. */
	@JsonIgnore
	protected Pattern[] targets;
	
	ExecutorService executorService = Executors.newFixedThreadPool(4);
	
	/** The list of Actor's External children needed just for {@link #getChildren()} method. */
	@JsonIgnore
	protected ObservableList<CuteElement> children = generateExternalChildrenList();
	
	public static class FinderTask implements Callable<Match> {
		
		private Finder finder; 
		private Pattern pattern;

	    public FinderTask(Finder finder, Pattern pattern) {
	        this.finder = finder;
	        this.pattern = pattern;
	    }

	    @Override
	    public Match call() {
			finder.find(pattern);
			return finder.next();
	    }
	}
	
	public RegionSwitchAction() {
	}

	@JsonCreator
	private RegionSwitchAction(@JsonProperty("similarity") float similarity,
			@JsonProperty("coords") Coordinates coords,
			@JsonProperty("findingBest") boolean findingBest,
			@JsonProperty("defaultActions") ArrayList<Action> defaultActions,
			@JsonProperty("targetImageWithActionsList") ArrayList<TargetImageWithActions> targetImageWithActionsList) {
		this.similarity.set(similarity);
		this.coords.set(coords);
		this.findingBest.set(findingBest);
		this.defaultActions.setAll(defaultActions);
		this.targetImageWithActionsList.setAll(targetImageWithActionsList);
	}
	
	private ObservableList<CuteElement> generateExternalChildrenList() {
		CuteElementContainer<TargetImageWithActions> targetImagesContainer = new CuteElementContainer<TargetImageWithActions>(
				targetImageWithActionsList, "Target Images container",
				AddableChildrenTypeInfo.TARGETIMAGEWITHACTIONS, MaxAddableChildrenCount.INFINITY,
				MinAddableChildrenCount.ONE, "Target Images");
		CuteElementContainer<Action> defaultActionsContainer = new CuteElementContainer<Action>(
				defaultActions, "Default Actions container", AddableChildrenTypeInfo.ACTION,
				MaxAddableChildrenCount.INFINITY, MinAddableChildrenCount.UNDEFINEDORZERO,
				"Default Actions");
		return FXCollections.observableArrayList(targetImagesContainer, defaultActionsContainer);
	}

	@Override
	public void init() {
		super.init();
		coords.get().initRegion(elementInfo);
		if (elementInfo.isBroken()) {
			// already broken by coords.initRegion();
			return;
		}
		// Create map for storing image file paths. It will allow later to check duplicate values. 
		HashMap<String, TargetImageWithActions> pathTargetImageWithActionsMap = new HashMap<>(
				targetImageWithActionsList.size());
		// Initialize and fill array with targets.
		targets = new Pattern[targetImageWithActionsList.size()];
		int i = 0;
		for (TargetImageWithActions ita: targetImageWithActionsList) {
			String itaName = ita.getElementInfo().getName();
			String imageFilePath = ita.getTargetImagePath();
			Image.unCacheBundledImage(imageFilePath);
			Pattern target = new Pattern(imageFilePath).similar(similarity.get());
			targets[i] = target;
			Image image = target.getImage();
			// Check if image will fit in the region.
			if (image.getSize().getWidth() > coords.get().getW()) {
				elementInfo.setAsBroken("The contained Target image's ('" + itaName
						+ "') width should be smaller than the width of the specified Region.");
				return;
			}
			if (image.getSize().getHeight() > coords.get().getH()) {
				elementInfo.setAsBroken("The contained Target image's ('" + itaName
						+ "') width should be smaller than the width of the specified Region.");
				return;
			}
			// Check for duplicate target image file paths.
			TargetImageWithActions previousValue = pathTargetImageWithActionsMap.put(imageFilePath,
					ita);
			if (previousValue != null) {
				elementInfo.setAsBroken("The contained Target images '" + itaName + "' and '"
						+ previousValue.getElementInfo().getName()
						+ "' can't use the same file path '" + imageFilePath + "'.");
			}
			i++;
		}
	}

	@Override
	public void execute() {
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			Region region = coords.get().getRegion();
			Screen screen = (Screen) region.getScreen();
			ScreenImage screenImage = screen.capture(region);
			Finder finder = new Finder(screenImage);
			int foundMatchIndex;
			if (findingBest.get()) {
				foundMatchIndex = findBestMatchIndex(finder, targets);
			} else {
				foundMatchIndex = findFirstMatchIndex(finder, targets);
			}
			if (foundMatchIndex == -1) { // No targets found. Need to execute Default Actions.
				logger.info(elementInfo.getName()
						+ ": no Target images found in the region. Executing Default Actions.");
				for (Action action: defaultActions) {
					action.execute();
				}
				elementInfo.setBooleanResult(false);
				return;
			}
			TargetImageWithActions targetImageWithActions = targetImageWithActionsList
					.get(foundMatchIndex);
			ObservableList<Action> actions = targetImageWithActions.getActions();
			logger.info(elementInfo.getName() + ": executing Actions associated with '"
					+ targetImageWithActions.getElementInfo().getName() + "' Target image.");
			for (Action action: actions) {
				action.execute();
			}
			elementInfo.setBooleanResult(true);
		}
	}

	/**
	 * Finds the first match that satisfies minimum acceptable similarity and returns index of it
	 * according to the provided array of Patterns.
	 *
	 * @param finder
	 *            The Finder with captured image inside.
	 * @param targetList
	 *            The array with targets (Patterns) with specified minimum similarity.
	 * @return The first target index in array that matches screen capture, -1 if there are no
	 *         matches.
	 */
	private int findFirstMatchIndex(Finder finder, Pattern[] targetList) {
		for (int i = 0; i < targetList.length; i++) {
			finder.find(targetList[i]);
			if (finder.next() != null) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Finds the best match among matches that satisfy minimum acceptable similarity and returns
	 * index of it according to the provided array of Patterns.
	 *
	 * @param finder
	 *            The Finder with captured image inside.
	 * @param targetList
	 *            The array with targets (Patterns) with specified minimum similarity.
	 * @return The target index in array with the best score, -1 if there are no matches.
	 */
	private int findBestMatchIndex(Finder finder, Pattern[] targetList) {
		int bestIndex = -1;
		double bestScore = 0.0;
		Match[] matches = getMatchesMultithreaded(finder, targetList);
		if (matches == null) {
			return -1;
		}
		int i = 0;
		for (Match match: matches) {
			if (match != null) { // Pattern found.
				double score = match.getScore();
				if (bestScore < score) {
					bestScore = score;
					bestIndex = i;
				}
			}
			i++;
		}
		return bestIndex;
	}

	/**
	 * Gets the array of matches on screen corresponding to targetArray's targets.
	 *
	 * @param finder The Finder with captured image inside.
	 * @param targetsArray The array with targets (Patterns) with specified minimum similarity.
	 * @return The array with matches corresponding to targetArray's targets.
	 */
	private Match[] getMatchesMultithreaded(Finder finder, Pattern[] targetsArray) {
		Match[] matches = new Match[targetsArray.length];
		ArrayList<Future<Match>> futures = new ArrayList<Future<Match>>(targetsArray.length);
		// Run tasks.
		for (Pattern p: targetsArray) {
			Future<Match> future = executorService.submit(new FinderTask(finder, p));
			futures.add(future);
		}
		// Wait for results and collect them.
		int matchIndex = 0;
		for (Future<Match> future: futures) {
			try {
				matches[matchIndex] = future.get();
			} catch (InterruptedException | ExecutionException e) {
				return null;
			}
			matchIndex++;
		}
		return matches;
	}
	
	@Override
	public ObservableList<? extends CuteElement> getChildren() {
		return children;
	}
	
}
