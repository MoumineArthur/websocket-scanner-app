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
import eu.gnome.morena.Scanner;

public class ODMWebSocket extends WebSocketServer{
	
	private static final int TCP_PORT = 6987;
    private static final String REQUEST_LIST_DEVICES = "DEVICES";
    private static final String REQUEST_SCAN_DOCUMENT = "SCAN_DOC";
    
    private final Set<WebSocket> connections;
    
    public ODMWebSocket() {
        super(new InetSocketAddress(TCP_PORT));
        connections = new HashSet<>();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        System.out.println("New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        System.out.println("Closed connection to " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message from client: " + message);

        try {
            Gson gson = new Gson();
            SocketRequestModel model = gson.fromJson(message, SocketRequestModel.class);
            ScannerComponent scannerComponent = new ScannerComponent();
            System.out.println("Message from client TYPE : " + model.getRequestType());
            switch (model.getRequestType().toUpperCase()) {
                case REQUEST_LIST_DEVICES:
                    handleListDevices(conn, gson, scannerComponent);
                    break;
                case REQUEST_SCAN_DOCUMENT:
                    handleScanDocument(conn, gson, scannerComponent, model);
                    break;
                default:
                    conn.send("Unknown request type: " + model.getRequestType());
            }
        } catch (Exception e) {
            conn.send("Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleListDevices(WebSocket conn, Gson gson, ScannerComponent scannerComponent) {
    	 System.out.println("Message from client: ");
        try {
            List<Device> devices = scannerComponent.getListeDevices(true);
            conn.send(gson.toJson(devices));
        } catch (Exception e) {
        	e.printStackTrace();
        	System.err.println("Failed to retrieve devices: " + e.getMessage());
            sendError(conn, "Failed to retrieve devices: " + e.getMessage(), e);
        }
    }

    private void handleScanDocument(WebSocket conn, Gson gson, ScannerComponent scannerComponent, SocketRequestModel model) {
        try {
        	 List<Device> devices = scannerComponent.getListeDevices(true);
        	// Device selectedDevice = devices.stream().filter(scanner -> scanner.getFileName()
        	//		 .equalsIgnoreCase(model.getOptions().getDeviceName())).findFirst().;
        	 for(Device device: devices) {
        		 if (device instanceof Scanner ) {
                	 Scanner scanner = (Scanner) device;	 
                	 model.getOptions().setDevice(scanner);
                	 File scannedFile = scannerComponent.scanner(model.getOptions());
                     byte[] fileContent = Files.readAllBytes(scannedFile.toPath());
                     conn.send(fileContent); // Binary response
                     break;
        		 }
        		
        	 }
           
        } catch (Exception e) {
            sendError(conn, "Failed to scan document: " + e.getMessage(), e);
        }
    }

    private void sendError(WebSocket conn, String message, Exception e) {
        System.err.println(message);
        if (e != null) {
            e.printStackTrace();
        }
        conn.send(message);
    }

    /*
	@Override
	public void onMessage(WebSocket conn, String message) {
		System.out.println("Message from client: " + message);

		Gson gson = new Gson();
		SocketRequestModel model = gson.fromJson(message, SocketRequestModel.class);
		ScannerComponent component = new ScannerComponent();
		
		System.out.println("Model : " + gson.toJson(model));
		if(LISTE_DEVICES.equalsIgnoreCase(model.getRequestType())) {
			List<Device> devices = component.getListeDevices(true);
			conn.send(gson.toJson(devices));
		}
		
		if(SCAN_DOC.equalsIgnoreCase(model.getRequestType())) {
			try {
				File doc = component.scanner(model.getOptions());
				byte[] fileContent = Files.readAllBytes(doc.toPath());
				conn.send(fileContent);
			} catch (Exception e) {
				e.printStackTrace();
				conn.send("FAILDED TO TO SCAN DOC FROM DEVICE ( " + e.getMessage() +")");
			}
			
		}
		
        conn.send("TETTET RESTT");
	}*/

    @Override
    public void onError(WebSocket conn, Exception ex) {
        if (conn != null) {
            connections.remove(conn);
            System.out.println("Error from connection: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
        }
        ex.printStackTrace();
    }

}
