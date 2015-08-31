package com.tygron.pub.api.data.misc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.tygron.pub.utils.JsonUtils;
import com.tygron.pub.utils.StringUtils;

public class GeometryObject {
	public static GeometryObject getGeometryObject(Map<String, ?> map) {
		return JsonUtils.castToObject(map, GeometryObject.class);
	}

	public static GeometryObject getGeometryObject(String coordinates) {
		return getGeometryObject("MultiPolygon", coordinates);
	}

	public static GeometryObject getGeometryObject(String type, String coordinates) {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("type", type);
		map.put(coordinates, JsonUtils.mapJsonToList(coordinates));

		return getGeometryObject(map);
	}

	private String type = StringUtils.EMPTY;

	private List<?> coordinates = null;
	private String stringCoordinates = null;

	public List<?> getCoordinates() {
		return coordinates;
	}

	public String getCoordinatesAsString() {
		if (coordinates == null) {
			return StringUtils.EMPTY;
		}

		if (!StringUtils.isEmpty(stringCoordinates)) {
			return stringCoordinates;
		}

		String coordinateList = StringUtils.EMPTY;
		coordinateList = JsonUtils.mapObjectToJson(coordinates);

		stringCoordinates = coordinateList;

		return coordinateList;
	}

	public String getGeometryType() {
		return type;
	}

	@Override
	public String toString() {
		return "{ \"type\" : \"" + type + "\", \"coordinates\" : " + getCoordinatesAsString() + " }";
	}

}
