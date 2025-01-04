package com.odmarth.scanner;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.gnome.morena.Device;
import eu.gnome.morena.TransferListener;
import eu.gnome.morena.wia.WIAScanner;
import eu.gnome.morena.DeviceBase;

public class ScanSession{
	 private static final Logger LOGGER = Logger.getLogger(ScanSession.class.getName());
	 
	    private MultiFileTransferHandler transferHandler;
	    private LinkedBlockingQueue<String> queue;
	    private boolean transferFinished = false;
	    private AtomicInteger blockedThreadCount;

	    private static final String END_OF_OPERATION = ""; // string designating an End of operation

//	    public void startSession(Device device, int item) throws Exception {
//	        startSession(device, item, 0); // 0 indicates scanning until feeder is empty
//	    }
	    
	    public void startSession(Device device, int item) throws Exception {
	    	startSinglePageSession(device, item);
	    }

	    public void startSession(Device device, int item, int pages) throws Exception {
	        validateDevice(device);

	        queue = new LinkedBlockingQueue<>();
	        blockedThreadCount = new AtomicInteger(0);
	        transferFinished = false;

	        if (pages > 0 && device instanceof WIAScanner) {
	            ((WIAScanner) device).setPgcount(pages);
	        }

	        transferHandler = new MultiFileTransferHandler(pages);
	        device.setFileName("mi_" + System.currentTimeMillis());
	        ((DeviceBase) device).startTransfer(transferHandler, item);
	    }

	    public void startSinglePageSession(Device device, int item) throws Exception {
	        queue = new LinkedBlockingQueue<>();
	        blockedThreadCount = new AtomicInteger(0);
	        transferFinished = false;

	        // Configure the scanner to scan only one page
	        if (device instanceof WIAScanner) {
	            ((WIAScanner) device).setPgcount(1); // Limit to one page
	        }

	        transferHandler = new MultiFileTransferHandler(1); // Expecting one page
	        device.setFileName("singlePage_" + System.currentTimeMillis());
	        ((DeviceBase) device).startTransfer(transferHandler, item);
	    }

	    public File getImageFile() {
	        try {
	            String filename = queue.poll();
	            if (filename == null && !transferFinished) {
	                blockedThreadCount.incrementAndGet();
	                filename = queue.take();
	                blockedThreadCount.decrementAndGet();
	            }

	            if (END_OF_OPERATION.equals(filename)) {
	                releaseBlockedThreads();
	            }

	            return filename == null || filename.isEmpty() ? null : new File(filename);
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	            LOGGER.log(Level.SEVERE, "Thread interrupted while retrieving image file", e);
	            return null;
	        }
	    }

	    public boolean isEmptyFeeder() {
	        return transferHandler != null && transferHandler.code == 0;
	    }

	    public int getErrorCode() {
	        return transferHandler != null ? transferHandler.code : -1;
	    }

	    public String getErrorMessage() {
	        return transferHandler != null ? transferHandler.error : "No transfer handler initialized";
	    }

	    private void releaseBlockedThreads() {
	        int count = blockedThreadCount.getAndSet(0);
	        for (int i = 0; i < count; i++) {
	            try {
	                queue.put(END_OF_OPERATION);
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	                LOGGER.log(Level.WARNING, "Thread interrupted while releasing blocked threads", e);
	            }
	        }
	    }
	    
	    private void validateDevice(Device device) throws IllegalArgumentException {
	        if (device == null) {
	            throw new IllegalArgumentException("Device cannot be null");
	        }
	    }
	    public static String getFileExtension(File file) {
	        String name = file.getName();
	        int dotIndex = name.lastIndexOf('.');
	        return (dotIndex > 0 && dotIndex + 1 < name.length()) ? name.substring(dotIndex + 1) : "";
	    }
	public static String getExt(File file) {
		String name = file.getName();
		int ix = name.lastIndexOf('.');
		if (ix > 0 && ix + 1 < name.length()) {
			return name.substring(ix + 1);
		}
		return "";
	}
//========================================================

	/**
	 * TransferDoneListener interface implementation that handles a scanned document
	 * as a File.
	 *
	 */
	class MultiFileTransferHandler implements TransferListener {

		int code;
        String error;

        private final int pages; // Expected number of pages (0 for until feeder is empty)
        private final AtomicInteger pageCounter = new AtomicInteger(0);

        public MultiFileTransferHandler(int pages) {
            this.pages = pages;
            this.code = -1;
            this.error = "No error";
        }

        @Override
        public void transferDone(File file) {
            try {
                queue.put(file.getAbsolutePath());
                if (pageCounter.incrementAndGet() == pages) {
                    queue.put(END_OF_OPERATION);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.SEVERE, "Thread interrupted during transferDone", e);
            }
        }

        @Override
        public void transferProgress(int percent) {
            LOGGER.log(Level.INFO, "Transfer progress: {0}%", percent);
        }

        @Override
        public void transferFailed(int code, String error) {
            this.code = code;
            this.error = error;
            transferFinished = true;
            try {
                queue.put(END_OF_OPERATION);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.SEVERE, "Thread interrupted during transferFailed", e);
            }
        }
	}
}