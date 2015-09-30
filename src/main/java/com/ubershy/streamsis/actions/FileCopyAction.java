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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.project.AbstractCuteNode;

/**
 * File Copy Action. <br>
 * This {@link Action} can copy Destination file to Source file.
 */
@SuppressWarnings("unchecked")
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "actnType")
public class FileCopyAction extends AbstractCuteNode implements Action {

	static final Logger logger = LoggerFactory.getLogger(FileCopyAction.class);

	/** The Destination file's path. */
	@JsonProperty
	protected String dstPath = "";

	/** The variable for storing Destination file's extension. */
	protected String extension;

	/** The Source file's path. */
	@JsonProperty
	protected String srcFilePath = "";

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
			@JsonProperty("dstPath") String dstFilePath) {
		this.dstPath = dstFilePath;
		this.srcFilePath = srcFilePath;
	}

	/**
	 * Check if Source and Destinations file paths are not null or empty.
	 *
	 * @return true, if successful
	 */
	protected boolean checkForEmptinessAndNothingness() {
		if (dstPath.isEmpty()) {
			elementInfo.setAsBroken("Destination path is not defined");
			return false;
		}
		if (srcFilePath.isEmpty()) {
			elementInfo.setAsBroken("Source path is not defined");
			return false;
		}
		return true;
	}

	@Override
	public void execute() {
		if (elementInfo.canWork()) {
			elementInfo.setAsWorking();
			logger.info("Copying file with '" + extension + "' extension");
			try {
				Util.copyFileSynced(new File(srcFilePath), new File(dstPath));
			} catch (IOException e) {
				elementInfo.setFailedResult();
				return;
			}
			elementInfo.setSuccessfulResult();
		}
	}

	public String getDstPath() {
		return dstPath;
	}

	public String getSrcPath() {
		return srcFilePath;
	}

	@Override
	public void init() {
		elementInfo.setAsReadyAndHealthy();
		if (!checkForEmptinessAndNothingness())
			return;
		extension = Util.extractFileExtensionFromPath(dstPath);
		if (extension != null) {
			if (Util.checkifSingleFileExists(srcFilePath)) {
				if (!Util.checkFileExtension(srcFilePath,
						Util.singleStringAsArrayOfStrings(extension))) {
					elementInfo.setAsBroken(
							"Source file and Destination file extensions don't match\nDestination file extension: '"
									+ extension + "' Source file extension: '"
									+ Util.extractFileExtensionFromPath(srcFilePath)
									+ "'\nPlease, make sure you are using files exactly of the same type");
					return;
				}
			} else {
				elementInfo.setAsBroken("Can't find or read Source image file " + srcFilePath);
				return;
			}
		} else {
			elementInfo.setAsBroken("Destination file has no extension: " + dstPath
					+ "\nPlease choose another file");
			return;
		}

	}

	public void setDstPath(String dstPath) {
		this.dstPath = dstPath;
	}

	public void setSrcPath(String srcPath) {
		this.srcFilePath = srcPath;
	}

}
