/* 
 * Exactly
 * Author: Nouman Tayyab (nouman@avpreserve.com)
 * Author: Rimsha Khalid (rimsha@avpreserve.com)
 * Version: 0.1
 * Requires: JDK 1.7 or higher
 * Description: This tool transfers digital files to the UK Exactly
 * Support: info@avpreserve.com
 * License: Apache 2.0
 * Copyright: University of Kentucky (http://www.uky.edu). All Rights Reserved
 *
 */
package uk.sipperfly.ui;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import static uk.sipperfly.ui.Exactly.GACOM;
import uk.sipperfly.utils.MyTransferListener;

public class FTPConnection {

	public String host;
	public String username;
	public String password;
	public int port;
	public String mode;
	public String destination;
	public String securityType = "FTPES";
	private final Exactly parent;

	public FTPConnection(Exactly parent, String host, String username, String password, int port, String mode, String destination, String securityType) {
		if (parent == null || host == null || host.length() < 1 || username == null || username.length() < 1
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
		this.securityType = securityType;
		this.parent = parent;
	}

	/**
	 * Connect with ftp with given info.
	 *
	 * @return true if connected successfully, false otherwise
	 */
	public String validateCon() {

		try {

			FTPClient ftp = this.connect(true);
			ftp.disconnect(true);
			return this.securityType;
		} catch (SocketException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return "false";
		} catch (IOException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return "false";
		} catch (IllegalStateException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return "false";
		} catch (FTPIllegalReplyException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return "false";
		} catch (FTPException ex) {
			System.out.println(ex.getMessage());
			System.out.println(ex.getCode());
			if (ex.getMessage().contains("SECURITY_FTPES cannot be applied")) {
				this.securityType = "FTP";
				return this.validateCon();
			}
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return "false";
		}
	}

	public boolean upload(File src, FTPClient ftp, String ftpSrc) throws IOException {
		if (src.isDirectory()) {
			try {
				ftp.createDirectory(src.getName());
				Logger.getLogger(GACOM).log(Level.INFO, "Directory created: ".concat(src.getName()));
				ftp.changeDirectory(src.getName());
				for (File file : src.listFiles()) {
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
					ftp.upload(new java.io.File(src.getAbsolutePath()), new MyTransferListener(src.getAbsolutePath(), ftp));
					this.parent.uploadedFiles = this.parent.uploadedFiles + 1;
					this.parent.UpdateProgressBar(this.parent.uploadedFiles);
				} catch (SocketTimeoutException e) {
					System.out.println("path== " + src.getAbsolutePath());
					Logger.getLogger(GACOM).log(Level.SEVERE, "Socket Timeout Exception ", e.getCause());
					ftp.disconnect(true);
					ftp = this.connect(false);
					ftp.changeDirectory(ftpSrc);
					ftp.setType(FTPClient.TYPE_BINARY);
					ftp.upload(new java.io.File(src.getAbsolutePath()));
				} catch (SocketException ex) {
					Logger.getLogger(GACOM).log(Level.SEVERE, "Socket Exception ", ex.getCause());
					ftp.disconnect(true);
					ftp = this.connect(false);
					ftp.changeDirectory(ftpSrc);
					ftp.setType(FTPClient.TYPE_BINARY);
					ftp.upload(new java.io.File(src.getAbsolutePath()));
				}
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
			FTPClient ftp = this.connect(false);
			File localSrc = new File(location);
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

	private FTPClient connect(boolean onlyValidate) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
		TrustManager[] trustManager = new TrustManager[]{new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		}};
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustManager, new SecureRandom());
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			e.printStackTrace();
		}

		SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
		FTPClient ftp = new FTPClient();
//		ftp.setType(FTPClient.TYPE_BINARY);
		ftp.setAutoNoopTimeout(10000);
		ftp.setCharset("UTF-8");
		ftp.setSSLSocketFactory(sslSocketFactory);

		if (this.securityType.equalsIgnoreCase("FTPES")) {
			ftp.setSSLSocketFactory(sslSocketFactory);
			ftp.setSecurity(FTPClient.SECURITY_FTPES);
		}
		ftp.connect(this.host, this.port);
		ftp.login(this.username, this.password);

		if (this.mode.equalsIgnoreCase("passive")) {
			ftp.setPassive(true);
		} else if (this.mode.equalsIgnoreCase("active")) {
			ftp.setPassive(false);
		}
		ftp.noop();
//		System.out.println("timeout: "+ftp.getAutoNoopTimeout());
		Logger.getLogger(GACOM).log(Level.INFO, "Connected to server.");
		if (this.destination.isEmpty()) {
			this.destination = "/";
		}
		ftp.changeDirectory(this.destination);

		return ftp;
	}
}
