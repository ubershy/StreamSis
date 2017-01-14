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

import com.ubershy.streamsis.networking.clients.NoneClient;
import com.ubershy.streamsis.networking.clients.OBSStudioClient;
import com.ubershy.streamsis.networking.clients.TypicalClient;

/**
 * The Enum with possible clients that support {@link TypicalClient} interface.
 */
public enum ClientType {

	/** The client for OBS Studio program. */
	NONE("Networking off", NoneClient.class),

	/** The client for OBS Studio program. */
	OBSSTUDIO("OBS Studio", OBSStudioClient.class);

	/** The class of the client. */
	private final Class<? extends TypicalClient> clazz;

	/** The user-friendly name of the client. */
	private final String name;

	/**
	 * Instantiates a new client type.
	 *
	 * @param name The user-friendly name of the client.
	 * @param clazz The class of the client.
	 */
	private ClientType(String name, Class<? extends TypicalClient> clazz) {
		this.name = name;
		this.clazz = clazz;
	}

	/**
	 * Instantiate the client associated with enum value;
	 *
	 * @return the typical client
	 */
	public TypicalClient instantiate() {
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Can't instantiate the client '" + name + "'", e);
		}
	}

	/* 
	 * Returns the user-friendly name of this enum value.
	 */
	@Override
	public String toString() {
		return name;
	}

}