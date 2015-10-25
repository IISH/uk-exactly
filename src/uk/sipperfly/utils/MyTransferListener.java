/*
 * UK Exactly
 * Author: Nouman Tayyab
 * Version: 1.0
 * Requires: JDK 1.7 or higher
 * Description: This tool transfers digital files to the AVPreserve
 * Support: info@avpreserve.com
 * Copyright 2015 AVPreserve
 */
package uk.sipperfly.utils;

import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.sipperfly.ui.FTPConnection;

/**
 *
 * @author noumantayyab
 */
public class MyTransferListener implements FTPDataTransferListener {

	private static String GACOM = "com.UKExactly";
	private String filePath;

	public MyTransferListener(String filePath) {
		this.filePath = filePath;
	}

	public void started() {
		Logger.getLogger(GACOM).log(Level.INFO, "File transfer started: ".concat(this.filePath));
		// Transfer startedÏ
	}

	public void transferred(int length) {
		// Yet other length bytes has been transferred since the last time this
		// method was called
	}

	public void completed() {
		Logger.getLogger(GACOM).log(Level.INFO, "File transfer completed: ".concat(this.filePath));
		// Transfer completed
	}

	public void aborted() {
		Logger.getLogger(GACOM).log(Level.INFO, "File transfer aborted: ".concat(this.filePath));
		// Transfer aborted
	}

	public void failed() {
		Logger.getLogger(GACOM).log(Level.INFO, "File transfer failed: ".concat(this.filePath));
		// Transfer failed
	}

}
