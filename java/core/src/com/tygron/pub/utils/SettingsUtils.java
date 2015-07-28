package com.tygron.pub.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import com.tygron.pub.logger.Log;

public class SettingsUtils {

	public static enum TygronCredential {
			SERVER(
				true),
			USERNAME(
				true),
			PASSWORD(
				true),
			CLIENT_ADDRESS(
				false),
			CLIENT_NAME(
				false);

		private boolean essential = false;

		private TygronCredential(boolean essential) {
			this.essential = essential;
		}

		public boolean isEssential() {
			return essential;
		}
	}

	public static final String EXTENSION = ".cfg";
	public static final String DEFAULT_AUTH_FILE = "authentication" + EXTENSION;

	public static boolean isValidCredentials(Properties properties) {
		for (TygronCredential credential : TygronCredential.values()) {
			if (credential.isEssential()
					&& StringUtils.isEmpty(properties.getProperty(credential.toString()))) {
				return false;
			}
		}
		return true;
	}

	public static Properties loadProperties() throws IOException {
		return loadProperties(DEFAULT_AUTH_FILE);
	}

	public static Properties loadProperties(String fileName) throws IOException {
		Properties prop = new Properties();
		if (StringUtils.isEmpty(fileName)) {
			return prop;
		}
		File file = new File("./" + fileName);
		if (!file.exists()) {
			return prop;
		}

		FileInputStream in = new FileInputStream(file);
		try {

			prop.load(in);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			Log.exception(e, "An unexpected exception occurred while loading file: " + fileName);
			throw e;
		} finally {
			in.close();
		}
		return prop;
	}

	public static boolean storeProperties(Properties properties) throws IOException {
		return storeProperties(DEFAULT_AUTH_FILE, properties, false, true);
	}

	public static boolean storeProperties(String fileName, Properties properties, boolean onlyIfExists,
			boolean overwrite) throws IOException {
		File file = new File("./" + fileName);
		if (file.exists() && !overwrite) {
			return false;
		}
		if (!file.exists() && onlyIfExists) {
			return false;
		}

		file.createNewFile();
		FileOutputStream out = new FileOutputStream(file);
		try {
			properties.store(out, StringUtils.EMPTY);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			Log.exception(e, "An unexpected exception occurred while loading file: " + fileName);
			throw e;
		} finally {
			out.close();
		}

		return true;
	}
}
