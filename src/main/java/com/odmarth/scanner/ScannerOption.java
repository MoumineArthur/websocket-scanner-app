package com.odmarth.scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import eu.gnome.morena.Device;

/**
 *
 * @author USER
 */
public class ScannerOption {
    
    private Device device;
    private String deviceName;
    private boolean duplex;
    private boolean adfScan;
    private int resolution;
    private String nomDuDocument;

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
    
    public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public boolean isDuplex() {
        return duplex;
    }

    public void setDuplex(boolean duplex) {
        this.duplex = duplex;
    }

    public boolean isAdfScan() {
        return adfScan;
    }

    public void setAdfScan(boolean adfScan) {
        this.adfScan = adfScan;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public String getNomDuDocument() {
        return nomDuDocument;
    }

    public void setNomDuDocument(String nomDuDocument) {
        this.nomDuDocument = nomDuDocument;
    }
}
