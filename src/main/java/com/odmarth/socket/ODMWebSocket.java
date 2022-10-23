package com.odmarth.socket;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.odmarth.model.SocketRequestModel;
import com.odmarth.scanner.ScannerComponent;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import eu.gnome.morena.Device;

public class ODMWebSocket extends WebSocketServer{
	
	private final static int TCP_PORT=6987;
	private final static String LISTE_DEVICES="DEVICES";
	private final static String SCAN_DOC="SCAN_DOC";
	private Set<WebSocket> conns;
	public ODMWebSocket() throws UnknownHostException {
		super(new InetSocketAddress(TCP_PORT));
		conns = new HashSet<>();
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		conns.add(conn);
        System.out.println("New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
        
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		
		///conn.send();
		conns.remove(conn);
		
        System.out.println("Closed connection to " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		System.out.println("Message from client: " + message);
//        for (WebSocket sock : conns) {
//            sock.send(message);
//        }
		Gson gson = new Gson();
		SocketRequestModel model = gson.fromJson(message, SocketRequestModel.class);
		ScannerComponent component = new ScannerComponent();
		
		System.out.println("Model : " + gson.toJson(model));
		if(LISTE_DEVICES.equalsIgnoreCase(model.getRequestType())) {
			List<Device> devices = component.getListeDevices();
			conn.send(gson.toJson(devices));
		}
		
		if(SCAN_DOC.equalsIgnoreCase(model.getRequestType())) {
			;
			try {
				File doc = component.scanner(model.getOptions());
				byte[] fileContent = Files.readAllBytes(doc.toPath());
				conn.send(fileContent);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				conn.send("FAILDED TO TO SCAN DOC FROM DEVICE ( " + e.getMessage() +")");
			}
			
		}
		
        conn.send("TETTET RESTT");
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		 //ex.printStackTrace();
        if (conn != null) {
            conns.remove(conn);
            // do some thing if required
        }
        System.out.println("ERROR from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
	}

}
