package com.odmarth.scanner;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import com.odmarth.scanner.ScannerOption;

import eu.gnome.morena.Configuration;
import eu.gnome.morena.Device;
import eu.gnome.morena.Manager;
import eu.gnome.morena.Scanner;

public class ScannerComponent {
	  private static final Logger LOGGER = Logger.getLogger(ScannerComponent.class.getName());

    private File pageScanneeCourante;

    
    /**
     * Récupère la liste des périphériques de type scanner compatibles avec les options de numérisation.
     *
     * @param logDevices Si true, journalise les périphériques trouvés.
     * @return Liste des périphériques compatibles.
     */
    public List<Device> getListeDevices(boolean logDevices) {
        // Configurations des types de périphériques
        Configuration.addDeviceType(".*fficejet.*", true);
        Configuration.addDeviceType(".*eskJet.*", true);
        Configuration.addDeviceType(".*canJet.*", true);
        Configuration.addDeviceType(".*ScanMaster.*", true);
        Configuration.addDeviceType(".*Epson.*", true);
        Configuration.addDeviceType(".*Brother.*", true);
        /* Nouveau */
        
        Configuration.addDeviceType(".*WIA-.*", true);
        Configuration.addDeviceType(".*TWAIN-.*", true);
        Configuration.addDeviceType(".*ICA-.*", true); 
        Configuration.addDeviceType(".*(Scanner|Flatbed).*", true);
        Configuration.addDeviceType(".*ADF.*", true); 
        Configuration.addDeviceType(".*OfficeJet.*", true);
        Configuration.addDeviceType(".*ImageFORMULA.*", true);
        
        Manager manager = Manager.getInstance();
        List<Device> devices = manager.listDevices();

        List<Device> compatibleScanners = new ArrayList<>();
        for (Device device : devices) {
            if (device instanceof Scanner ) {
            	 Scanner scanner = (Scanner) device;	 
                try {
                  //  scanner.setMode(Scanner.RGB_16); 
                  //  scanner.setResolution(100); 
                  //  scanner.setFrame(50, 60, 1550, 2225);
                    compatibleScanners.add(scanner);
                    if (logDevices) {
                    	System.err.println("Scanner compatible trouvé : " + scanner.getFeederFunctionalUnit());
                    	System.err.println("Scanner compatible trouvé : " + scanner.getResolution());
                        LOGGER.info("Scanner compatible trouvé : " + scanner.getFileName());
                    }
                } catch (Exception e) {
                    LOGGER.warning("Scanner incompatible ignoré : " + device.getFileName());
                }
            }
        }
        return compatibleScanners;
    }
    /**
     * Récupère la liste des périphériques compatibles avec le scanner.
     *
     * @return Liste des périphériques de type scanner.
     */
  /*  public List<Device> getListeDevices() {
        Configuration.addDeviceType(".*fficejet.*", true);
        Configuration.addDeviceType(".*eskJet.*", true);
        Configuration.addDeviceType(".*canJet.*", true);
        Configuration.addDeviceType(".*ScanMaster.*", true);
        
        Manager manager = Manager.getInstance();
        List<Device> devices = manager.listDevices();
        List<Device> scanners = new ArrayList<>();
        
        // Filtre les périphériques pour ne conserver que les scanners
      //  devices.removeIf(device -> !(device instanceof Scanner));

       // return devices;
        
        for (Device device : devices) {
            if (device instanceof Scanner) {
                    scanners.add(device);
                        LOGGER.info("Périphérique trouvé : " + device.getName());
                }
            }

        return scanners;
    }
*/
    /**
     * Effectue une numérisation avec les options spécifiées.
     *
     * @param option Options de numérisation.
     * @return Fichier contenant l'image scannée.
     * @throws Exception En cas d'erreur pendant la numérisation.
     */
   /* public File scanner(final ScannerOption option) throws Exception {
        if (option.getDevice() == null) {
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

        Path repertoire = Paths.get("/sira");
        if (!Files.exists(repertoire)) {
            Files.createDirectory(repertoire);
        }

        try {
            BufferedImage image = SynchronousHelper.scanImage(scanner);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp;

            File tempFile = File.createTempFile(imageFileName, ".png");
            try (FileOutputStream fout = new FileOutputStream(tempFile)) {
                ImageIO.write(image, "png", fout);
            }

            pageScanneeCourante = tempFile;
            return pageScanneeCourante;
        } catch (Exception ex) {
            Logger.getLogger(ScannerComponent.class.getName()).log(Level.SEVERE, "Failed to scan document using the device", ex);
            throw new Exception("Failed to scan document using the device", ex);
        }
    }*/
    
    public File scanner(final ScannerOption option) throws Exception {
        // Validation des paramètres
        if (option == null) {
            throw new IllegalArgumentException("ScannerOption cannot be null.");
        }
        if (option.getDevice() == null) {
            throw new IllegalArgumentException("Scanner device cannot be null.");
        }

        Scanner scanner = (Scanner) option.getDevice();

        try {
            // Configuration du scanner
           /* scanner.setMode(Scanner.RGB_16);
            scanner.setResolution(300);
            scanner.setFrame(50, 60, 1550, 2225);*/
            
            scanner.setMode(Scanner.GRAY_8); // Mode niveaux de gris, idéal pour documents texte
          //  scanner.setResolution(400); // Résolution à 300 DPI pour un bon compromis entre qualité et taille
            scanner.setFrame(0, 0, 2481, 3508); // Utiliser toute la surface du scanner

            if (scanner.isDuplexSupported() && option.isDuplex()) {
                scanner.setDuplexEnabled(true);
            } else {
                scanner.setDuplexEnabled(false);
            }

            // Création du répertoire de sauvegarde
            Path outputDirectory = Paths.get("/sira");
            if (!Files.exists(outputDirectory)) {
                try {
                    Files.createDirectory(outputDirectory);
                } catch (IOException e) {
                    throw new IOException("Failed to create output directory: " + outputDirectory, e);
                }
            }

            // Scanning et enregistrement de l'image
            BufferedImage image = SynchronousHelper.scanImage(scanner);
          //  BufferedImage processed = ImageProcessor.processImageForVisibility(image);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp;

            File outputFile = File.createTempFile(imageFileName, ".png", outputDirectory.toFile());
           
            try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                ImageIO.write(image, "png", fileOutputStream);
            }
            return outputFile;

        } catch (Exception ex) {
            String errorMessage = "Failed to scan document using the device: " + ex.getMessage();
            Logger.getLogger(ScannerComponent.class.getName()).log(Level.SEVERE, errorMessage, ex);
            throw new Exception(errorMessage, ex);
        }
    }

    
}
