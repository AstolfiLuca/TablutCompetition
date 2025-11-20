package it.unibo.ai.didattica.competition.tablut.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;

import com.google.gson.Gson;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;
import it.unibo.ai.didattica.competition.tablut.util.Configuration;
import it.unibo.ai.didattica.competition.tablut.util.StreamUtils;

/**
 * Classe astratta di un client per il gioco Tablut
 * 
 * @author Andrea Piretti
 *
 */
public abstract class TablutClient implements Runnable {

	private State.Turn player;
	private String name;
	private Socket playerSocket;
	private int port;
	private DataInputStream in;
	private DataOutputStream out;
	private Gson gson;
	private State currentState;
	private int timeout;
	private String serverIp;

	public State.Turn getPlayer() {
		return player;
	}

	public void setPlayer(State.Turn player) {
		this.player = player;
	}

	public State getCurrentState() {
		return currentState;
	}

	public void setCurrentState(State currentState) {
		this.currentState = currentState;
	}

	/**
	 * Creates a new player initializing the sockets and the logger
	 * 
	 * @param player
	 *            The role of the player (black or white)
	 * @param name
	 *            The name of the player
	 * @param timeout
	 *            The timeout that will be taken into account (in seconds)
	 * @param ipAddress
	 *            The ipAddress of the server
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public TablutClient(String player, String name, int timeout, String ipAddress, int port)
			throws UnknownHostException, IOException {
		serverIp = ipAddress;
		this.timeout = timeout;
		this.gson = new Gson();
		if (player.toLowerCase().equals("white")) {
			this.player = State.Turn.WHITE;
		} else if (player.toLowerCase().equals("black")) {
			this.player = State.Turn.BLACK;
		} else {
			throw new InvalidParameterException("Player role must be BLACK or WHITE");
		}
		this.port = port;
		playerSocket = new Socket(serverIp, this.port);
		out = new DataOutputStream(playerSocket.getOutputStream());
		in = new DataInputStream(playerSocket.getInputStream());
		this.name = name;
	}

	public TablutClient(String player, String name, int timeout, String ipAddress) throws IOException {
		this(player, name, timeout, player.equalsIgnoreCase("black") ? Configuration.blackPort : Configuration.whitePort);
	}

	/**
	 * Creates a new player initializing the sockets and the logger. The server
	 * is supposed to be communicating on the same machine of this player.
	 * 
	 * @param player
	 *            The role of the player (black or white)
	 * @param name
	 *            The name of the player
	 * @param timeout
	 *            The timeout that will be taken into account (in seconds)
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public TablutClient(String player, String name, int timeout, int port) throws UnknownHostException, IOException {
		this(player, name, timeout, "localhost", port);
	}

	/**
	 * Creates a new player initializing the sockets and the logger. Timeout is
	 * set to be 60 seconds. The server is supposed to be communicating on the
	 * same machine of this player.
	 * 
	 * @param player
	 *            The role of the player (black or white)
	 * @param name
	 *            The name of the player
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public TablutClient(String player, String name, int port) throws UnknownHostException, IOException {
		this(player, name, 60, "localhost", port);
	}

	public TablutClient(String player, String name) throws UnknownHostException, IOException {
		this(player, name, 60, "localhost", player.equalsIgnoreCase("black") ? Configuration.blackPort : Configuration.whitePort);
	}

	/**
	 * Creates a new player initializing the sockets and the logger. Timeout is
	 * set to be 60 seconds.
	 * 
	 * @param player
	 *            The role of the player (black or white)
	 * @param name
	 *            The name of the player
	 * @param ipAddress
	 *            The ipAddress of the server
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public TablutClient(String player, String name, String ipAddress, int port) throws UnknownHostException, IOException {
		this(player, name, 60, ipAddress, port);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Write an action to the server
	 */
	public void write(Action action) throws IOException, ClassNotFoundException {
		StreamUtils.writeString(out, this.gson.toJson(action));
	}

	/**
	 * Write the name to the server
	 */
	public void declareName() throws IOException, ClassNotFoundException {
		StreamUtils.writeString(out, this.gson.toJson(this.name));
	}

	/**
	 * Read the state from the server
	 */
	public void read() throws ClassNotFoundException, IOException {
		this.currentState = this.gson.fromJson(StreamUtils.readString(in), StateTablut.class);
	}

	public int getTimeout() {
		return timeout;
	}
}
