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

import uk.sipperfly.utils.FTPUtil;
import java.io.File;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPReply;
import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
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
			if (ftp.login(user, pass)) {
				ftp.logout();
				if (ftp.isConnected()) {
					try {
						ftp.disconnect();
						return true;
					} catch (IOException ioe) {
						Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ioe);
						return true;
					}
				}
			} else {
				Logger.getLogger(GACOM).log(Level.INFO, "FTP Login: Invalid username or password");
				return false;
			}

			return true;
		} catch (SocketException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		} catch (IOException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}
	}

	/**
	 * Upload files to ftp server
	 *
	 * @param location of the folder which have to upload on ftp
	 * @param type     zip file or folder
	 * @return
	 */
	public boolean uploadFiles(String location, String type) {
		this.validateCon();
		FTPUtil ftpUtil = new FTPUtil(this);
		FTPClient ftpClient = new FTPClient();
		ftpClient.setControlEncoding("UTF-8");
		try{
			ftpClient.connect(this.host, this.port);
			ftpClient.login(this.username, this.password);
			if (this.mode.equalsIgnoreCase("passive")) {
				ftpClient.enterLocalPassiveMode();
			} else if (this.mode.equalsIgnoreCase("active")) {
				ftpClient.enterLocalActiveMode();
			}
			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				Logger.getLogger(GACOM).log(Level.INFO, "FTP Login: ".concat(ftpClient.getReplyString()));
				ftpClient.disconnect();
				return false;
			}
			ftpClient.setKeepAlive(true);
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
			ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
			ftpClient.setControlKeepAliveTimeout(999999999);
			ftpClient.sendSiteCommand("RECFM=FB");
			ftpClient.sendSiteCommand("LRECL=2000");
			ftpClient.sendSiteCommand("BLKSIZE=27000");
			ftpClient.sendSiteCommand("CY");
			ftpClient.sendSiteCommand("PRI= 50");
			ftpClient.sendSiteCommand("SEC=25");
			String remoteFile;
			File localFile = new File(location);
			if (this.destination != null) {
				if (this.destination.endsWith("/")) {
					remoteFile = this.destination + localFile.getName();
				} else {
					remoteFile = this.destination + "/" + localFile.getName();
				}
			} else {
				if (ftpClient.printWorkingDirectory().endsWith("/")) {
					remoteFile = ftpClient.printWorkingDirectory() + localFile.getName();
				} else {
					remoteFile = ftpClient.printWorkingDirectory() + "/" + localFile.getName();
				}
			}
			boolean done;
			if (type.equals("zip")) {
				done = FTPUtil.uploadSingleFile(ftpClient, location, remoteFile);
				if (done) {
					ftpClient.logout();
					ftpClient.disconnect();
					Logger.getLogger(GACOM).log(Level.INFO, "FTP Upload: The file is uploaded successfully.");
					System.out.println("The file is uploaded successfully.");
					return true;
				} else {
					ftpClient.logout();
					ftpClient.disconnect();
					Logger.getLogger(GACOM).log(Level.SEVERE, "FTP Upload: Error occured while uploading.");
					System.out.println("Error occured while uploading.");
					return false;
				}
			} else {
				ftpClient.makeDirectory(remoteFile);
				done = FTPUtil.uploadDirectory(ftpClient, remoteFile, location, "");
				if (done) {
					ftpClient.logout();
					ftpClient.disconnect();
					Logger.getLogger(GACOM).log(Level.INFO, "FTP Upload: The file is uploaded successfully.");
					System.out.println("The file is uploaded successfully.");
					return true;
				} else {
					ftpClient.logout();
					ftpClient.disconnect();
					Logger.getLogger(GACOM).log(Level.SEVERE, "FTP Upload: Error occured while uploading.");
					System.out.println("Error occured while uploading.");
					return false;
				}
			}
		} catch (SocketException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		} catch (IOException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}
		
	}

}
