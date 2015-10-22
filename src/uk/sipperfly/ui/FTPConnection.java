/* 
 * Exactly
 * Author: Nouman Tayyab (nouman@avpreserve.com)
 * Author: Rimsha Khalid (rimsha@avpreserve.com)
 * Version: 0.1
 * Requires: JDK 1.7 or higher
 * Description: This tool transfers digital files to the UK Exactly
 * Support: info@avpreserve.com
 * Copyright Audio Visual Preservation Solutions, Inc
 */
package uk.sipperfly.ui;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import java.io.File;
import java.io.FileInputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPReply;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.apache.commons.net.ftp.FTPClient;
//import org.apache.commons.net.ftp.FTPSClient;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import static uk.sipperfly.ui.Exactly.GACOM;

public class FTPConnection {

	public String host;
	public String username;
	public String password;
	public int port;
	public String mode;
	public String destination;

	public FTPConnection(String host, String username, String password, int port, String mode, String destination) {
		if (host == null || host.length() < 1 || username == null || username.length() < 1
				|| password == null || password.length() < 1
				|| mode == null || mode.length() < 1) {
			throw new IllegalArgumentException();
		}
		this.host = host;
		this.username = username;
		this.password = password;
		this.port = port;
		this.mode = mode;
		this.destination = destination;
	}

	/**
	 * Connect with ftp with given info.
	 *
	 * @return true if connected successfully, false otherwise
	 */
	public boolean validateCon() {
		try {
			String server = this.host;
			int ftpPort = this.port;
			String user = this.username;
			String pass = this.password;
			FTPClient ftp = new FTPClient();
			ftp.connect(server, ftpPort);

			System.out.println("Connected to " + server + ".");
			ftp.login(user, pass);
			ftp.disconnect(true);
			return true;
		} catch (SocketException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		} catch (IOException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		} catch (IllegalStateException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		} catch (FTPIllegalReplyException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		} catch (FTPException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}
	}

	public boolean upload(File src, FTPClient ftp, String ftpSrc) throws IOException {
		if (src.isDirectory()) {
			try {
				ftp.createDirectory(src.getName());
				Logger.getLogger(GACOM).log(Level.INFO, "Directory created: ".concat(src.getName()));
				ftp.changeDirectory(src.getName());
				for (File file : src.listFiles()) {
					Logger.getLogger(GACOM).log(Level.INFO, "About to upload the file: ".concat(file.getAbsolutePath()));
					System.out.println("About to upload the file: " + file);
					upload(file, ftp, src.getName());
				}
				ftp.changeDirectoryUp();
			} catch (IllegalStateException ex) {
				Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			} catch (FTPIllegalReplyException ex) {
				Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			} catch (FTPException ex) {
				Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			}
		} else {
			try {
				try {
					ftp.setType(FTPClient.TYPE_BINARY);
					ftp.upload(new java.io.File(src.getAbsolutePath()));
				} catch (SocketTimeoutException e) {
					Logger.getLogger(GACOM).log(Level.SEVERE, "Socket Timeout Exception ", e.getCause());
					ftp = this.reconnect();
					ftp.changeDirectory(ftpSrc);
					ftp.setType(FTPClient.TYPE_BINARY);
					ftp.upload(new java.io.File(src.getAbsolutePath()));
				} catch (SocketException ex) {
					Logger.getLogger(GACOM).log(Level.SEVERE, "Socket Exception ", ex.getCause());
					ftp = this.reconnect();
					ftp.changeDirectory(ftpSrc);
					ftp.setType(FTPClient.TYPE_BINARY);
					ftp.upload(new java.io.File(src.getAbsolutePath()));
				}
				System.out.println("UPLOADED a file to: " + src.getAbsolutePath());
				Logger.getLogger(GACOM).log(Level.SEVERE, "UPLOADED a file to: ".concat(src.getAbsolutePath()));
			} catch (IllegalStateException ex) {
				Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			} catch (FileNotFoundException ex) {
				Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			} catch (FTPIllegalReplyException ex) {
				Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			} catch (FTPException ex) {
				Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			} catch (FTPDataTransferException ex) {
				Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			} catch (FTPAbortedException ex) {
				Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			}
		}
		return true;
	}

	/**
	 * Upload files to ftp server
	 *
	 * @param location of the folder which have to upload on ftp
	 * @param type     zip file or folder
	 * @return
	 */
	public boolean uploadFiles(String location, String type) throws IOException {
		try {
			File localSrc = new File(location);
			FTPClient ftp = new FTPClient();
			ftp.setCharset("UTF-8");
			ftp.connect(this.host, this.port);
			ftp.login(this.username, this.password);
			if (this.mode.equalsIgnoreCase("passive")) {
				ftp.setPassive(true);
			} else if (this.mode.equalsIgnoreCase("active")) {
				ftp.setPassive(false);
			}
			Logger.getLogger(GACOM).log(Level.INFO, "Connected to server.");
			ftp.changeDirectory(this.destination);
			try {
				if (!upload(localSrc, ftp, "")) {
					return false;
				}
			} catch (SocketException ex) {
				Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			} finally {
				ftp.disconnect(true);
				Logger.getLogger(GACOM).log(Level.INFO, "FTP disconnected");
			}
			return true;
		} catch (IllegalStateException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		} catch (FTPIllegalReplyException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		} catch (FTPException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}
	}

	private FTPClient reconnect() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
		FTPClient ftp = new FTPClient();
		ftp.setCharset("UTF-8");
		ftp.connect(this.host, this.port);
		ftp.login(this.username, this.password);
		if (this.mode.equalsIgnoreCase("passive")) {
			ftp.setPassive(true);
		} else if (this.mode.equalsIgnoreCase("active")) {
			ftp.setPassive(false);
		}
		Logger.getLogger(GACOM).log(Level.INFO, "Connected to server.");
		ftp.changeDirectory(this.destination);
		return ftp;
	}
}
