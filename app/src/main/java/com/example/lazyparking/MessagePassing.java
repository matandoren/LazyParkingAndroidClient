package com.example.lazyparking;

import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import common.Reply;
import common.ReplyMessage;
import common.RequestMessage;
import common.RequestType;


public class MessagePassing implements Runnable {
	/*
	 * The classes that are common to both the server and the client:
	 * RequestMessage, ReplyMessage, RequestType (Enum), Reply (Enum)
	 * are found in the common package
	 */
	private Socket socket;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	private boolean stopRunning;
	private BlockingQueue<ReplyMessage> loginReplies;
	private BlockingQueue<ReplyMessage> cancelReservationReplies;
	private BlockingQueue<ReplyMessage> changePasswordReplies;
	private BlockingQueue<ReplyMessage> deleteDriverReplies;
	private BlockingQueue<ReplyMessage> addDriverReplies;
	private BlockingQueue<ReplyMessage> updateDriverExpirationReplies;
	private BlockingQueue<ReplyMessage> reserveParkingSpotReplies;
	private BlockingQueue<ReplyMessage> requestForExpiredUsersReplies;


	public MessagePassing(String serverIp, int serverPort) throws IOException {
		try {
			socket = new Socket(serverIp, serverPort);
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());

			loginReplies = new LinkedBlockingQueue<>();
			cancelReservationReplies = new LinkedBlockingQueue<>();
			changePasswordReplies = new LinkedBlockingQueue<>();
			deleteDriverReplies = new LinkedBlockingQueue<>();
			addDriverReplies = new LinkedBlockingQueue<>();
			updateDriverExpirationReplies = new LinkedBlockingQueue<>();
			reserveParkingSpotReplies = new LinkedBlockingQueue<>();
			requestForExpiredUsersReplies = new LinkedBlockingQueue<>();

			(new Thread(this)).start();
		} catch (IOException e) {
			if (socket != null)
				socket.close();
			throw e;
		}
	}

	@Override
	public void run() {
		while (!stopRunning) {
			try {
				ReplyMessage msg = (ReplyMessage) inputStream.readObject();

				if (msg.type == RequestType.LOGOUT) {
					stopRunning = true;
				} else if (msg.type == RequestType.GET_PARKING_STATUS) {
					if (msg.reply == Reply.SUCCESS)
						if (MainActivity.activeUserActivity != null)
							MainActivity.activeUserActivity.runOnUiThread(()->{
								MainActivity.activeUserActivity.setParkingSpot(msg.intField, msg.boolField1, msg.boolField2, msg.stringField);
							});
				} else {
					queueReply(msg);
				}
			} catch (Exception e) {
				stopRunning = true;
				displayError(e.getMessage());
			}
		}
		try {
			socket.close();
		} catch (IOException e) {
			displayError(e.getMessage());
		}
	}

	public void sendLoginRequest(String username, String password) {
		RequestMessage request = new RequestMessage();
		request.type = RequestType.LOGIN;
		request.stringField1 = username;
		request.stringField2 = password;
		try {
			outputStream.writeObject(request);
		} catch (IOException e) {
			displayError(e.getMessage());
		}
	}

	public void sendGetParkingStatusRequest(int parkingSpotId) {
		RequestMessage request = new RequestMessage();
		request.type = RequestType.GET_PARKING_STATUS;
		request.intField = parkingSpotId;
		try {
			outputStream.writeObject(request);
		} catch (IOException e) {
			displayError(e.getMessage());
		}
	}

	public void sendLogoutRequest() {
		RequestMessage request = new RequestMessage();
		request.type = RequestType.LOGOUT;
		try {
			outputStream.writeObject(request);
		} catch (IOException e) {
			displayError(e.getMessage());
		}
	}

	public void sendCancelReservationRequest(int parkingSpotId) {
		RequestMessage request = new RequestMessage();
		request.type = RequestType.CANCEL_RESERVATION;
		request.intField = parkingSpotId;
		try {
			outputStream.writeObject(request);
		} catch (IOException e) {
			displayError(e.getMessage());
		}
	}

	public void sendChangePasswordRequest(String username, String password) {
		RequestMessage request = new RequestMessage();
		request.type = RequestType.CHANGE_PW;
		request.stringField1 = username;
		request.stringField2 = password;
		try {
			outputStream.writeObject(request);
		} catch (IOException e) {
			displayError(e.getMessage());
		}
	}

	public void sendDeleteDriverRequest(String username) {
		RequestMessage request = new RequestMessage();
		request.type = RequestType.REMOVE_DRIVER;
		request.stringField1 = username;
		try {
			outputStream.writeObject(request);
		} catch (IOException e) {
			displayError(e.getMessage());
		}
	}

	public void sendAddDriverRequest(String name, String passwordStr, Date expirationDate) {
		RequestMessage request = new RequestMessage();
		request.type = RequestType.ADD_DRIVER;
		request.stringField1 = name;
		request.stringField2 = passwordStr;
		request.dateField = expirationDate;
		try {
			outputStream.writeObject(request);
		} catch (IOException e) {
			displayError(e.getMessage());
		}
	}

	public void sendUpdateDriverExpirationRequest(String username, Date date) {
		RequestMessage request = new RequestMessage();
		request.type = RequestType.CHANGE_EXPIRATION;
		request.stringField1 = username;
		request.dateField = date;
		try {
			outputStream.writeObject(request);
		} catch (IOException e) {
			displayError(e.getMessage());
		}
	}

	public void sendReserveParkingSpotRequest(int parkingSpotID, String reservedFor, Date expirationDate) {
		RequestMessage request = new RequestMessage();
		request.type = RequestType.RESERVE_PARKING_SPOT;
		request.intField = parkingSpotID;
		request.stringField1 = reservedFor;
		request.dateField = expirationDate;
		try {
			outputStream.writeObject(request);
		} catch (IOException e) {
			displayError(e.getMessage());
		}
	}

	public void sendRequestForExpiredUsersRequest() {
		RequestMessage request = new RequestMessage();
		request.type = RequestType.GET_NEXT_EXPIRED;
		try {
			outputStream.writeObject(request);
		} catch (IOException e) {
			displayError(e.getMessage());
		}
	}

	public ReplyMessage getNextLoginReply() {
		try {
			return loginReplies.take();
		} catch (InterruptedException e) {
			displayError(e.getMessage());
			return null;
		}
	}

	public ReplyMessage getNextReserveParkingSpotReply() {
		try {
			return reserveParkingSpotReplies.take();
		} catch (InterruptedException e) {
			displayError(e.getMessage());
			return null;
		}
	}

	public ReplyMessage getNextChangePasswordReply() {
		try {
			return changePasswordReplies.take();
		} catch (InterruptedException e) {
			displayError(e.getMessage());
			return null;
		}
	}

	public ReplyMessage getNextAddDriverReply() {
		try {
			return addDriverReplies.take();
		} catch (InterruptedException e) {
			displayError(e.getMessage());
			return null;
		}
	}

	public ReplyMessage getNextCancelReservationReply() {
		try {
			return cancelReservationReplies.take();
		} catch (InterruptedException e) {
			displayError(e.getMessage());
			return null;
		}
	}

	public ReplyMessage getNextUpdateDriverExpirationReply() {
		try {
			return updateDriverExpirationReplies.take();
		} catch (InterruptedException e) {
			displayError(e.getMessage());
			return null;
		}
	}

	public ReplyMessage getNextDeleteDriverReply() {
		try {
			return deleteDriverReplies.take();
		} catch (InterruptedException e) {
			displayError(e.getMessage());
			return null;
		}
	}

	public ReplyMessage getNextRequestForExpiredUsersReply() {
		try {
			return requestForExpiredUsersReplies.take();
		} catch (InterruptedException e) {
			displayError(e.getMessage());
			return null;
		}
	}

	private void queueReply(ReplyMessage reply) {
		try {
			switch (reply.type) {
				case LOGIN:
					loginReplies.put(reply);
					break;
				case CHANGE_PW:
					changePasswordReplies.put(reply);
					break;
				case RESERVE_PARKING_SPOT:
					reserveParkingSpotReplies.put(reply);
					break;
				case ADD_DRIVER:
					addDriverReplies.put(reply);
					break;
				case CANCEL_RESERVATION:
					cancelReservationReplies.put(reply);
					break;
				case CHANGE_EXPIRATION:
					updateDriverExpirationReplies.put(reply);
					break;
				case REMOVE_DRIVER:
					deleteDriverReplies.put(reply);
					break;
				case GET_NEXT_EXPIRED:
					requestForExpiredUsersReplies.put(reply);
					break;
			}
		} catch (InterruptedException e) {
			displayError(e.getMessage());
		}
	}

	private void displayError(String errorMsg) {
		if (MainActivity.activeUserActivity != null)
			MainActivity.activeUserActivity.runOnUiThread(()->{
				MainActivity.activeMainActivity.playMusic.StarSound();
				Toast.makeText(MainActivity.activeUserActivity, errorMsg, Toast.LENGTH_LONG).show();
			});
		else if (MainActivity.activeMainActivity != null)
			MainActivity.activeMainActivity.runOnUiThread(()->{
				MainActivity.activeMainActivity.playMusic.StarSound();
				Toast.makeText(MainActivity.activeMainActivity, errorMsg, Toast.LENGTH_LONG).show();
			});
	}

}
