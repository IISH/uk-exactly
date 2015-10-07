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
package uk.sipperfly.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPSClient;

public class FTPUtil {

	private static String GACOM = "com.UKExactly";

	/**
	 * Upload whole directory (including its nested sub directories and files) to FTP server.
	 *
	 * @param ftpClient       an instance of org.apache.commons.net.ftp.FTPClient class.
	 * @param remoteDirPath   Path of the destination directory on the server.
	 * @param localParentDir  Path of the local directory being uploaded.
	 * @param remoteParentDir Path of the parent directory of the current directory on the server (used by recursive calls).
	 * @throws IOException if any network or IO error occurred.
	 */
	public static boolean uploadDirectory(FTPSClient ftpClient,
			String remoteDirPath, String localParentDir, String remoteParentDir)
			throws IOException {

		System.out.println("LISTING directory: " + localParentDir);
		Logger.getLogger(GACOM).log(Level.INFO, "LISTING directory: ", localParentDir);
		File localDir = new File(localParentDir);
		File[] subFiles = localDir.listFiles();
		if (subFiles != null && subFiles.length > 0) {
			for (File item : subFiles) {
				String remoteFilePath = remoteDirPath + "/" + remoteParentDir
						+ "/" + item.getName();
				if (remoteParentDir.equals("")) {
					remoteFilePath = remoteDirPath + "/" + item.getName();
				}
				if (item.isFile()) {
					// upload the file
					String localFilePath = item.getAbsolutePath();
					Logger.getLogger(GACOM).log(Level.INFO, "About to upload the file: ", localFilePath);
					System.out.println("About to upload the file: " + localFilePath);
					boolean uploaded = uploadSingleFile(ftpClient, localFilePath, remoteFilePath);
					
					if (uploaded) {
						Logger.getLogger(GACOM).log(Level.INFO, "UPLOADED a file to: ", remoteFilePath);
						System.out.println("UPLOADED a file to: " + remoteFilePath);
					} else {
						System.out.println("COULD NOT upload the file: " + localFilePath);
						Logger.getLogger(GACOM).log(Level.INFO, "COULD NOT upload the file: ", localFilePath);
						return false;
					}
				} else {
					// create directory on the server
					boolean created = ftpClient.makeDirectory(remoteFilePath);
					if (created) {
						System.out.println("CREATED the directory: " + remoteFilePath);
						Logger.getLogger(GACOM).log(Level.INFO, "CREATED the directory: ", remoteFilePath);
					} else {
						System.out.println("COULD NOT create the directory: " + remoteFilePath);
						Logger.getLogger(GACOM).log(Level.INFO, "COULD NOT create the directory: ", remoteFilePath);
						return false;
					}

					// upload the sub directory
					String parent = remoteParentDir + "/" + item.getName();
					if (remoteParentDir.equals("")) {
						parent = item.getName();
					}

					localParentDir = item.getAbsolutePath();
					uploadDirectory(ftpClient, remoteDirPath, localParentDir, parent);
				}
			}
		}
		return true;
	}

	/**
	 * Upload a single file to the FTP server.
	 *
	 * @param ftpClient      an instance of org.apache.commons.net.ftp.FTPClient class.
	 * @param localFilePath  Path of the file on local computer
	 * @param remoteFilePath Path of the file on remote the server
	 * @return true if the file was uploaded successfully, false otherwise
	 * @throws IOException if any network or IO error occurred.
	 */
	public static boolean uploadSingleFile(FTPSClient ftpClient,
			String localFilePath, String remoteFilePath) throws IOException {
		File localFile = new File(localFilePath);
		try (InputStream inputStream = new FileInputStream(localFile)) {
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			return ftpClient.storeFile(remoteFilePath, inputStream);
		}
	}
}
