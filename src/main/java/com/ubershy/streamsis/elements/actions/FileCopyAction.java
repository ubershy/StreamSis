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
package com.ubershy.streamsis.elements.actions;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.elements.AbstractCuteElement;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * File Copy Action. <br>
 * This {@link Action} can copy Source file to Destination file.
 */
public class FileCopyAction extends AbstractCuteElement implements Action {

	static final Logger logger = LoggerFactory.getLogger(FileCopyAction.class);

	/** The Destination file's path. */
	@JsonIgnore
	protected StringProperty dstFilePath = new SimpleStringProperty("");

	/** The variable for storing Destination file's extension. */
	private String dstExtension;

	/** The Source file's path. */
	@JsonIgnore
	protected StringProperty srcFilePath = new SimpleStringProperty("");

	public FileCopyAction() {
	}

	/**
	 * Instantiates a new File Replace Action
	 *
	 * @param srcFilePath
	 *            the Source file's path
	 * @param dstFilePath
	 *            the Destination file's path
	 */
	@JsonCreator
	public FileCopyAction(@JsonProperty("srcFilePath") String srcFilePath,
			@JsonProperty("dstFilePath") String dstFilePath) {
		this();
		this.dstFilePath.set(dstFilePath);
		this.srcFilePath.set(srcFilePath);
	}

	/**
	 * Check if Source and Destinations file path are not empty.
	 *
	 * @return true, if successful
	 */
	protected boolean checkForEmptinessAndNothingness() {
		if (dstFilePath.get().isEmpty()) {
			elementInfo.setAsBroken("Destination path is not defined.");
			return false;
		}
		if (srcFilePath.get().isEmpty()) {
			elementInfo.setAsBroken("Source path is not defined.");
			return false;
		}
		return true;
	}

	@Override
	public void execute() {
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			logger.info("Copying file with '" + dstExtension + "' extension");
			try {
				Util.copyFileSynced(new File(srcFilePath.get()), new File(dstFilePath.get()));
			} catch (IOException e) {
				elementInfo.setBooleanResult(false);
				return;
			}
			elementInfo.setBooleanResult(true);
		}
	}

	@JsonProperty("dstFilePath")
	public String getDstFilePath() {
		return dstFilePath.get();
	}

	@JsonProperty("srcFilePath")
	public String getSrcFilePath() {
		return srcFilePath.get();
	}

	@Override
	public void init() {
		super.init();
		// The method below while executing can set the element as broken.
		if (!checkForEmptinessAndNothingness())
			return;
		String srcExtension = Util.extractFileExtensionFromPath(srcFilePath.get());
		dstExtension = Util.extractFileExtensionFromPath(dstFilePath.get());
		// Let's not allow files without extensions. Let's not allow copying to different file
		// extension.
		if (dstExtension == null) {
			elementInfo.setAsBroken("The file on destination path should have an extension.");
			return;
		} else {
			if (!dstExtension.equals(srcExtension)) {
				elementInfo.setAsBroken("The source and destination files should have same "
						+ "extension.");
				return;
			}
		}
		if (!Util.checkifSingleFileExists(srcFilePath.get())) {
			elementInfo.setAsBroken("Can't find or read Source file " + srcFilePath.get());
			return;
		}
		if (srcFilePath.get().equals(dstFilePath.get())) {
			elementInfo.setAsBroken("Source and destination paths must be different.");
			return;
		}
		if (!Util.checkIfAbsolutePathSeemsValid(dstFilePath.get())) {
			elementInfo.setAsBroken("The destination path seems slightly... invalid.");
			return;
		}
	}
	
	protected void doSuperInit() {
		super.init();
	}

	@JsonProperty("dstFilePath")
	public void setDstFilePath(String dstFilePath) {
		this.dstFilePath.set(dstFilePath);
	}

	@JsonProperty("srcFilePath")
	public void setSrcFilePath(String srcFilePath) {
		this.srcFilePath.set(srcFilePath);
	}

	public StringProperty srcFilePathProperty() {
		return srcFilePath;
	}

	public StringProperty dstFilePathProperty() {
		return dstFilePath;
	}

}
