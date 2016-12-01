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

import com.ubershy.streamsis.CuteConfig;
import com.ubershy.streamsis.networking.responses.GetSceneNameResponse;
import com.ubershy.streamsis.networking.responses.Response;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

public class StreamingProgramManager {

	static final Logger logger = LoggerFactory.getLogger(StreamingProgramManager.class);
	
	/** The connection maintainer of the current {@link TypicalClient} instance. */
	private static ConnectionMaintainer connectionMaintainer;
	
	/** Indicates that {@link StreamingProgramManager} was initialized. */
	private static boolean started = false;

	/**
	 * Indicates that {@link StreamingProgramManager} was stopped forever, probably on program exit.
	 */
	private static boolean stoppedForever = false;
	
	/** The current type of the {@link TypicalClient} instance loaded. */
	private static ReadOnlyObjectWrapper<ClientType> clientType = new ReadOnlyObjectWrapper<ClientType>(
			ClientType.NONE);
	public static ReadOnlyObjectProperty<ClientType> clientTypeProperty() {
		return clientType.getReadOnlyProperty();
	}
	public static ClientType getClientType() {return clientType.get();}
	public static void setClientType(ClientType type) {setClientTypeAndRemember(type);};
	
	/** The current connection status of the {@link TypicalClient} instance loaded. */
	private static ReadOnlyObjectWrapper<ConnectionStatus> status = new ReadOnlyObjectWrapper<ConnectionStatus>(
			ConnectionStatus.OFFLINE);
	public static ReadOnlyObjectProperty<ConnectionStatus> statusProperty() {
		return status.getReadOnlyProperty();
	}
	public static ConnectionStatus getStatus() {return status.get();}
	
	/**
	 * Starts and initializes {@link StreamingProgramManager}. Immediately tries to connect and
	 * maintain connection to the Streaming Program which is specified in the configuration file.
	 * 
	 * @throws UnsupportedOperationException
	 *             If was started already before. Or was stopped before.
	 */
	public static void start() {
		logger.info("Initializing Streaming Program Manager in a separate thread...");
		if (started) {
			throw new UnsupportedOperationException("Can't start because it's already started");
		}
		started = true;
		if (stoppedForever) {
			throw new UnsupportedOperationException(
					"Can't start again after it was forever stopped");
		}
		NetUtil.startInNewThread(() -> {
			setClientTypeAndRemember(getClientTypeFromConfig());
		});
		logger.info("Streaming Program Manager is on.");
	}

	/**
	 * Stops {@link StreamingProgramManager}. Immediately disconnects from the Streaming Program.
	 * 
	 * @throws UnsupportedOperationException
	 *             If was stopped alredy before.
	 */
	public static void stopForever() {
		throwExceptionIfNotStarted();
		if (stoppedForever) {
			throw new UnsupportedOperationException(
					"Can't stop again after it was forever stopped");
		}
		stoppedForever = true;
		logger.info("Stopping Streaming Program Manager forever...");
		connectionMaintainer.stopMaintainingConnection();
		logger.info("Streaming Program Manager is stopped forever.");
	}

	private static void throwExceptionIfNotStarted() {
		if (!started) {
			throw new UnsupportedOperationException(
					"The operation is unsupported because Streaming Program Manager wasn't started");
		}
	}

	private static void setClientTypeAndRemember(ClientType type) {
		throwExceptionIfNotStarted();
		if (type == null) {
			throw new IllegalArgumentException("Can't set an empty client type.");
		}
		logger.info("Now setting client type to: '" + type.toString() + "'");
		boolean needNewInstance = true;
		if (connectionMaintainer != null) {
			// Stop existing client.
			connectionMaintainer.stopMaintainingConnection();
			if (type.equals(clientType.get())) {
				// If type is the same, instantiation of the new client is not needed.
				needNewInstance = false;
			}
		} 
		if (needNewInstance) {
			clientType.set(type);
			TypicalClient newClient;
			try {
				newClient = type.instantiate();
				status.bind(newClient.statusProperty());
			} catch (Exception e) {
				throw new RuntimeException(
						"An error occured during instantiation of client type: "
								+ type.toString(),
						e);
			}
			connectionMaintainer = new ConnectionMaintainer(newClient);
		}
		connectionMaintainer.maintainConnection();
		// If everything went alright, let's save the client type to config.
		CuteConfig.setString(CuteConfig.CUTE, "StreamingProgramName", type.name());
	}
	
	private static ClientType getClientTypeFromConfig() {
		String name = CuteConfig.getString(CuteConfig.CUTE, "StreamingProgramName");
		ClientType clientType;
		try {
			clientType = ClientType.valueOf(name);
		} catch (IllegalArgumentException e) { 
			// If there's wrong value in config, let's reset it to default.
			name = CuteConfig.getStringDefault(CuteConfig.CUTE, "StreamingProgramName");
			CuteConfig.setString(CuteConfig.CUTE, "StreamingProgramName", name);
			clientType = ClientType.valueOf(name);
		}
		return clientType;
	}

	/**
	 * Gets the current scene name in the Streaming Program in response.
	 *
	 * @return The current scene name in the Streaming Program in response.
	 */
	public static GetSceneNameResponse getSceneName() {
		throwExceptionIfNotStarted();
		return connectionMaintainer.getClient().getSceneName();
	}

	/**
	 * Sets the current scene in the Streaming Program by name.
	 *
	 * @return The name of the scene to make current in the Streaming Program.
	 */
	public static Response setSceneName(String name) {
		throwExceptionIfNotStarted();
		return connectionMaintainer.getClient().setSceneName(name);
	}
	
}
