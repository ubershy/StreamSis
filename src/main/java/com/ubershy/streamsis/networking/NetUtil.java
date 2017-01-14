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
package com.ubershy.streamsis.networking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.networking.responses.Response;

/**
 * NetUtil contains useful static stuff for networking.
 */
public class NetUtil {
	
	static final Logger logger = LoggerFactory.getLogger(NetUtil.class);

	/**
	 * Starts runnable in a new thread and returns this thread.
	 *
	 * @param r The runnable
	 * @return The thread.
	 */
	public static Thread startInNewThread(Runnable r) {
		Thread newThread = new Thread(r);
		newThread.start();
		return newThread;
	}

	/**
	 * Builds the error response and logs it.
	 *
	 * @param errorText The text describing the error.
	 * @param cause The Throwable, can be null.
	 * @return The {@link Response} instance with empty raw data, but with the error text.
	 * 
	 * @throws IllegalArgumentException If the error text is null or empty.
	 */
	public static Response buildErrorResponseAndLog(String errorText, Throwable cause) {
		if (errorText == null || errorText.isEmpty()) {
			throw new IllegalArgumentException("Error text can't be null or empty.");
		}
		if (cause != null)
			logger.error(errorText, cause);
		else
			logger.error(errorText);
		return new Response(errorText, null);
	}

}
