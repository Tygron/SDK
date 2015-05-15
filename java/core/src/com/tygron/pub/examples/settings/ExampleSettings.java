package com.tygron.pub.examples.settings;

import java.io.FileInputStream;
import java.util.Properties;

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

	public static String SERVER = "https://server2.tygron.com:3020/";

	public static String USERNAME = "";

	public static String PASSWORD = "";
	public static String CLIENT_ADDRESS = "Unknown IP address";

	public static String CLIENT_NAME = "Example from SDK";

	static {
		if (USERNAME == null || USERNAME.equals("") || PASSWORD == null || PASSWORD.equals("")
				|| SERVER == null || SERVER.equals("")) {
			try {

				Properties prop = new Properties();
				FileInputStream in = new FileInputStream("./authentication.properties");
				prop.load(in);
				in.close();

				USERNAME = prop.getProperty("USERNAME", USERNAME);
				PASSWORD = prop.getProperty("PASSWORD", PASSWORD);
				SERVER = prop.getProperty("SERVER", SERVER);

			} catch (Exception e) {
				e.printStackTrace();
				System.err
						.println("Failed to load credentials file: authentication.properties, and no credentials in ExampleSettings.");
				System.exit(-1);
			}
		}
	}
}
