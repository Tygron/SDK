package com.tygron.pub.utils;

import java.io.File;

public class StringUtils {

	public static final String URL_DELIMITER = "/";
	public static final String URL_SEGMENT_API = "api/";
	public static final String URL_SEGMENT_SERVERSLOT = "slots/";
	public static final String URL_SEGMENT_LISTS = "lists/";
	public static final String URL_SEGMENT_LOCATION = "location/";
	public static final String URL_SEGMENT_EVENT = "event/";
	public static final String URL_SEGMENT_SERVICES = "services/";
	public static final String URL_SEGMENT_UPDATE = "update/";
	public static final String URL_SEGMENT_SIZE = "size/";
	public static final String URL_SEGMENT_VERSION = "version/";
	public static final String URL_SEGMENT_JSON_QUERY_PARAMETER = "f=JSON";

	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String NULL = "null";

	public static final String INVALID_REQUEST = "INVALID_REQUEST";
	public static final String INSUFFICIENT_RIGHTS = "INSUFFICIENT_RIGHTS";

	public static final String EMPTY = "";
	public static final int NOTHING = -1;

	public static final String LINEBREAK = System.lineSeparator();
	public static final String FILE_SEPARATOR = File.separator;
	public static final String SPACE = " ";

	public static boolean isEmpty(String string) {
		return string == null || EMPTY.equals(string);
	}

	public static boolean isNumeric(String string) {
		try {
			Double.parseDouble(string);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
}
