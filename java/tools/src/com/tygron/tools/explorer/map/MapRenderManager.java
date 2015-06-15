package com.tygron.tools.explorer.map;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.StringUtils;

public class MapRenderManager {

	public interface RenderManagerListener {
		public void processRenderUpdate();
	}

	private static final int DEFAULT_RENDER_PANE_SIZE = 400;

	private static final String POLYGONS = "polygons";
	private final Pane mapRenderPane;

	private SimpleIntegerProperty mapSize = new SimpleIntegerProperty();

	private Pane dataPolygonGroup = new Pane();
	private Pane definedPolygonGroup = new Pane();

	private HashMap<String, Collection<Shape>> shapeCache = new HashMap<String, Collection<Shape>>();
	private HashMap<String, String> mapLinkHash = new HashMap<String, String>();
	private HashMap<Pane, String> activeHash = new HashMap<Pane, String>();

	private Collection<RenderManagerListener> listeners = new LinkedList<RenderManagerListener>();

	public MapRenderManager(Pane mapRenderPane) {
		this.mapRenderPane = mapRenderPane;

		setRenderSize(DEFAULT_RENDER_PANE_SIZE);

		addPolygonGroupToRenderPane(dataPolygonGroup);
		addPolygonGroupToRenderPane(definedPolygonGroup);
	}

	private void addPolygonGroupToRenderPane(Pane polygonGroup) {
		resetPolygonGroup(polygonGroup);

		Scale scale = new Scale();
		scale.setPivotX(0.0);
		scale.setPivotY(0.0);
		scale.xProperty().bind(mapRenderPane.widthProperty().divide(mapSize));
		scale.yProperty().bind(mapRenderPane.heightProperty().divide(mapSize));
		polygonGroup.getTransforms().add(scale);

		polygonGroup.setStyle("-fx-background-color: rgba(0, 0, 0, 0.0);");

		polygonGroup.visibleProperty().bind(this.mapSize.greaterThan(0));
		mapRenderPane.getChildren().add(polygonGroup);
		polygonGroup.toFront();
	}

	public void addRenderManagerListener(RenderManagerListener listener) {
		if (listener == null) {
			return;
		}
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	private void alertListeners() {
		for (RenderManagerListener listener : listeners) {
			listener.processRenderUpdate();
		}
	}

	private void alertRenderedImage() {
		alertListeners();
	}

	private boolean cacheIsCurrent(final String mapLink, final String hashCode) {
		if (StringUtils.isEmpty(hashCode) || StringUtils.isEmpty(mapLink)) {
			return false;
		}
		if (!shapeCache.containsKey(hashCode)) {
			return false;
		}
		return hashCode.equals(mapLinkHash.get(mapLink));
	}

	private void cacheUpdate(final String mapLink, final String hashCode, final Collection<Shape> shapes) {
		if (StringUtils.isEmpty(hashCode) || StringUtils.isEmpty(mapLink)) {
			return;
		}
		mapLinkHash.put(mapLink, hashCode);
		shapeCache.put(hashCode, shapes);
	}

	private Image createImageOfRenderedMap(int imageSize) {
		double renderMapSize = getRenderSize();

		WritableImage writableImage = new WritableImage(imageSize, imageSize);
		SnapshotParameters parameters = new SnapshotParameters();
		parameters.setTransform(Transform.scale((imageSize / renderMapSize), imageSize / renderMapSize));
		parameters.setViewport(new Rectangle2D(-imageSize, -imageSize, renderMapSize, renderMapSize));

		Image image = mapRenderPane.snapshot(parameters, writableImage);
		Log.info("Creating image of rendered map of size: " + imageSize + ", with rendered map at size "
				+ getRenderSize());

		return image;
	}

	private Collection<Shape> createPolygonsFromData(Map<String, ? extends Object> data) {
		boolean alerted = false;

		final Collection<Shape> newShapes = new LinkedList<Shape>();

		for (Object o : data.values()) {
			if (!(o instanceof Map<?, ?>)) {
				continue;
			}
			Map<?, ?> subMap = (Map<?, ?>) o;
			if (!subMap.containsKey(POLYGONS)) {
				continue;
			}
			if (!alerted) {
				// getCommunicator().setStatus("Creating data polygons");
				alerted = true;
			}
			Shape polygon = ShapeUtils.createPolygon((String) subMap.get(POLYGONS));
			if (polygon == null) {
				continue;
			}
			newShapes.add(polygon);
		}
		// getCommunicator().setStatus("Data polygons created");
		return newShapes;
	}

	public void displayData(final String mapLink, final Object dataObject) {
		// shapeCache.clear();
		if (!(dataObject instanceof Map<?, ?>)) {
			return;
		}

		final String hashCode = generateHash(dataObject);
		hashSetActive(dataPolygonGroup, hashCode);

		final Map<String, ? extends Object> data = (Map<String, ? extends Object>) dataObject;

		Thread shapeThread = new Thread() {
			@Override
			public void run() {
				final Collection<Shape> newShapes;

				if (data.containsKey(POLYGONS)) {
					newShapes = new LinkedList<Shape>();
					Shape newShape = ShapeUtils.createPolygon((String) data.get(POLYGONS));
					if (newShape != null) {
						newShapes.add(newShape);
					}
				} else {
					if (!cacheIsCurrent(mapLink, hashCode)) {
						newShapes = createPolygonsFromData(data);
						cacheUpdate(mapLink, hashCode, newShapes);
					} else {
						newShapes = new LinkedList<Shape>();
						newShapes.addAll(shapeCache.get(hashCode));
					}
				}

				renderShapesToRenderPane(dataPolygonGroup, newShapes, hashCode);
			}
		};

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				resetPolygonGroup(dataPolygonGroup);
				shapeThread.start();
			}
		});
		shapeThread.setDaemon(true);
	}

	public void displayUserDefinedPolygon(Collection<Shape> shapes) {
		resetPolygonGroup(definedPolygonGroup);
		if (shapes != null) {
			definedPolygonGroup.getChildren().addAll(shapes);
		}
		alertRenderedImage();
	}

	public void displayUserDefinedPolygon(String polygonString) {
		resetPolygonGroup(definedPolygonGroup);
		Shape renderedPolygon = ShapeUtils.createPolygon(polygonString);
		if (renderedPolygon != null) {
			renderedPolygon.setFill(Color.CRIMSON);
			definedPolygonGroup.getChildren().add(renderedPolygon);
		}

		alertRenderedImage();
	}

	private String generateHash(final Object data) {
		return Integer.toString(data.hashCode());
	}

	public int getMapSize() {
		return mapSize.get();
	}

	public Image getRenderedImage() {
		return createImageOfRenderedMap((int) getRenderSize());
	}

	public Image getRenderedImage(int imageSize) {
		return createImageOfRenderedMap(imageSize);
	}

	public double getRenderSize() {
		return mapRenderPane.getWidth();
	}

	private boolean hashIsActive(final Pane polygonGroup, final String hashCode) {
		if (StringUtils.isEmpty(hashCode)) {
			return true;
		}
		return hashCode.equals(activeHash.get(polygonGroup));
	}

	private void hashSetActive(final Pane polygonGroup, final String hashCode) {
		if (hashCode != null && polygonGroup != null) {
			activeHash.put(polygonGroup, hashCode);
		}
	}

	private void removePolygonGroupFromRenderPane(Pane polygonGroup) {
		mapRenderPane.getChildren().remove(polygonGroup);
		polygonGroup.getTransforms().clear();
		polygonGroup.visibleProperty().unbind();
	}

	public void removeRenderManagerListener(RenderManagerListener listener) {
		if (listener == null) {
			return;
		}
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	private void renderShapesToRenderPane(final Pane polygonGroup, final Collection<Shape> shapes,
			final String hashCode) {
		// hashSetActive(polygonGroup, hashCode);

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (!hashIsActive(polygonGroup, hashCode)) {
					return;
				}

				resetPolygonGroup(polygonGroup);
				polygonGroup.getChildren().addAll(shapes);
				alertRenderedImage();
			}
		});
	}

	private void resetPolygonGroup(Pane polygonGroup) {
		polygonGroup.getChildren().clear();
		Polygon mapSizePolygon = new Polygon();

		Double[] coordinates = new Double[] { 0.0, 0.0, 0.0, new Double(mapSize.get()),
				new Double(mapSize.get()), new Double(mapSize.get()), new Double(mapSize.get()), 0.0 };

		mapSizePolygon.getPoints().addAll(coordinates);

		mapSizePolygon.setStrokeWidth(0);
		mapSizePolygon.setStroke(Color.TRANSPARENT);
		mapSizePolygon.setFill(Color.TRANSPARENT);

		Polygon clipPolygon = new Polygon();
		clipPolygon.getPoints().addAll(coordinates);
		polygonGroup.setClip(clipPolygon);

		polygonGroup.getChildren().add(mapSizePolygon);
	}

	public void setMapSize(int mapSize) {
		this.mapSize.set(mapSize);
		setRenderSize(mapSize);
	}

	private void setRenderSize(double newSize) {
		Log.info("Set renderer size to " + newSize);
		mapRenderPane.setMinSize(newSize, newSize);
		mapRenderPane.setMaxSize(newSize, newSize);
		mapRenderPane.layoutXProperty().bind(mapRenderPane.widthProperty().multiply(-1));
		mapRenderPane.layoutYProperty().bind(mapRenderPane.heightProperty().multiply(-1));
	}
}
