package com.tygron.pub.examples.utilities;

import java.util.Properties;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.SettingsUtils;
import com.tygron.pub.utils.StringUtils;

/**
 * This class provides credentials for the examples. It is also possible to create the file
 * "authentication.properties" in this package, with the following contents, which will overwrite the values
 * here:<br>
 * USERNAME=youraccount@tygron.com<br>
 * PASSWORD=yourpassw0rd<br>
 * SERVER=https://server2.tygron.com:3020/<br>
 * @author Rudolf
 *
 */

public class ExampleSettings {

	public static final String AUTH_FILE = SettingsUtils.DEFAULT_AUTH_FILE;

	public static String SERVER = "https://server2.tygron.com:3020/";
	public static String USERNAME = "";
	public static String PASSWORD = "";
	public static String CLIENT_ADDRESS = "Unknown IP address";

	public static String CLIENT_NAME = "Example from SDK";

	static {
		try {
			Properties prop = SettingsUtils.loadProperties(AUTH_FILE);

			USERNAME = prop.getProperty("USERNAME", USERNAME);
			PASSWORD = prop.getProperty("PASSWORD", PASSWORD);
			SERVER = prop.getProperty("SERVER", SERVER);

		} catch (Exception e) {
			Log.exception(e, "Credentials not loaded from file.");
		}

		if (!(StringUtils.isEmpty(SERVER) || StringUtils.isEmpty(USERNAME) || StringUtils.isEmpty(PASSWORD))) {
			System.err.println("Failed to load credentials file: " + AUTH_FILE + ", and no credentials in "
					+ ExampleSettings.class.getSimpleName() + ".");
			System.exit(-1);
		}
	}
}
