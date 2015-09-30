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
package com.ubershy.streamsis.actors;

import java.io.File;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.StreamSis;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.actions.Action;
import com.ubershy.streamsis.actions.FileCopyAction;
import com.ubershy.streamsis.actions.MultiFileCopyAction;
import com.ubershy.streamsis.actions.MultiSoundAction;
import com.ubershy.streamsis.checkers.Checker;
import com.ubershy.streamsis.checkers.Coordinates;
import com.ubershy.streamsis.checkers.MultiTargetRegionChecker;

/**
 * This class will be probably deleted after {@link StreamSis} will get fully functional GUI.<br>
 * It is intended now <b>just to save programmer's time</b>. <br>
 * It generates {@link Actor Actors}, some types of {@link Checker Checkers} and some types of {@link Action Actions} automatically based on passed arguments
 * and the contents of special {@link ConstsAndVars#actorBuilderResourcesDir resource directory}. <br>
 * To build all these things, resource directory's contents must be placed and named properly. <br>
 * <p>
 * The directory structure of {@link ConstsAndVars#actorBuilderResourcesDir resource directory} must be:<br>
 * |-{Name of the resource} <i>- for example "Actor1Resources"</i><br>
 * |---Images <i>- contains images of a <b>single type</b> (".png", ".jpg", ".gif") for generating {@link MultiFileCopyAction}</i><br>
 * |---Texts <i>- contains texts (".txt") for generating {@link MultiFileCopyAction}</i><br>
 * |---Sounds <i>- contains sounds (".wav", ".mp3", ".ogg") for generating {@link MultiSoundAction}</i><br>
 * |---Targets <i>- contains images (".png") for generating {@link MultiTargetRegionChecker}</i><br>
 * |---current.png <i>- a file to pass to OBS as layer. When Actor is switches on, it is replaced by one of the files in Image directory. When Actor is switched
 * off, it is replaced by default.png</i><br>
 * |---current.jpg <i>- ...</i><br>
 * |---current.gif <i>- ...</i><br>
 * |---current.txt <i>- ...</i><br>
 * |---default.png <i>- explanation is given above</i> <br>
 * |---default.jpg <i>- ...</i><br>
 * |---default.gif <i>- ...</i><br>
 * |---default.txt <i>- ...</i><br>
 * |-{Name of another resource} <i>- for example "NiceActorResources"</i><br>
 * |- ... and so on<br>
 * <p>
 * The Images, Texts and Sounds directories can be empty. Empty directory means no corresponding action will be generated.
 */
public class ActorBuilder {

	static final Logger logger = LoggerFactory.getLogger(ActorBuilder.class);

	/** The directory with resources needed for ActorBuilder to build Actors. */
	private String actorBuilderResourcesDir;

	/**
	 * Instantiates a new ActorBuilder.
	 *
	 * @param actorBuilderResourcesDir
	 *            the directory with resources needed for ActorBuilder to build Actors.
	 */
	public ActorBuilder(String actorBuilderResourcesDir) {
		this.actorBuilderResourcesDir = actorBuilderResourcesDir;
	}

	/**
	 * Creates the {@link UniversalActor}.
	 *
	 * @param name
	 *            Name of {@link Actor} (and also the name of associated directory inside {@link ConstsAndVars#actorBuilderResourcesDir Resources Directory})
	 * @param checkInterval
	 *            the {@link Actor}'s Check interval
	 * @param repeatInterval
	 *            the {@link Actor}'s Repeat interval. if <i>"0"</i>, {@link Actor} will not repeat any Actions
	 * @param doOnRepeat
	 *            true to repeat On Actions when {@link Actor} is switched On
	 * @param doOffRepeat
	 *            true to repeat Off Actions when {@link Actor} is switched Off
	 * @param coords
	 *            the Coordinates of region to check by {@link MultiTargetRegionChecker}
	 * @param similarity
	 *            the {@link MultiTargetRegionChecker#similarity similarity} of image to find
	 * @param outToSharedFiles
	 *            copy files to shared files directory or not if some Actions need something to copy
	 * @param andCondition
	 *            the and condition
	 * @param doUndo
	 *            revert changes back when Actor is executing Off Actions?
	 * @return the UniversalActor
	 */
	@SuppressWarnings("serial")
	public UniversalActor createUniversalActor(String name, int checkInterval, int repeatInterval, boolean doOnRepeat, boolean doOffRepeat, Coordinates coords,
			float similarity, boolean outToSharedFiles, boolean andCondition, boolean doUndo) {
		ArrayList<Action> actions = new ArrayList<Action>();
		;
		ArrayList<Action> undoActions = new ArrayList<Action>();
		;
		fillListsWithActions(name, outToSharedFiles, doUndo, actions, undoActions);
		return new UniversalActor(name, checkInterval, repeatInterval, doOnRepeat, doOffRepeat, new ArrayList<Checker>() {
			{
				add(createChecker(name, coords, andCondition, similarity));
			}
		}, actions, undoActions);
	}

	/**
	 * Creates the Checker of type {@link MultiTargetRegionChecker}.
	 *
	 * @param name
	 *            the name
	 * @param coords
	 *            the Coordinates of region to check by MultiTargetRegionChecker
	 * @param andCondition
	 *            the and condition
	 * @param similarity
	 *            the similarity of image to find
	 * @return {@link MultiTargetRegionChecker}
	 */
	public Checker createChecker(String name, Coordinates coords, boolean andCondition, float similarity) {
		File sharedDir = new File(actorBuilderResourcesDir);
		File actorDir = new File(sharedDir, name);
		File targetDir = new File(actorDir, "Targets");
		return new MultiTargetRegionChecker(coords, targetDir.toString(), similarity, andCondition);
	}

	/**
	 * Creates the actions.
	 *
	 * @param name
	 *            the name
	 * @param outToSharedFiles
	 *            copy files to shared files directory if some Actions need something to copy
	 * @param doUndo
	 *            the do undo
	 * @param actions
	 *            the actions
	 * @param undoActions
	 *            the undo actions
	 * @return the ArrayList of Actions
	 */
	private void fillListsWithActions(String name, boolean outToSharedFiles, boolean doUndo, ArrayList<Action> actions, ArrayList<Action> undoActions) {

		File sharedDir = new File(actorBuilderResourcesDir);
		File actorDir = new File(sharedDir, name);
		File sourceImagesDir = new File(actorDir, "Images");

		String extension = ".png"; // Default extension
		// Based on extension of the first file in Images directory we'll choose extension to work with.
		String[] possibleExtensions = new String[] { ".png", ".jpg", ".bmp", ".gif" };
		if (!sourceImagesDir.toString().isEmpty()) {
			File[] files = Util.findFilesInDirectory(sourceImagesDir.toString(), possibleExtensions);
			if (files != null) {
				if (files.length != 0) {
					extension = files[0].getName().substring(files[0].getName().lastIndexOf("."), files[0].getName().length());
				}
			}
		}

		File defaultImg = new File(actorDir, "default" + extension);
		File destinationImg = null;

		File sourceTextsDir = new File(actorDir, "Texts");
		File defaultText = new File(actorDir, "default.txt");
		File destinationText = null;

		File sourceSoundsDir = new File(actorDir, "Sounds");

		destinationImg = new File(outToSharedFiles ? sharedDir : actorDir, "current" + extension);
		destinationText = new File(outToSharedFiles ? sharedDir : actorDir, "current.txt");

		if (sourceImagesDir.exists()) {
			Action imgAction = new MultiFileCopyAction(sourceImagesDir.toString(), destinationImg.toString(), true);
			initActionAndAddToListIfNotBroken(imgAction, actions);
			if (doUndo && !imgAction.getElementInfo().isBroken()) {
				Action undoImgAction = new FileCopyAction(defaultImg.toString(), destinationImg.toString());
				initActionAndAddToListIfNotBroken(undoImgAction, undoActions);
			}
		}

		if (sourceTextsDir.exists()) {
			Action textAction = new MultiFileCopyAction(sourceTextsDir.toString(), destinationText.toString(), true);
			initActionAndAddToListIfNotBroken(textAction, actions);
			if (doUndo && !textAction.getElementInfo().isBroken()) {
				Action undoTextAction = new FileCopyAction(defaultText.toString(), destinationText.toString());
				initActionAndAddToListIfNotBroken(undoTextAction, undoActions);
			}
		}

		if (sourceSoundsDir.exists()) {
			Action soundAction = new MultiSoundAction(sourceSoundsDir.toString(), 1.0, true);
			initActionAndAddToListIfNotBroken(soundAction, actions);
		}
	}

	/**
	 * Inits the action and add to list if not broken.
	 *
	 * @param action
	 *            the action
	 * @param list
	 *            the list
	 */
	private void initActionAndAddToListIfNotBroken(Action action, ArrayList<Action> list) {
		action.init();
		if (!action.getElementInfo().isBroken())
			list.add(action);
	}
}
