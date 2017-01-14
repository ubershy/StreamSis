package com.ubershy.streamsis.networking.clients;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubershy.streamsis.networking.responses.Response;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

import com.ubershy.streamsis.CuteConfig;
import com.ubershy.streamsis.networking.ConnectionStatus;
import com.ubershy.streamsis.networking.CuteDecoder;
import com.ubershy.streamsis.networking.NetUtil;
import com.ubershy.streamsis.networking.responses.GetSceneNameResponse;

/**
 * OBSStudioClient allows to connect to OBS Studio that has websocket plugin turned on.
 */
@ClientEndpoint(decoders = CuteDecoder.class)
public class OBSStudioClient implements TypicalClient {

	private interface RequestCallback {
		public void processResponse(JsonObject response, String errorMessage);
	}

	static final Logger logger = LoggerFactory.getLogger(OBSStudioClient.class);

	private static final JsonBuilderFactory factory = Json.createBuilderFactory(null);

	private static final String SERVERURI = "ws://127.0.0.1:4444/";

	private static final String PASSCONFIGSUBKEY = "OBSSTUDIOPASS";

	private final Map<String, RequestCallback> requestCallbacks = new ConcurrentHashMap<>();

	private Session session;

	private static final long RESPONSETIMEOUT = 3000;

	private ReadOnlyObjectWrapper<ConnectionStatus> status = new ReadOnlyObjectWrapper<ConnectionStatus>(
			ConnectionStatus.OFFLINE);

	public ReadOnlyObjectProperty<ConnectionStatus> statusProperty() {
		return status.getReadOnlyProperty();
	}
	
	@Override
	public void connect() {
		logger.info("Connecting...");
		status.set(ConnectionStatus.CONNECTING);
		try {
			WebSocketContainer container = ContainerProvider.getWebSocketContainer();
			container.connectToServer(this, new URI(SERVERURI));
		} catch (Exception e) {
			// Doing nothing, onError() will handle the situation.
		}
	}

	@Override
	public void disconnect() {
		try {
			logger.info("Disconnecting on will...");
			if (session == null) {
				logger.info("No need to disconnect as there is no active session.");
			} else {
				session.close();
				logger.info("Disconnected on will.");
			}
		} catch (IOException e) {
			logger.error("Error occured while disconnecting on will.", e);
		}
	}

	@Override
	public GetSceneNameResponse getSceneName() {
		Response genericResponse = sendRequestGetResponse("GetCurrentScene", null);
		String sceneName = null;
		if (genericResponse.getErrorText() == null) {
			sceneName = genericResponse.getRawData().getString("name");
		}
		return new GetSceneNameResponse(genericResponse.getErrorText(),
				genericResponse.getRawData(), sceneName);
	}

	@Override
	public Response setSceneName(String name) {
		return sendRequestGetResponse("SetCurrentScene",
				factory.createObjectBuilder().add("scene-name", name).build());
	}

	@OnOpen
	public void onOpen(Session session) throws IOException {
		this.session = session;
		session.setMaxIdleTimeout(0);
		logger.info("Connected.");
		status.set(ConnectionStatus.ONLINE);
		NetUtil.startInNewThread(() -> {
			authenticateIfNeeded();
		});
	}

	@OnMessage
	public void onMessage(JsonObject json) {
		if (json.getString("update-type", null) != null) {
			logger.debug("Received 'update-type' message. Doing nothing.");
			return;
		}
		// If it's not "update-type" event, but a response, lets process it.
		String errorMessage = getErrorFromRawResponse(json);
		String id = json.getString("message-id", null);
		if (id != null) {
			NetUtil.startInNewThread(() -> {
				logger.debug("Received response with ID: " + id);
				RequestCallback callback = requestCallbacks.remove(id);
				if (callback != null)
					callback.processResponse(json, errorMessage);
				else
					logger.error(
							"Callback with such ID is unknown: " + id + ". Doing nothing. o_O");
			});
		} else {
			logger.error("Wrong response received, it doesn't have 'message-id' field.");
		}
	}

	@OnClose
	public void onClose(Session userSession, CloseReason reason) {
		this.session = null;
		logger.info("Disconnected with reason: " + reason.getReasonPhrase());
		status.set(ConnectionStatus.OFFLINE);
	}

	@OnError
	public void onError(Session session, Throwable thr) {
		if (thr instanceof ConnectException) {
			logger.error("Connection failed.");
			status.set(ConnectionStatus.CONNECTIONERROR);
			return;
		}
		logger.error("Unknown error during connection occurred.", thr);
	}

	private void authenticateIfNeeded() {
		Response authNeededResponse = sendRequestGetResponse("GetAuthRequired", null);
		boolean authRequired = true;
		if (authNeededResponse.getErrorText() != null) {
			logger.error("Can't figure out if authentication is needed, got error response: \""
					+ authNeededResponse.getErrorText() + "\".");
			status.set(ConnectionStatus.ERROR);
			return;
		}
		authRequired = authNeededResponse.getRawData().getBoolean("authRequired");
		if (!authRequired) {
			logger.info("No authentication is needed. Ready to work.");
			status.set(ConnectionStatus.ONLINE);
			return;
		}
		logger.info("Authentication is required. Authenticating...");
		status.set(ConnectionStatus.AUTHENTICATING);

		String password = CuteConfig.getString(CuteConfig.NETWORKING, PASSCONFIGSUBKEY);
		String challenge = authNeededResponse.getRawData().getString("challenge");
		String salt = authNeededResponse.getRawData().getString("salt");

		String secretString = password + salt;
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			logger.error("Can't get hash function needed for authentication", e);
			return;
		}
		byte[] secretHash = md.digest(secretString.getBytes());
		String secret = Base64.getEncoder().encodeToString(secretHash);

		String authString = secret + challenge;
		byte[] authHash = md.digest(authString.getBytes());
		String auth = Base64.getEncoder().encodeToString(authHash);

		Response authResultResponse = sendRequestGetResponse("Authenticate",
				factory.createObjectBuilder().add("auth", auth).build());
		if (authResultResponse.getErrorText() == null) {
			logger.info("Successfully authenticated! ♥ Ready to work.");
			status.set(ConnectionStatus.ONLINE);
		} else {
			logger.error("Access denied. Maybe wrong password? Got this error response: \""
					+ authResultResponse.getErrorText() + "\".");
			status.set(ConnectionStatus.AUTHENTICATIONFAIL);
		}
	}

	private synchronized boolean sendRequest(String requestType, JsonObject optionalRequestSpecificFields,
			UUID messageID) {
		// Create request builder with common fields.
		JsonObjectBuilder requestBuilder = factory.createObjectBuilder()
				.add("request-type", requestType).add("message-id", messageID.toString());
		// Add request-specific fields to builder if they exist.
		if (optionalRequestSpecificFields != null) {
			optionalRequestSpecificFields.forEach((field, value) -> {
				requestBuilder.add(field, value);
			});
		}
		// Finally build request and serialize.
		String serializedRequest = requestBuilder.build().toString();
		// Send the request.
		logger.info("Sending request: " + serializedRequest);
		try {
			session.getBasicRemote().sendText(serializedRequest);
			logger.debug("Sent request with ID: " + messageID.toString());
		} catch (IOException e) {
			logger.error("Can't send the request.", e);
			return false;
		}
		return true;
	}

	private Response sendRequestGetResponse(String requestType,
			JsonObject optionalRequestSpecificFields) {
		if (session == null || !session.isOpen()) {
			return NetUtil.buildErrorResponseAndLog("Can't send request as there is no connection.",
					null);
		}
		UUID messageID = UUID.randomUUID();
		CountDownLatch responseReceivedLatch = new CountDownLatch(1);
		AtomicReference<JsonObject> atomicRawResponse = new AtomicReference<>(null);
		AtomicReference<String> atomicErrorMessage = new AtomicReference<>(null);
		// Create callback
		RequestCallback callback = (response, errorMessage) -> {
			atomicRawResponse.set(response);
			atomicErrorMessage.set(errorMessage);
			responseReceivedLatch.countDown();
		};
		// Register callback with ID
		requestCallbacks.put(messageID.toString(), callback);
		try {
			boolean sendingSuccess = sendRequest(requestType, optionalRequestSpecificFields,
					messageID);
			if (!sendingSuccess) {
				// There is an error in fake response, that means the request wasn't even sent.
				requestCallbacks.remove(messageID);
				return NetUtil.buildErrorResponseAndLog("Can't send request for unknown reason.",
						null);
			}
			boolean responseReceivedInTime = responseReceivedLatch.await(RESPONSETIMEOUT,
					TimeUnit.MILLISECONDS);
			if (!responseReceivedInTime) {
				return NetUtil.buildErrorResponseAndLog("Timeout exceeded, no response.", null);
			}
			String errorMessage = atomicErrorMessage.get();
			if ("Not Authenticated".equals(errorMessage)) {
				status.set(ConnectionStatus.AUTHENTICATIONFAIL);
			}
			return new Response(errorMessage, atomicRawResponse.get());
		} catch (InterruptedException e) {
			requestCallbacks.remove(messageID);
			return NetUtil.buildErrorResponseAndLog("Response waiting was interrupted.", e);
		}
	}

	private String getErrorFromRawResponse(JsonObject response) {
		String status = response.getString("status", null);
		if (status == null) {
			String errorToShow = "Wrong response received, it doesn't have 'status' field.";
			logger.error(errorToShow);
			return errorToShow;
		}
		boolean errorExists = status.equals("error");
		if (errorExists) {
			String errorText = response.getString("error");
			logger.error("Error response received: " + errorText);
			return errorText;
		} else {
			return null;
		}
	}

}
