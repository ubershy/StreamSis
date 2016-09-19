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
package com.ubershy.streamsis;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for {@link com.ubershy.streamsis.StreamSis StreamSis} that contains some useful methods.
 */
public final class Util {

	/** The Constant logger. */
	static final Logger logger = LoggerFactory.getLogger(Util.class);

	/**
	 * Instantiates a new util.
	 */
	private Util() {
		throw new RuntimeException("You can't instantiate me, b-b-baka!");
	}

	/**
	 * Check if file's extension is valid. <br>
	 * Ignores if extension is lowercase or uppercase. <br>
	 * Only accepts extensions in ".extension" format.
	 *
	 * @param filePath
	 *            the path of file (can be just a name of file, e.g. "image.jpg"
	 * @param extensions
	 *            the list of allowed extensions in ".extension" format
	 * @return true, if extension is valid
	 * @throws IllegalArgumentException
	 *             if file path or one of extensions is empty
	 * @throws NullPointerException
	 *             if arguments contain nulls
	 */
	public static boolean checkFileExtension(String filePath, String[] extensions) {
		if (filePath != null) {
			if (filePath.isEmpty()) {
				throw new IllegalArgumentException("File path can't be empty");
			}
		} else {
			throw new NullPointerException("File path can't be null");
		}

		if (extensions != null) {
			for (String extension : extensions) {
				if (extension != null) {
					if (extension.isEmpty()) {
						throw new IllegalArgumentException("Extension can't be empty");
					}
				} else {
					throw new NullPointerException("Extension can't be null");
				}
			}
		} else {
			throw new NullPointerException("Array of extensions can't be null");
		}

		// Set initial value
		boolean result = false;
		String lowercaseName = filePath.toLowerCase();
		for (String ext : extensions) {
			result = result || lowercaseName.endsWith(ext);
		}
		return result;
	}

	/**
	 * Extracts file's extension in ".extension" format. <br>
	 *
	 * @param path
	 *            the file path
	 * @return the extension in lowercase,<br>
	 *         null if file has no extension
	 * @throws IllegalArgumentException
	 *             if file path is empty
	 * @throws NullPointerException
	 *             if file path is null
	 */
	public static String extractFileExtensionFromPath(String path) {
		if (path != null) {
			if (path.isEmpty()) {
				throw new IllegalArgumentException("File path can't be empty");
			}
		} else {
			throw new NullPointerException("File path can't be null");
		}
		String lowercaseName = path.toLowerCase();
		int lastIndexOfDot = lowercaseName.lastIndexOf(".");
		if (lastIndexOfDot == -1) {
			return null;
		}
		return lowercaseName.substring(lastIndexOfDot);
	}

	/**
	 * Check if directory exists and is not a file.
	 *
	 * @param path
	 *            the path of directory
	 * @return true, if directory exists and is directory(not a file)
	 * @throws NullPointerException
	 *             if directory path is null
	 */
	public static boolean checkDirectory(String path) {
		if (path != null) {
			if (path.isEmpty()) {
				return false;
			}
		} else {
			throw new NullPointerException("File path can't be null");
		}
		boolean result = false;
		File directory = new File(path);
		if (directory.exists()) {
			if (directory.isDirectory()) {
				result = true;
			} else {
				logger.debug("File is not directory: " + path);
			}
		} else {
			logger.debug("This directory does not exist: " + path);
		}
		return result;
	}

	/**
	 * Checks if file exists and has proper extension. Only accepts extensions in ".extension" format.
	 *
	 * @param path
	 *            the path of file
	 * @param extensions
	 *            allowed extensions in ".extension" format
	 * @return true, if file exists and is not a directory and also has a valid extension
	 * @throws IllegalArgumentException
	 *             if file path or one of extensions is empty
	 * @throws NullPointerException
	 *             if arguments contain nulls
	 */
	public static boolean checkSingleFileExistanceAndExtension(String path, String[] extensions) {
		if (path != null) {
			if (path.isEmpty()) {
				throw new IllegalArgumentException("File path can't be empty");
			}
		} else {
			throw new NullPointerException("File path can't be null");
		}
		if (extensions != null) {
			for (String extension : extensions) {
				if (extension != null) {
					if (extension.isEmpty()) {
						throw new IllegalArgumentException("Extension can't be empty");
					}
				} else {
					throw new NullPointerException("Extension can't be null");
				}
			}
		} else {
			throw new NullPointerException("Array of extensions can't be null");
		}

		// set initial value
		boolean result = checkifSingleFileExists(path);

		if (!checkFileExtension(path, extensions)) {
			result = false;
			logger.debug("This file has wrong extension (must be: " + Arrays.toString(extensions) + "): " + path);
		}
		return result;
	}

	/**
	 * Checks if single file exists.
	 *
	 * @param path
	 *            the path of file
	 * @return true, if file exists and is not a directory
	 * @throws IllegalArgumentException
	 *             if file path is empty
	 * @throws NullPointerException
	 *             if file path is null
	 */
	public static boolean checkifSingleFileExists(String path) {
		if (path != null) {
			if (path.isEmpty()) {
				throw new IllegalArgumentException("File path can't be empty");
			}
		} else {
			throw new NullPointerException("File path can't be null");
		}
		File file = new File(path);
		boolean result = true;
		if (file.exists()) {
			if (file.isDirectory()) {
				result = false;
				logger.debug("This file is not a file, but directory: " + path);
			}
		} else {
			result = false;
			logger.debug("This file does not exist: " + path);
		}
		return result;
	}

	/**
	 * The method for simple file copying, replaces existing file.
	 *
	 * @param source
	 *            the path of file to copy
	 * @param destination
	 *            the path to where the file will be copied
	 * @throws IOException when can't copy file
	 */
	public static void copyFileSimple(File source, File destination) throws IOException {
		try {
			Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
			logger.info("CopyFile. Source: " + source + " Destination: " + destination);
		} catch (IOException e) {
			String error = "Can't copy file from source: " + source + " to destination: " + destination;
			logger.error(error);
			throw new IOException(error);
		}
	}

	/**
	 * The Map needed for safe method {@link #copyFileSynced(File, File)} to work. <br>
	 * It contains file copy destination paths and associated simple objects. <br>
	 * These simple objects are used as mutexes, not allowing concurrent file copy to the same destination.
	 */
	private static Map<String, Object> syncMap = new HashMap<String, Object>();

	/**
	 * The method for synchronized file copying. It's thread-safe and not allows concurrent file copy to the same destination. ;)
	 *
	 * @param source
	 *            the path of file to copy
	 * @param destination
	 *            the path to where the file will be copied
	 * @throws IOException 
	 */
	public static void copyFileSynced(File source, File destination) throws IOException {
		String dstPath = destination.getAbsolutePath();
		Object sync;
		synchronized (syncMap) {
			// *Sigh* We need to deal with possible memory leak.
			// Though the leak can happen only when the user is a really hardcore streamer. I mean REALLY.
			if (syncMap.size() > 4096) {
				syncMap.clear();
			}
			sync = syncMap.get(dstPath);
			if (sync == null) {
				sync = new Object();
				syncMap.put(dstPath, sync);
			}
		}
		synchronized (sync) {
			copyFileSimple(source, destination);
		}
	}

	/**
	 * The method for file copying. If copying fails, it tries again after some time. <br>
	 * But it stops when catches {@link IOException}
	 * 
	 * @param source
	 *            the in
	 * @param destination
	 *            the out
	 */
	@Deprecated
	public static void copyFileRepeatOnException(File source, File destination) {
		boolean repeat = false;
		do {
			try {
				Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
				logger.info("CopyFile. Source: " + source + " Destination: " + destination);
				repeat = false;
			} catch (FileSystemException e) {
				// Empty
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// Do nothing
				}
				repeat = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				repeat = false;
			}
		} while (repeat);
	}

	/**
	 * Find files in directory.
	 *
	 * @param path
	 *            the path to directory where to find files
	 * @param extensions
	 *            the list of allowed extensions in ".extension" format
	 * @return the array of found files <br>
	 *         is empty if directory don't have files <br>
	 *         null if directory is not valid
	 * @throws IllegalArgumentException
	 *             if file path or one of extensions is empty
	 * @throws NullPointerException
	 *             if arguments contain nulls
	 */
	public static File[] findFilesInDirectory(String path, String[] extensions) {
		if (path != null) {
			if (path.isEmpty()) {
				throw new IllegalArgumentException("File path can't be empty");
			}
		} else {
			throw new NullPointerException("File path can't be null");
		}
		if (extensions != null) {
			for (String extension : extensions) {
				if (extension != null) {
					if (extension.isEmpty()) {
						throw new IllegalArgumentException("Extension can't be empty");
					}
				} else {
					throw new NullPointerException("Extension can't be null");
				}
			}
		} else {
			throw new NullPointerException("Array of extensions can't be null");
		}
		File[] fileList = null;
		FilenameFilter imageFilter = new FilenameFilter() {
			public boolean accept(File file, String fileName) {
				return checkFileExtension(fileName, extensions);
			}
		};
		File directoryFile = new File(path);
		fileList = directoryFile.listFiles(imageFilter);
		if (fileList == null) {
			logger.debug("This Directory is not valid: " + path);
		} else if (fileList.length == 0) {
			logger.debug("This Directory does not contain files with specified extensions: " + path);
		}
		return fileList;
	}

	/**
	 * Puts the single item inside newly created {@link ArrayList} and returns this list.
	 *
	 * @param <T>
	 *            the generic type of item
	 * @param item
	 *            the item
	 * @return {@link ArrayList} with single item inside
	 */
	public static <T> ArrayList<T> singleItemAsList(T item) {
		ArrayList<T> list = new ArrayList<T>();
		list.add(item);
		return list;
	}

	/**
	 * Puts the single string inside newly created Array of Strings and returns this Array.
	 *
	 * @param item
	 *            String
	 * @return Array of Strings with single string inside
	 */
	public static String[] singleStringAsArrayOfStrings(String item) {
		String[] array = new String[1];
		array[0] = item;
		return array;
	}

	/**
	 * Creates a file with directories at the given path if it's not exists already. <br>
	 * If something goes wrong, returns false.
	 *
	 * @param filePath
	 *            String the path of file that needs to exist
	 * @return True if all went okay, False if something went wrong.
	 */
	public static boolean createFileAndDirectoriesAtPath(String filePath) {
		boolean success = true;
		File file = new File(filePath);
		boolean needToCreate = false;
		if (!file.exists()) {
			needToCreate = true;
		} else {
			if (file.isDirectory()) {
				needToCreate = true;
			}
		}
		if (needToCreate) {
			createDirectoriesAtPath(file.getParent());
			logger.info("Creating new file: " + filePath);
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.error("Can't create new file for some reason");
				success = false;
			}
		}
		return success;
	}

	/**
	 * Creates a directory structure for the given path if it's not exists already. <br>
	 * If something goes wrong, returns false.
	 *
	 * @param path
	 *            String the path representing a directory structure that needs to exist
	 * @return True if all went okay, False if something went wrong.
	 */
	public static boolean createDirectoriesAtPath(String path) {
		boolean success = true;
		File toCreate = new File(path);
		boolean needToCreate = false;
		if (!toCreate.exists()) {
			needToCreate = true;
		} else {
			if (toCreate.isFile()) {
				needToCreate = true;
			}
		}
		if (needToCreate) {
			logger.info("Creating new directory: " + toCreate.getPath());
			boolean createdDirs = toCreate.mkdirs();
			if (!createdDirs) {
				logger.error("Can't create new directory for some reason");
				success = false;
			}
		}
		return success;
	}
	
	/**
	 * Receives two objects. If the first object is null, returns the second object. If not, returns
	 * the first object.
	 *
	 * @param <T>
	 *            The generic type
	 * @param normalValue
	 *            The object to return if it's not null.
	 * @param valueIfNull
	 *            The object to return is another argument is null. Can't be null.
	 * @return One of the arguments based on rules above.
	 * 
	 * @throws IllegalArgumentException
	 * 			  If the second argument is null.
	 */
	public static <T> T ifNullReturnOther(T normalValue, T valueIfNull) {
		if (valueIfNull == null) {
			throw new IllegalArgumentException("There's no point in passing null second argument");
		}
		if (normalValue == null) {
			return valueIfNull;
		}
		return normalValue;
	}

}
