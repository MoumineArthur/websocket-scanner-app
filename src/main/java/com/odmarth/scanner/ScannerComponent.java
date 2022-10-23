package com.odmarth.scanner;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;


import eu.gnome.morena.Configuration;
import eu.gnome.morena.Device;
import eu.gnome.morena.Manager;
import eu.gnome.morena.Scanner;


public class ScannerComponent {


	private File pageScanneeCourante;
	

	public List<Device> getListeDevices() {
		Configuration.addDeviceType(".*fficejet.*", true);
		Configuration.addDeviceType(".*eskJet.*", true);
		Configuration.addDeviceType(".*canJet.*", true);
		Manager manager = Manager.getInstance();
		List<Device> devices = manager.listDevices();
		Iterator<Device> it = devices.iterator();
		while (it.hasNext()) {
			Device d  = it.next();
			if(!(d instanceof Scanner)) {
				devices.remove(d);
			}
			
		}
		
		return devices;
	}


	public File scanner(final ScannerOption option) throws Exception {

		if (option.getDevice()!=null) {
			throw new Exception("Null device");
		}

		Scanner scanner = (Scanner) option.getDevice();
		scanner.setMode(Scanner.RGB_16);
		scanner.setResolution(100);
		scanner.setFrame(50, 60, 1550, 2225);

		if (scanner.isDuplexSupported() && option.isDuplex()) {
			scanner.setDuplexEnabled(true);
		} else {
			scanner.setDuplexEnabled(false);
		}

		try {
			Path repertoire = Paths.get("/sira");
			if (!Files.exists(repertoire)) {
				Files.createDirectory(repertoire);
			}
			BufferedImage image = SynchronousHelper.scanImage(scanner);
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			String imageFileName = "JPEG_" + timeStamp + "_";

			File tempFile = File.createTempFile(imageFileName, ".png");
			if (!tempFile.exists()) {
				tempFile.createNewFile();
			}
			FileOutputStream fout = new FileOutputStream(tempFile);
			ImageIO.write(image, "png", fout);
			pageScanneeCourante = tempFile;
			return pageScanneeCourante;
		} catch (Exception ex) {
			Logger.getLogger(ScannerComponent.class.getName()).log(Level.SEVERE, null, ex);
			throw new Exception("Failed to scanne document using the device ", ex);
		}
	}

	
}
