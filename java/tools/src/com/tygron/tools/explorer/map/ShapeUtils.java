package com.tygron.tools.explorer.map;

import java.util.List;
import java.util.Map;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import com.tygron.pub.api.data.misc.GeometryObject;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.StringUtils;

public class ShapeUtils {

	public static Shape createPolygon(GeometryObject coordinates) {

		if (!coordinates.getGeometryType().equals("MultiPolygon")) {
			return null;
		}
		List<List<List<List<Double>>>> fullpolygonList = (List<List<List<List<Double>>>>) coordinates
				.getCoordinates();

		Shape mainShape = null;

		for (List<List<List<Double>>> singleOutline : fullpolygonList) {
			boolean add = true;
			for (List<List<Double>> singlePolygon : singleOutline) {
				Polygon newPolygon = new Polygon();
				for (List<Double> singleCoordinate : singlePolygon) {
					if (singleCoordinate.size() != 2) {
						Log.warning("Invalid coordinate: " + singleCoordinate);
						continue;
					}
					newPolygon.getPoints().addAll(singleCoordinate.get(0), singleCoordinate.get(1));
				}
				if (add) {
					mainShape = ((mainShape == null) ? mainShape = newPolygon : Shape.union(mainShape,
							newPolygon));
				} else {
					mainShape = Shape.subtract(mainShape, newPolygon);
				}
				add = false;
			}
		}
		if (mainShape != null) {
			// mainShape.setFill(Color.CYAN);
			mainShape.setFill(Color.TRANSPARENT);
			mainShape.setStroke(Color.BLACK);
		}

		return mainShape;
	}

	public static Shape createPolygon(Map<String, ?> shapeMap) {
		return createPolygon(GeometryObject.getGeometryObject(shapeMap));
	}

	public static Shape createPolygon(String polygonString) {
		// TODO: This should be done by a library such as Java Topology Suite
		if (polygonString == null) {
			return null;
		}

		String word = StringUtils.EMPTY;
		Shape mainShape = null;
		int level = 0;
		boolean add = true;
		StringBuilder currentCoordinatesBuilder = new StringBuilder();

		for (int i = 0; i < polygonString.length(); i++) {
			char c = polygonString.charAt(i);
			if (Character.isAlphabetic(c)) {
				word += c;
				continue;
			}
			switch (word) {
				case "MULTIPOLYGON":
					break;
				case "POLYGON":
					level++;
					break;
				default:
					break;
			}
			word = StringUtils.EMPTY;

			if (Character.isDigit(c)) {
				// The highest level (3) is the only level where numbers exist. When we encounter numbers, it
				// must be on level 3.
				level = 3;
				currentCoordinatesBuilder.append(c);
				continue;
			} else if (Character.isWhitespace(c) && currentCoordinatesBuilder.length() > 0) {
				currentCoordinatesBuilder.append(c);
				continue;
			} else if (c == '.' && currentCoordinatesBuilder.length() > 0) {
				currentCoordinatesBuilder.append(c);
				continue;
			} else if (c == ',') {
				if (level == 1) {
					add = true;
				}
				if (level == 2) {
					add = false;
				}
				continue;
			} else if (c == '(') {
				level++;
				continue;
			} else if (c == ')') {
				level--;
				if (currentCoordinatesBuilder.length() > 0) {
					Polygon newPolygon = new Polygon();
					String[] coordinates = currentCoordinatesBuilder.toString().split("[\\s:]");
					for (String s : coordinates) {
						try {
							newPolygon.getPoints().addAll(Double.parseDouble(s));
						} catch (Exception e) {
							Log.exception(e, "Failed to parse: " + s);
						}
					}
					if (add) {
						if (mainShape != null) {
							mainShape = Shape.union(mainShape, newPolygon);
						} else {
							mainShape = newPolygon;
						}
					} else {
						mainShape = Shape.subtract(mainShape, newPolygon);
					}
					currentCoordinatesBuilder = new StringBuilder();
				}
			}
		}
		if (mainShape != null) {
			// mainShape.setFill(Color.CYAN);
			mainShape.setFill(Color.TRANSPARENT);
			mainShape.setStroke(Color.BLACK);
		}

		return mainShape;
	}

	public static Color intToColor(int colorInteger) {
		String colorString = Integer.toHexString(colorInteger);
		int base = 255;
		double boost = 0.25;

		double red = Math.min((new Double(Integer.valueOf(colorString.substring(2, 4), 16)) / base) + boost
				+ boost, 1.0);
		double green = Math.min(
				(new Double(Integer.valueOf(colorString.substring(4, 6), 16)) / base) + boost, 1.0);
		double blue = Math.min((new Double(Integer.valueOf(colorString.substring(6, 8), 16)) / base) + boost,
				1.0);
		double alpha = Math.min(
				(new Double(Integer.valueOf(colorString.substring(0, 2), 16)) / base) + boost, 1.0);
		return new Color(red, green, blue, alpha);
	}
}
