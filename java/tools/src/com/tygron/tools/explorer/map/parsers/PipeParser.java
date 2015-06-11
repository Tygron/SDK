package com.tygron.tools.explorer.map.parsers;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import com.tygron.pub.api.enums.MapLink;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.StringUtils;
import com.tygron.tools.explorer.logic.ExplorerCommunicator;
import com.tygron.tools.explorer.map.ShapeUtils;

public class PipeParser {

	private static final String CONNECTED_STATE = "connectedState";
	private static final String READY = "READY";
	private static final String NAME = "name";
	private static final String ID = "id";
	private static final String LOADIDS = "loadIDs";
	private static final String BUILDINGIDS = "buildingIDs";
	private static final String OWNERID = "ownerID";

	private static final String POLYGONS = "polygons";
	private static final String COLOR = "color";
	private static final String RGBA = "rgba";

	private ExplorerCommunicator communicator;

	/**
	 *
	 <pre>
	 *
	 * Button pipeParseButton = new Button(&quot;PipeParser&quot;);
	 * pipeParseButton.setOnAction(new EventHandler&lt;ActionEvent&gt;() {
	 * 	public void handle(ActionEvent event) {
	 * 		Platform.runLater(new Runnable() {
	 * 			public void run() {
	 * 				PipeParser parser = new PipeParser(getCommunicator());
	 * 				displayData(parser.getShapes());
	 * 				// renderPolygon(polygonField.getText());
	 * 			}
	 * 		});
	 * 	}
	 * });
	 * verticalPane.getChildren().add(pipeParseButton);
	 * </pre>
	 *
	 *
	 */
	public PipeParser(ExplorerCommunicator communicator) {
		this.communicator = communicator;
	}

	private Collection<Shape> getBuildings() {
		LinkedList<Shape> shapes = new LinkedList<Shape>();

		Map<Integer, Map<?, ?>> pipeClusters = communicator.getData(MapLink.PIPE_CLUSTERS);
		Map<Integer, Map<?, ?>> pipeLoads = communicator.getData(MapLink.PIPE_LOADS);
		Map<Integer, Map<?, ?>> buildings = communicator.getData(MapLink.BUILDINGS);
		Map<Integer, Map<?, ?>> stakeholders = communicator.getData(MapLink.STAKEHOLDERS);

		HashMap<Integer, Color> stakeholderColors = new HashMap<Integer, Color>();

		for (Map<?, ?> cluster : pipeClusters.values()) {
			// if (!cluster.get(CONNECTED_STATE).equals(READY)) {
			// continue;
			// }

			Shape clusterShape = null;

			int ownerID = StringUtils.NOTHING;
			try {
				ownerID = (Integer) cluster.get(OWNERID);
				if (!stakeholderColors.containsKey(ownerID)) {
					Map<?, ?> stakeholder = stakeholders.get(ownerID);

					double red = 0.0;
					double blue = 0.0;
					double green = 0.0;
					double alpha = 1.0;
					switch (ownerID) {
						case 2:
							blue = 112.0 / 255.0;
							red = 167.0 / 255.0;
							green = 65.0 / 255.0;
							stakeholderColors.put(ownerID, new Color(red, green, blue, alpha));
							break;
						case 7:
							blue = 159.0 / 255.0;
							red = 35.0 / 255.0;
							green = 129.0 / 255.0;
							stakeholderColors.put(ownerID, new Color(red, green, blue, alpha));
							break;
						case 8:
							blue = 61.0 / 255.0;
							red = 96.0 / 255.0;
							green = 173.0 / 255.0;
							stakeholderColors.put(ownerID, new Color(red, green, blue, alpha));
							break;
						default:
							stakeholderColors.put(ownerID, ShapeUtils
									.intToColor(((Map<String, Integer>) stakeholder.get(COLOR)).get(RGBA)));
							break;
					}

				}
			} catch (Exception e) {
				Log.warning("Stakeholder " + cluster.get(OWNERID) + " did not have a parseable color");
			}

			List<Integer> loadIDs = null;
			try {
				loadIDs = (List<Integer>) cluster.get(LOADIDS);
			} catch (ClassCastException e) {
				Log.warning("Cluster " + cluster.get(ID) + " " + LOADIDS + " not parsable.");
				continue;
			}

			for (Integer loadID : loadIDs) {
				Map<?, ?> load = pipeLoads.get(loadID);
				if (load == null) {
					continue;
				}

				List<Integer> buildingIDs = null;
				try {
					buildingIDs = (List<Integer>) load.get(BUILDINGIDS);
				} catch (ClassCastException e) {
					Log.warning("Load " + loadID + " " + BUILDINGIDS + " not parsable.");
					continue;
				}
				if (buildingIDs.size() > 1) {
					Log.info("LoadID " + loadID + "has " + buildingIDs.size() + " buildings.");
				}
				for (Integer buildingID : buildingIDs) {
					Map<?, ?> building = buildings.get(buildingID);
					if (building == null) {
						continue;
					}
					Shape newShape = null;
					try {
						newShape = ShapeUtils.createPolygon((String) building.get(POLYGONS));
					} catch (Exception e) {
						Log.warning("Building " + buildingID + "'s " + POLYGONS + " not parsable.");
						continue;
					}
					if (newShape != null) {
						try {
							Color currentColor = stakeholderColors.get(ownerID);
							newShape.setFill(currentColor);
							if (!cluster.get(CONNECTED_STATE).equals(READY)) {
								newShape.setFill(new Color(currentColor.getRed(), currentColor.getGreen(),
										currentColor.getBlue(), 0.5));
								newShape.setStroke(Color.TRANSPARENT);
							}
						} catch (Exception e) {
							Log.warning("Something failed with coloring for building " + buildingID);
						}
						shapes.addFirst(newShape);

						if (clusterShape == null) {
							clusterShape = newShape;
						} else {
							clusterShape = Shape.union(clusterShape, newShape);
						}
					}
				}
			}

			if (clusterShape == null) {
				continue;
			}
			// Create text here
			Text shapeText = new Text(((String) cluster.get(NAME)).substring(4));
			shapeText.setTranslateX((clusterShape.getLayoutBounds().getMinX() + clusterShape
					.getLayoutBounds().getMaxX()) / 2);
			shapeText.setTranslateY((clusterShape.getLayoutBounds().getMinY() + clusterShape
					.getLayoutBounds().getMaxY()) / 2);

			shapeText.setScaleX(3.2);
			shapeText.setScaleY(3.2);

			shapes.addLast(shapeText);
		}

		// Log.info("Drew buildings of " + stakeholderColors.size() + " stakeholders.");

		return shapes;
	}

	protected final ExplorerCommunicator getCommunicator() {
		return this.communicator;
	}

	public Collection<Shape> getShapes() {
		Collection<Shape> shapes = new LinkedList<Shape>();
		shapes.addAll(getBuildings());
		return shapes;
	}
}
