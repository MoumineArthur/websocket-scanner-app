package com.odmarth.socket;

import java.net.UnknownHostException;

public class ScanningApp {
	
public static void main(String[] args) throws Exception {
	
	try {
		new ODMWebSocket().start();
	} catch (UnknownHostException e) {
		throw new Exception("Failed to load socket");
	}
}
}
