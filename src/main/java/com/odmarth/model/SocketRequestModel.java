package com.odmarth.model;

import com.odmarth.scanner.ScannerOption;

public class SocketRequestModel {
	String requestType;
	ScannerOption options;
	public String getRequestType() {
		return requestType;
	}
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	public ScannerOption getOptions() {
		return options;
	}
	public void setOptions(ScannerOption options) {
		this.options = options;
	}
	
	
}
