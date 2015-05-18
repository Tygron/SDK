package com.tygron.pub.utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUtils {

	private final static ObjectMapper MAPPER;
	private final static ObjectReader READER_LIST;
	private final static ObjectReader READER_MAP;

	static {
		MAPPER = new ObjectMapper();
		MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
		MAPPER.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);

		MAPPER.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		MAPPER.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
		MAPPER.setVisibility(PropertyAccessor.IS_GETTER, Visibility.NONE);
		MAPPER.setVisibility(PropertyAccessor.SETTER, Visibility.NONE);

		READER_LIST = JsonUtils.MAPPER.reader(List.class);
		READER_MAP = JsonUtils.MAPPER.reader(Map.class);
	}

	public static <T extends Object> T mapJsonIntoObject(final String json, final T object) {
		if (object == null) {
			throw new NullPointerException("Object to map to may not be null");
		}
		if (json == null || StringUtils.EMPTY.equals(json)) {
			return null;
		}

		try {
			return MAPPER.readerForUpdating(object).readValue(json);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to parse Json to "
					+ object.getClass().getCanonicalName(), e);
		}
	}

	public static List<?> mapJsonToList(final String json) {
		if (json == null || StringUtils.EMPTY.equals(json)) {
			return null;
		}

		try {
			return READER_LIST.readValue(json);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to parse Json to list", e);
		}
	}

	public static Map<?, ?> mapJsonToMap(final String json) {
		if (json == null || StringUtils.EMPTY.equals(json)) {
			return null;
		}

		try {
			return READER_MAP.readValue(json);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to parse Json to map", e);
		}
	}

	public static <T> T mapJsonToType(final String json, final Class<T> type) {
		if (type == null) {
			throw new NullPointerException("Type to map to may not be null");
		}
		if (json == null || StringUtils.EMPTY.equals(json)) {
			return null;
		}

		try {
			return MAPPER.reader(type).readValue(json);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to parse Json to " + type.getCanonicalName(), e);
		}
	}

	public static String mapObjectToJson(final Object object) {
		if (object == null) {
			return StringUtils.EMPTY;
		}

		try {
			return MAPPER.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Failed to parse object to json", e);
		}
	}
}
