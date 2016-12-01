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

/**
 * The Enum with possible connection states for networking clients.
 */
public enum ConnectionStatus {
	/** Status that indicates that Streaming Program is connected and ready. */
	ONLINE("Online"),
	/** Status that indicates that Streaming Program is not connected. */
	OFFLINE("Offline"),
	/** Status that indicates that StreamSis is trying to connect to Streaming Program. */
	CONNECTING("Connecting..."),
	/** Status that indicates that StreamSis is trying to authenticate in Streaming Program. */
	AUTHENTICATING("Authenticating..."),
	/** Status that indicates that StreamSis is trying to authenticate in Streaming Program. */
	AUTHENTICATIONFAIL("Auth failed"),
	/** Status that indicates that StreamSis connection to Streaming Program failed. */
	CONNECTIONERROR("Connection error"),
	/** Status that indicates that error occurred while communicating with Streaming Program. */
	ERROR("Error");

	/** The user-friendly name of the status. */
	private final String name;

	/**
	 * Instantiates a new ConnectionStatus.
	 *
	 * @param name
	 *            The user-friendly name of the status.
	 */
	private ConnectionStatus(String name) {
		this.name = name;
	}

	/*
	 * Returns user-friendly name of this status.
	 */
	@Override
	public String toString() {
		return name;
	}
}