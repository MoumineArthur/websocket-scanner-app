package com.odmarth.socket;

public class ScanningApp {
	
public static void main(String[] args) throws Exception {
	
	try {
		new ODMWebSocket().start();
	} catch (Exception e) {
		throw new Exception("Failed to load socket");
	}
}
}
