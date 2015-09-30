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
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ubershy.streamsis.Util;

/**
 * Multi File Copy Action. <br>
 * This {@link Action} can copy Destination file with one of the files from Source file directory or
 * from predefined list of files. <br>
 * It can choose a particular file either <b>randomly</b> or <b>sequentially</b>.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "actnType")
public class MultiFileCopyAction extends FileCopyAction {

	static final Logger logger = LoggerFactory.getLogger(MultiFileCopyAction.class);

	@JsonProperty
	private MultiSourceFileChooser fileChooser = new MultiSourceFileChooser();

	public MultiFileCopyAction() {
	}

	/**
	 * Instantiates a new Multi File Copy Action by path with files.
	 *
	 * @param srcDirectoryPath
	 *            the Source <b>directory</b>'s path
	 * @param dstFilePath
	 *            the Destination <b>file</b>'s path
	 * @param chooseFileRandomly
	 *            choose file randomly
	 */
	public MultiFileCopyAction(String srcDirectoryPath, String dstFilePath,
			boolean chooseFileRandomly) {
		fileChooser.srcPath = srcDirectoryPath;
		this.dstPath = dstFilePath;
		fileChooser.chooseFileRandomly = chooseFileRandomly;
		fileChooser.findSourcesInSrcPath = true;
	}

	/**
	 * Instantiates a new Multi File Copy Action by path with files.
	 *
	 * @param srcDirectoryPath
	 *            the Source <b>directory</b>'s path
	 * @param dstFilePath
	 *            the Destination <b>file</b>'s path
	 * @param chooseFileRandomly
	 *            choose file randomly
	 */
	public MultiFileCopyAction(ArrayList<File> persistentSourceFileList, String dstFilePath,
			boolean chooseFileRandomly) {
		fileChooser.persistentSourceFileList.setAll(persistentSourceFileList);
		this.dstPath = dstFilePath;
		fileChooser.chooseFileRandomly = chooseFileRandomly;
		fileChooser.findSourcesInSrcPath = false;
	}

	/**
	 * Instantiates a new Multi File Copy Action FULLY. Used by deserializator.
	 *
	 * @param dstFilePath
	 *            the Destination <b>file</b>'s path
	 * @param fileChooser
	 *            the {@link MultiSourceFileChooser}
	 */
	@JsonCreator
	public MultiFileCopyAction(@JsonProperty("dstPath") String dstFilePath,
			@JsonProperty("fileChooser") MultiSourceFileChooser fileChooser) {
		this.dstPath = dstFilePath;
		this.fileChooser = fileChooser;
	}

	@Override
	public void execute() {
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			logger.info("Copying Source directory file № " + (fileChooser.currentFileIndex + 1)
					+ " with '" + extension + "' extension");
			File fileToCopy = fileChooser.temporarySourceFileList[fileChooser.currentFileIndex];
			fileChooser.computeNextFileIndex();
			try {
				Util.copyFileSynced(fileToCopy, new File(dstPath));
			} catch (IOException e) {
				elementInfo.setFailedResult();
				return;
			}
			elementInfo.setSuccessfulResult();
		}
	}

	@Override
	public void init() {
		elementInfo.setAsReadyAndHealthy();
		if (dstPath.isEmpty()) {
			elementInfo.setAsBroken("Destination path is not defined");
			return;
		}
		extension = Util.extractFileExtensionFromPath(dstPath);
		if (extension != null) {
			fileChooser.initTemporaryFileList(elementInfo, new String[] { extension },
					"Source files");
		} else {
			elementInfo.setAsBroken("Destination file has no extension: " + dstPath
					+ "\nPlease choose another file");
		}
		if (elementInfo.isBroken()) {
			// already broken by fileChooser.initTemporaryFileList() or null extension
			return;
		}
		if (fileChooser.chooseFileRandomly) {
			fileChooser.computeNextFileIndex();
		}
	}

}
