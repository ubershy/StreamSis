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

import com.ubershy.streamsis.networking.clients.TypicalClient;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;

/**
 * ConnectionMaintainer maintains connections to Streaming Programs. Reconnects in case of trouble.
 */
public class ConnectionMaintainer {

	static final Logger logger = LoggerFactory.getLogger(ConnectionMaintainer.class);
	
	/** The client which connection to Streaming Program {@link ConnectionMaintainer} maintains. */
	private final TypicalClient client;
	private boolean reconnectFailedBefore = false;
	private Thread reconnectionWaitingThread;
	private Thread disconnectOnErrorThread;
	private static final long RECONNECTDELAY = 2000;
	private static final long ERRORDISCONNECTDELAY = 5000;

	InvalidationListener statusListener = (o) -> {
		@SuppressWarnings("unchecked")
		ConnectionStatus currentStatus = ((ReadOnlyObjectProperty<ConnectionStatus>) o).getValue();
		logger.info("Current status of the networking client is: '" + currentStatus + "'.");
		switch (currentStatus) {
		case AUTHENTICATING:
			break;
		case AUTHENTICATIONFAIL:
			// Scheduling disconnect for the user to see the status. After disconnect, there will be
			// reconnect.
			if (disconnectOnErrorThread == null) // If it's not already scheduled.
				scheduleDisconnectBecauseOfError();
			break;
		case CONNECTING:
			break;
		case CONNECTIONERROR: 
			reconnect();
			break;
		case ERROR:
			// Scheduling disconnect for the user to see the status. After disconnect, there will be
			// reconnect.
			if (disconnectOnErrorThread == null) // If it's not already scheduled.
				scheduleDisconnectBecauseOfError();
			break;
		case OFFLINE:
			cancelScheduledDisconnect();
			reconnect();
			break;
		case ONLINE:
			cancelScheduledDisconnect();
			reconnectFailedBefore = false;
			break;
		default:
			throw new RuntimeException("What is this status?");
		}
	};

	/**
	 * Instantiates new {@link ConnectionMaintainer}.
	 *
	 * @param client
	 *            The client which connection to Streaming Program should be maintained.
	 * @throws IllegalArgumentException If the client is null.
	 */
	public ConnectionMaintainer(TypicalClient client) {
		if (client == null) {
			throw new IllegalArgumentException("Client can't be null");
		}
		this.client = client;
	}
	
	/**
	 * Gets the client which connection to Streaming Program {@link ConnectionMaintainer} maintains.
	 *
	 * @return The client which connection to Streaming Program {@link ConnectionMaintainer}
	 *         maintains.
	 */
	public TypicalClient getClient() {
		return client;
	}

	/**
	 * Starts to maintain connection from the {@link #client} to the Streaming Program. Tries to
	 * connect to Streaming Program immediately.
	 */
	public void maintainConnection() {
		logger.info("Maintaining connection.");
		client.statusProperty().addListener(statusListener);
		client.connect();
	}

	/**
	 * Stops maintaining connection from the {@link #client} to the Streaming Program.
	 */
	public void stopMaintainingConnection() {
		logger.info("Stopping maintaining connection.");
		client.statusProperty().removeListener(statusListener);
		client.disconnect();
		cancelScheduledReconnect();
	}

	private void cancelScheduledReconnect() {
		if (reconnectionWaitingThread != null && reconnectionWaitingThread.isAlive()) {
			reconnectionWaitingThread.interrupt();
			reconnectionWaitingThread = null;
		}
	}
	
	private void cancelScheduledDisconnect() {
		if (disconnectOnErrorThread != null && disconnectOnErrorThread.isAlive()) {
			disconnectOnErrorThread.interrupt();
			disconnectOnErrorThread = null;
		}
	}

	private void scheduleDisconnectBecauseOfError() {
		disconnectOnErrorThread = NetUtil.startInNewThread(() -> {
			logger.info(
					"Scheduling disconnect in " + ERRORDISCONNECTDELAY + " ms because of error...");
			try {
				Thread.sleep(ERRORDISCONNECTDELAY);
			} catch (InterruptedException e) {
				logger.info("Scheduled disconnect was cancelled.");
				return;
			}
			logger.info("Executing scheduled disconnect...");
			disconnectOnErrorThread = null;
			client.disconnect();
		});
	}

	private void reconnect() {
		boolean sleepWasInterrupted = false;
		if (reconnectFailedBefore) {
			logger.info("Scheduling reconnect in " + RECONNECTDELAY + " ms.");
			try {
				reconnectionWaitingThread = Thread.currentThread();
				Thread.sleep(RECONNECTDELAY);
			} catch (InterruptedException e) {
				logger.info("Scheduled reconnect was cancelled.");
				sleepWasInterrupted = true;
			}
		} else {
			logger.info("Reconnecting immediately...");
		}
		reconnectionWaitingThread = null;
		if (!sleepWasInterrupted) {
			reconnectFailedBefore = true;
			client.connect();
		}
	}

}
