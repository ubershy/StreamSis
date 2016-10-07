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
import com.ubershy.streamsis.MultiSourceFileChooser;

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

	@JsonProperty
	private MultiSourceFileChooser fileChooser = new MultiSourceFileChooser();

	static final Logger logger = LoggerFactory.getLogger(MultiSoundAction.class);

	public MultiSoundAction() {
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
		this.volume.set(volume);;
		fileChooser.setSrcPath(soundDirectoryPath);
		fileChooser.setChooseFilesRandomly(chooseFileRandomly);
		fileChooser.setFindingSourcesInSrcPath(true);
		fileChooser.acceptableExtensionsProperty().set(allowedExtensions);
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
		this.volume.set(volume);
		fileChooser.getPersistentSourceFileList().setAll(persistentSourceFileList);
		fileChooser.setChooseFilesRandomly(chooseFileRandomly);
		fileChooser.setFindingSourcesInSrcPath(false);
		fileChooser.acceptableExtensionsProperty().set(allowedExtensions);
	}

	/**
	 * Instantiates a new Multi Sound Action FULLY. Used by deserializator.
	 *
	 * @param volume
	 *            the volume playback from 0 to 1
	 * @param fileChooser
	 *            the {@link MultiSourceFileChooser}
	 */
	@JsonCreator
	public MultiSoundAction(@JsonProperty("volume") double volume,
			@JsonProperty("fileChooser") MultiSourceFileChooser fileChooser) {
		this.volume.set(volume);
		this.fileChooser = fileChooser;
		this.fileChooser.acceptableExtensionsProperty().set(allowedExtensions);
	}

	@Override
	public void execute() {
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			logger.info(
					"Playing Source directory file # " + (fileChooser.getCurrentFileIndex() + 1));
			boolean wasAbleToPlay = play();
			fileChooser.computeNextFileIndex();
			File nextFileToPlay = fileChooser.getTemporarySourceFileList()
					.get(fileChooser.getCurrentFileIndex());
			String nextSoundToPlay = nextFileToPlay.getPath();
			soundToPlay = initializeSound(nextSoundToPlay);
			elementInfo.setBooleanResult(wasAbleToPlay);
		}
	}

	@Override
	public void init() {
		elementInfo.setAsReadyAndHealthy();
		fileChooser.initTemporaryFileList(elementInfo, "sounds", null);
		if (elementInfo.isBroken()) {
			// already broken by fileChooser.initTemporaryFileList()
			return;
		}
		soundToPlay = initializeSound(
				fileChooser.getTemporarySourceFileList().get(fileChooser.getCurrentFileIndex())
						.getPath());
		if (fileChooser.isChoosingFilesRandomly()) {
			fileChooser.computeNextFileIndex();
		}
	}

}
