/** 
 * StreamSis
 * Copyright (C) 2016 Eva Balycheva
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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.gui.helperclasses.GUIUtil;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;

/**
 * SneakyExceptionHandler helps to nicely catch any cunning and devious exceptions. <p>
 * By default it shows JavaFX window with exception name, thread and stack trace. <br>
 * This behavior can be temporary overridden from any part of the program by using the static method 
 * {@link #setTemporaryUncaughtExceptionHandler(UncaughtExceptionHandler)}. It's useful when
 * exceptions are thrown by threads we can't control (for example, a tread that was started in a
 * library), but we want to handle such exceptions.
 */
public final class SneakyExceptionHandler  {
	
	/**
	 * The general exception handler which behavior can change depending on if
	 * {@link #temporaryExceptionHandler} is set.
	 */
	private static class AlteringExceptionHandler implements UncaughtExceptionHandler {
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			if (temporaryExceptionHandler == null) {
				defaultUncaughtException(t, e);
			} else {
				logger.error(e.getClass().getSimpleName() + " occured in " + t.getName() + " thread."
						+ " Using temporary uncaught exception handler...");
				temporaryExceptionHandler.uncaughtException(t, e);
			}
		}
	}
	
	/** The Constant logger. */
	static final Logger logger = LoggerFactory.getLogger(SneakyExceptionHandler.class);

	/** The temporary exception handler to use instead of default one if set. */
	private static UncaughtExceptionHandler temporaryExceptionHandler;
	
	private static AlteringExceptionHandler alteringExceptionHandler;
	
	private SneakyExceptionHandler() {
	}
	
	public static UncaughtExceptionHandler getSingleton() {
		if (alteringExceptionHandler == null) {
			alteringExceptionHandler = new AlteringExceptionHandler();
		}
		return alteringExceptionHandler;
	}

	
	/**
	 * Default uncaught exception handler. <br>
	 * Used if {@link #temporaryExceptionHandler} is not set by
	 * {@link #setTemporaryUncaughtExceptionHandler(UncaughtExceptionHandler)}.
	 *
	 * @param t
	 *            The thread.
	 * @param e
	 *            The exception.
	 */
	private static void defaultUncaughtException(Thread t, Throwable e) {
		// FIXME: Backup changed project near the current project's path without prompting the user.
		// Because, if prompted, the user might overwrite the original project file and there's no
		// guarantee that he will be able to load this project next time. 
		// If project is new and with unsaved changes, provide a button for the user to save it.
		logger.error("Catched unhandled Exception", e);
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.ERROR);
			GUIUtil.cutifyAlert(alert);
			alert.setTitle("Nobody expects the " + e.getClass().getSimpleName() + "!");
			alert.setHeaderText("Something bad happened to StreamSis and it stopped working");
			alert.setContentText("The problem occured in \"" + t.getName() + "\" thread.\n\n"
					+ "Click the \"Show details\" button to see the stack trace."
					+ " If you are not scared.");
			ByteArrayOutputStream ostream = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(ostream));
			TextArea stackTraceArea = new TextArea(ostream.toString());
			stackTraceArea.setEditable(false);
			stackTraceArea.setMinSize(800, 450);
			alert.getDialogPane().setExpandableContent(stackTraceArea);
			GUIUtil.showAlertInPrimaryStageCenter(alert);
			Platform.exit();
		});
	}

	/**
	 * Sets the temporary uncaught exception handler to use instead of default one when an unhandled
	 * exception occurs.
	 * <p>
	 * Warning 1: this method should be used in exceptional situations only, for example, when we
	 * are using some library method that creates a thread that might crash and wrapping such method
	 * in try-catch block doesn't work.
	 * <p>
	 * Warning 2: remove the temporary uncaught exception handler by
	 * {@link #removeTemporaryUncaughtExceptionHandler()} immediately after the problematic code has
	 * finished working.
	 *
	 * @param eh
	 *            The temporary uncaught exception handler.
	 */
	public static void setTemporaryUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
		if (temporaryExceptionHandler != null) {
			throw new RuntimeException("Temporary exception handler is already set."
					+ " Use removeTemporaryExceptionHandler() to remove the previous one first.");
		}
		temporaryExceptionHandler = eh;
	}

	/**
	 * Removes the temporary uncaught exception handler so the default one will be used when an
	 * unhandled exception occurs.
	 */
	public static void removeTemporaryUncaughtExceptionHandler() {
		if (temporaryExceptionHandler == null) {
			throw new RuntimeException("Temporary exception handler isn't set.");
		}
		temporaryExceptionHandler = null;
	}

}
