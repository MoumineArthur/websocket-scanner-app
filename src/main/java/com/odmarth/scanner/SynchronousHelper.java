package com.odmarth.scanner;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import eu.gnome.morena.Device;
import eu.gnome.morena.DeviceBase;
import eu.gnome.morena.TransferDoneListener;

public class SynchronousHelper {

	public static final int WIA_ERROR_PAPER_EMPTY = 417;
    private static final Logger LOGGER = Logger.getLogger(SynchronousHelper.class.getName());

	 /**
     * TransferDoneListener implementation for handling scanned document as BufferedImage.
     */
    static class ImageTransferHandler implements TransferDoneListener {
        BufferedImage image;
        int code;
        String error;
        boolean transferDone = false;

        @Override
        public void transferDone(File file) {
            if (file != null) {
                try {
                    image = ImageIO.read(file);
                } catch (IOException e) {
                    error = "Error reading image file: " + e.getMessage();
                    LOGGER.log(Level.SEVERE, error, e);
                }
            }
            notifyRequestor();
        }

        @Override
        public void transferFailed(int code, String error) {
            this.code = code;
            this.error = error;
            LOGGER.log(Level.SEVERE, "Transfer failed with code {0}: {1}", new Object[]{code, error});
            notifyRequestor();
        }

        private synchronized void notifyRequestor() {
            transferDone = true;
            this.notify();
        }
    }
    
    
	/**
     * TransferDoneListener implementation for handling scanned document as File.
     */
    static class FileTransferHandler implements TransferDoneListener {
        File imageFile;
        int code;
        String error;
        boolean transferDone = false;

        @Override
        public void transferDone(File file) {
            imageFile = file;
            notifyRequestor();
        }

        @Override
        public void transferFailed(int code, String error) {
            this.code = code;
            this.error = error;
            LOGGER.log(Level.SEVERE, "Transfer failed with code {0}: {1}", new Object[]{code, error});
            notifyRequestor();
        }

        private synchronized void notifyRequestor() {
            transferDone = true;
            this.notify();
        }
    }
    
    /**
     * Scans an image using the specified device and returns it as a BufferedImage.
     *
     * @param device The scanning device.
     * @return BufferedImage of the scanned document.
     * @throws Exception If an error occurs during scanning.
     */
    public static BufferedImage scanImage(Device device) throws Exception {
        return scanImage(device, 0);
    }

    /**
     * Scans an image using the specified device and functional unit, and returns it as a BufferedImage.
     *
     * @param device The scanning device.
     * @param item   The functional unit (e.g., flatbed, document feeder).
     * @return BufferedImage of the scanned document.
     * @throws Exception If an error occurs during scanning.
     */
    public static BufferedImage scanImage(Device device, int item) throws Exception {
        if (device == null) {
            throw new IllegalArgumentException("Device cannot be null.");
        }

        ImageTransferHandler handler = new ImageTransferHandler();

        synchronized (handler) {
            ((DeviceBase) device).startTransfer(handler, item);
            while (!handler.transferDone) {
                handler.wait();
            }
        }

        if (handler.image != null) {
            return handler.image;
        }

        throw new Exception("Failed to scan image: " + handler.error);
    }

    /**
     * Scans an image using the specified device and returns it as a File.
     *
     * @param device The scanning device.
     * @return File containing the scanned document.
     * @throws Exception If an error occurs during scanning.
     */
    public static File scanFile(Device device) throws Exception {
        return scanFile(device, 0);
    }

    /**
     * Scans an image using the specified device and functional unit, and returns it as a File.
     *
     * @param device The scanning device.
     * @param item   The functional unit (e.g., flatbed, document feeder).
     * @return File containing the scanned document.
     * @throws Exception If an error occurs during scanning.
     */
    public static File scanFile(Device device, int item) throws Exception {
        if (device == null) {
            throw new IllegalArgumentException("Device cannot be null.");
        }

        FileTransferHandler handler = new FileTransferHandler();

        synchronized (handler) {
            ((DeviceBase) device).startTransfer(handler, item);
            while (!handler.transferDone) {
                handler.wait();
            }
        }

        if (handler.imageFile != null) {
            return handler.imageFile;
        }

        throw new Exception("Failed to scan file: " + handler.error);
    }

}