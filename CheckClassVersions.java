package me.ahola.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 
 * @author jari
 *
 */
public class CheckClassVersions {

	private static final int VERSION_ID_BASE = 44; // Magic start of Java versioning
	private static final int MAX_VERSION = 9;

	private static String rootFolder;
	private static int targetVersion = 0;
	
	private static int versionCount[] = new int[MAX_VERSION + 1];
	
	private static int totalFileCount = 0;
	private static int totalFolderCount = 0;
	private static int totalJavaFileCount = 0;
	private static int totalClassFileCount = 0;
	
	private static boolean acceptTV;

	public static void main(String[] args) {
		for (String param : args) {
			if (param.startsWith("-")) {
				if (param.equals("-tv")) {
					acceptTV = true;
				} else {
					showUsage();
					System.exit(1);
				}
				
			} else {
				if (acceptTV) {
					targetVersion = Integer.parseInt(param);
					acceptTV = false;
				} else {
					rootFolder = param;
				}
			}
		}
		if (rootFolder == null) {
			showUsage();
			System.exit(1);
		}
		System.out.println("Scanning folder " + rootFolder + " and its subfolders.");
		File folder = new File(rootFolder);
		listFilesInFolder(folder);
		printResults();
	}

	private static void showUsage() {
		System.out.println("Usage:");
		System.out.println("java...");
	}

	private static String getParam(String param) {
		int end = param.indexOf(" ");
		if (end < 0) {
			return param;
		}
		return param.substring(0, end);
	}

	private static void printResults() {
		System.out.println();
		System.out.println("Results:");
		for (int i = 1; i <= MAX_VERSION; i++) {
			if (versionCount[i] > 0) {
				System.out.println("Java " + i + " : " + versionCount[i]);
			}
		}
		if (versionCount[0] > 0) {
			System.out.println("Unknown: " + versionCount[0]);
		}
		System.out.println();
		System.out.println("Total files      : " + totalFileCount);
		System.out.println("Total folders    : " + totalFolderCount);
		System.out.println("Total java files : " + totalJavaFileCount);
		System.out.println("Total class files: " + totalClassFileCount);
	}

	private static void listFilesInFolder(File folder) {
		File[] listOfFiles = folder.listFiles();
		totalFolderCount++;
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				totalFileCount++;
				if (listOfFiles[i].getName().endsWith(".class")) {
					totalClassFileCount++;
					addVersionCount(readVersionBytes(listOfFiles[i].getPath()));
				} if (listOfFiles[i].getName().endsWith(".java")) {
					totalJavaFileCount++;
				}
			} else if (listOfFiles[i].isDirectory()) {
				listFilesInFolder(listOfFiles[i]);
			}
		}
	}
	
	private static void addVersionCount(int version) {
		if (version > 0 && version <= MAX_VERSION) {
			versionCount[version]++;
		} else {
			versionCount[0]++;
		}
	}

	private static int readVersionBytes(String pathname) {
		int version = 0;
		try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(pathname))) {
		    byte[] bbuf = new byte[16];
		    int len;
			while ((len = in.read(bbuf)) != -1) {
				if (len > 8) {
					version = bbuf[7] - VERSION_ID_BASE; // 6 and 7 bytes actually
					if (targetVersion > 0 && version != targetVersion) {
						System.out.println("jdk " + version + ": " + pathname);
					}
				}
				break;
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
		return version;
	}

}