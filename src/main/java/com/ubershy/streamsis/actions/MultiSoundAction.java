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
package com.ubershy.streamsis.actions;

import java.io.File;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.MultiSourceFilePicker;

/**
 * Multi Sound Action. <br>
 * This {@link Action} can play one of the sound files from Source file directory or from predefined
 * list of files. <br>
 * It can choose a particular sound file either <b>randomly</b> or
 * <b>sequentially</b>. <br>
 * Supports ".wav", ".mp3" and ".ogg" files. <br>
 * TODO: some wav files can't be played as they have different format. Find a solution.
 */
public class MultiSoundAction extends SoundAction {

	/** The file picker that helps to choose each next sound file. */
	@JsonProperty
	private MultiSourceFilePicker filePicker = new MultiSourceFilePicker();

	static final Logger logger = LoggerFactory.getLogger(MultiSoundAction.class);

	public MultiSoundAction() {
		filePicker.setAcceptableExtensions(allowedExtensions);
	}

	/**
	 * Instantiates a new Multi Sound Action by path with sound files.
	 *
	 * @param soundDirectoryPath
	 *            the directory where sounds are stored
	 * @param volume
	 *            the volume playback from 0 to 1
	 * @param chooseFileRandomly
	 *            choose file randomly or sequentially from the list of source files
	 */
	public MultiSoundAction(String soundDirectoryPath, double volume, boolean chooseFileRandomly) {
		this();
		this.volume.set(volume);;
		filePicker.setSrcPath(soundDirectoryPath);
		filePicker.setPickingFilesRandomly(chooseFileRandomly);
		filePicker.setFindingSourcesInSrcPath(true);
	}

	/**
	 * Instantiates a new Multi Sound Action by list of sound files.
	 *
	 * @param persistentSourceFileList
	 *            a list with files to use as source files
	 * @param volume
	 *            the volume playback from 0 to 1
	 * @param chooseFileRandomly
	 *            choose file randomly or sequentially from the list of source files
	 */
	public MultiSoundAction(ArrayList<File> persistentSourceFileList, double volume,
			boolean chooseFileRandomly) {
		this();
		this.volume.set(volume);
		filePicker.getPersistentSourceFileList().setAll(persistentSourceFileList);
		filePicker.setPickingFilesRandomly(chooseFileRandomly);
		filePicker.setFindingSourcesInSrcPath(false);
		filePicker.setAcceptableExtensions(allowedExtensions);
	}

	/**
	 * Instantiates a new Multi Sound Action FULLY. Used by deserializator.
	 *
	 * @param volume
	 *            the volume playback from 0 to 1
	 * @param filePicker
	 *            the {@link MultiSourceFilePicker}
	 */
	@JsonCreator
	public MultiSoundAction(@JsonProperty("volume") double volume,
			@JsonProperty("filePicker") MultiSourceFilePicker filePicker) {
		this.volume.set(volume);
		this.filePicker = filePicker;
		this.filePicker.setAcceptableExtensions(allowedExtensions);
	}

	@Override
	public void execute() {
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			logger.info(
					"Playing Source directory file # " + (filePicker.getCurrentFileIndex() + 1));
			boolean wasAbleToPlay = play();
			filePicker.computeNextFileIndex();
			File nextFileToPlay = filePicker.getTemporarySourceFileList()
					.get(filePicker.getCurrentFileIndex());
			String nextSoundToPlay = nextFileToPlay.getPath();
			soundToPlay = initializeSound(nextSoundToPlay);
			elementInfo.setBooleanResult(wasAbleToPlay);
		}
	}

	@Override
	public void init() {
		elementInfo.setAsReadyAndHealthy();
		filePicker.initTemporaryFileList(elementInfo, "sounds", null);
		if (elementInfo.isBroken()) {
			// already broken by filePicker.initTemporaryFileList()
			return;
		}
		soundToPlay = initializeSound(
				filePicker.getTemporarySourceFileList().get(filePicker.getCurrentFileIndex())
						.getPath());
		if (filePicker.isPickingFilesRandomly()) {
			filePicker.computeNextFileIndex();
		}
	}

	/**
	 * Gets the {@link #filePicker}.
	 *
	 * @return The {@link #filePicker}.
	 */
	public MultiSourceFilePicker getFilePicker() {
		return filePicker;
	}
	
	/**
	 * Sets the {@link #filePicker}. In most cases it's not needed to set filePicker. This setter
	 * exists only for automatic copying of attributes.
	 *
	 * @param filePicker
	 *            The new {@link #filePicker}.
	 */
	public void setFilePicker(MultiSourceFilePicker filePicker) {
		filePicker.setAcceptableExtensions(allowedExtensions);
		this.filePicker = filePicker;
	}

}
