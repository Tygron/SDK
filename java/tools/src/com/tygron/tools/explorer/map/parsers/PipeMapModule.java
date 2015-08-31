package com.tygron.tools.explorer.map.parsers;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import com.tygron.pub.api.data.misc.GeometryObject;
import com.tygron.pub.api.enums.MapLink;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.StringUtils;
import com.tygron.tools.explorer.gui.MapPaneSubPane;
import com.tygron.tools.explorer.map.ShapeUtils;

public class PipeMapModule extends AbstractMapModule {

	private enum DisplayType {
			ALL,
			PARTIAL,
			CONNECTED;
	}

	private static final String CONNECTED_STATE = "connectedState";
	private static final String READY = "READY";
	private static final String NAME = "name";
	private static final String ID = "id";
	private static final String LOADIDS = "loadIDs";
	private static final String BUILDINGIDS = "buildingIDs";
	private static final String JUNCTIONID = "junctionID";
	private static final String STARTJUNCTIONID = "startJunctionID";
	private static final String ENDJUNCTIONID = "endJunctionID";
	private static final String CONNECTEDPIPEIDS = "connectedPipeIDs";

	private static final String OWNERID = "ownerID";
	private static final String POLYGONS = "polygons";
	private static final String POINT = "point";
	private static final String COLOR = "color";

	private static final String RGBA = "rgba";

	private static final String DEFAULT_TEXT_STYLE = "-fx-font-size:40; -fx-stroke-width:1.5px; -fx-stroke:black; -fx-fill:white;";

	private static void addRadioToGrid(GridPane grid, int column, int row, String string,
			DisplayType displayType, ToggleGroup toggleGroup, DisplayType selected) {
		RadioButton radio = new RadioButton();
		Text text = new Text(string);
		radio.setUserData(displayType);
		radio.setToggleGroup(toggleGroup);
		radio.setSelected(displayType.equals(selected));
		grid.add(radio, column, row);
		grid.add(text, column + 1, row);
	}

	private MapPaneSubPane pane = null;

	private DisplayType connectionsDisplayType = DisplayType.ALL;
	private DisplayType pipesDisplayType = DisplayType.ALL;

	private TextField textStyleField = new TextField();
	private CheckBox clusterIDBox = new CheckBox();
	private CheckBox junctionIDBox = new CheckBox();

	HashMap<Integer, Color> stakeholderColors = new HashMap<Integer, Color>();

	public PipeMapModule() {

	}

	private List<Integer> getConnectingPipes(List<Integer> activeJunctionsProvided,
			Map<Integer, Map<?, ?>> pipes, Map<Integer, Map<?, ?>> pipeJunctions) {
		List<Integer> activePipes = new LinkedList<Integer>();
		for (Map<?, ?> pipe : pipes.values()) {
			activePipes.add((Integer) pipe.get(ID));
		}

		if (activeJunctionsProvided.size() == 0) {
			return activePipes;
		}

		for (Map<?, ?> currentJunction : pipeJunctions.values()) {
			Map<?, ?> junction = currentJunction;
			Integer junctionID = (Integer) junction.get(ID);
			// Only itterate over the non-active junctions
			if (activeJunctionsProvided.contains(junctionID)) {
				continue;
			}

			List<Integer> connectedPipes = new LinkedList<Integer>();
			connectedPipes.addAll((List<Integer>) junction.get(CONNECTEDPIPEIDS));
			connectedPipes.retainAll(activePipes);
			int size = connectedPipes.size();

			while (size == 1) {

				Integer pipeID = connectedPipes.get(0);
				Map<?, ?> pipe = pipes.get(pipeID);
				activePipes.remove(pipeID);
				Integer newJunctionID = (Integer) pipe.get(STARTJUNCTIONID);
				if (junctionID == newJunctionID) {
					newJunctionID = (Integer) pipe.get(ENDJUNCTIONID);
				}
				junctionID = newJunctionID;
				junction = pipeJunctions.get(junctionID);

				connectedPipes = new LinkedList<Integer>();
				connectedPipes.addAll((List<Integer>) junction.get(CONNECTEDPIPEIDS));
				connectedPipes.retainAll(activePipes);
				size = connectedPipes.size();
			}
		}

		return activePipes;
	}

	@Override
	public String getName() {
		return "Pipe map";
	}

	@Override
	public MapPaneSubPane getPane() {
		if (pane != null) {
			return pane;
		}

		pane = new MapPaneSubPane();

		VBox verticalBox = new VBox();
		verticalBox.setFillWidth(true);
		pane.fill(verticalBox, 0);

		GridPane settingsPane = new GridPane();

		settingsPane.setAlignment(Pos.CENTER);
		settingsPane.setHgap(10);
		settingsPane.setVgap(10);
		settingsPane.setPadding(new Insets(0, 10, 10, 10));

		Text connectionsText = new Text("Connections");
		Text pipesText = new Text("Pipes");

		settingsPane.add(connectionsText, 1, 1, 2, 1);
		settingsPane.add(pipesText, 3, 1, 2, 1);

		ToggleGroup connectionsGroup = new ToggleGroup();
		ToggleGroup pipesGroup = new ToggleGroup();

		int column = 1;
		int row = 2;
		addRadioToGrid(settingsPane, column, row++, "All", DisplayType.ALL, connectionsGroup,
				connectionsDisplayType);
		addRadioToGrid(settingsPane, column, row++, "Partial", DisplayType.PARTIAL, connectionsGroup,
				connectionsDisplayType);
		addRadioToGrid(settingsPane, column, row++, "Connected", DisplayType.CONNECTED, connectionsGroup,
				connectionsDisplayType);
		column = 3;
		row = 2;
		addRadioToGrid(settingsPane, column, row++, "All", DisplayType.ALL, pipesGroup, pipesDisplayType);
		addRadioToGrid(settingsPane, column, row++, "Partial", DisplayType.PARTIAL, pipesGroup,
				pipesDisplayType);
		addRadioToGrid(settingsPane, column, row++, "Connected", DisplayType.CONNECTED, pipesGroup,
				pipesDisplayType);

		textStyleField.setPrefWidth(300);
		textStyleField.setText(DEFAULT_TEXT_STYLE);

		Button resetStyle = new Button("Reset");
		resetStyle.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						textStyleField.setText(DEFAULT_TEXT_STYLE);
					}
				}.start();

			}
		});
		clusterIDBox.setSelected(true);
		junctionIDBox.setSelected(false);

		column = 6;
		row = 2;
		settingsPane.add(new Text("Text style:"), column - 1, row, 1, 1);
		settingsPane.add(textStyleField, column, row, 2, 1);
		settingsPane.add(resetStyle, column + 2, row++, 1, 1);
		settingsPane.add(clusterIDBox, column, row, 1, 1);
		settingsPane.add(new Text("Cluster IDs"), column + 1, row++, 1, 1);
		settingsPane.add(junctionIDBox, column, row, 1, 1);
		settingsPane.add(new Text("Junction IDs"), column + 1, row++, 1, 1);

		ChangeListener<Toggle> changeListener = new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle) {
				if (newToggle == null) {
					return;
				}

				DisplayType selectedSize = (DisplayType) newToggle.getUserData();

				if (newToggle.getToggleGroup().equals(connectionsGroup)) {
					connectionsDisplayType = selectedSize;
				} else if (newToggle.getToggleGroup().equals(pipesGroup)) {
					pipesDisplayType = selectedSize;
				}
			}
		};

		connectionsGroup.selectedToggleProperty().addListener(changeListener);
		pipesGroup.selectedToggleProperty().addListener(changeListener);

		Button render = new Button("Render");
		render.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						render();
					}
				}.start();

			}
		});
		Button clear = new Button("Clear");
		clear.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						getRenderManager().displayUserDefinedPolygon((Collection<Shape>) null);
					}
				});
			}
		});

		settingsPane.add(render, 0, 1);
		settingsPane.add(clear, 0, 2);

		verticalBox.getChildren().addAll(settingsPane);
		pane.getChildren().add(verticalBox);
		return pane;
	}

	private Collection<Shape> getShapesBuildings() {
		LinkedList<Shape> shapes = new LinkedList<Shape>();

		Map<Integer, Map<?, ?>> pipeClusters = getCommunicator().getData(MapLink.PIPE_CLUSTERS);
		Map<Integer, Map<?, ?>> pipeLoads = getCommunicator().getData(MapLink.PIPE_LOADS);
		Map<Integer, Map<?, ?>> buildings = getCommunicator().getData(MapLink.BUILDINGS);

		Color stakeholderColor = Color.TRANSPARENT;

		for (Map<?, ?> cluster : pipeClusters.values()) {
			if ((!cluster.get(CONNECTED_STATE).equals(READY))
					&& connectionsDisplayType.equals(DisplayType.CONNECTED)) {
				continue;
			}

			Shape clusterShape = null;

			int ownerID = StringUtils.NOTHING;
			try {
				ownerID = (Integer) cluster.get(OWNERID);
				stakeholderColor = getStakeholderColor(ownerID);
			} catch (Exception e) {
				Log.warning("Stakeholder " + cluster.get(OWNERID) + " was not parseable");
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
					Log.info("LoadID " + loadID + " has " + buildingIDs.size() + " buildings.");
				}
				for (Integer buildingID : buildingIDs) {
					Map<?, ?> building = buildings.get(buildingID);
					if (building == null) {
						continue;
					}
					Shape newShape = null;
					try {
						newShape = ShapeUtils.createPolygon(GeometryObject
								.getGeometryObject((Map<String, ?>) building.get(POLYGONS)));
					} catch (Exception e) {
						Log.warning("Building " + buildingID + "'s " + POLYGONS + " not parsable.");
						continue;
					}
					if (newShape != null) {
						try {
							Color currentColor = stakeholderColor;
							newShape.setFill(currentColor);
							if ((!cluster.get(CONNECTED_STATE).equals(READY))
									&& connectionsDisplayType.equals(DisplayType.PARTIAL)) {
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

			if (clusterIDBox.isSelected()) {
				// Create text here
				Text shapeText = new Text(((String) cluster.get(NAME)).substring(4));
				shapeText.setTranslateX((clusterShape.getLayoutBounds().getMinX() + clusterShape
						.getLayoutBounds().getMaxX()) / 2);
				shapeText.setTranslateY((clusterShape.getLayoutBounds().getMinY() + clusterShape
						.getLayoutBounds().getMaxY()) / 2);

				// shapeText.setScaleX(0.5);
				// shapeText.setScaleY(0.5);

				shapeText.setStyle(textStyleField.getText());

				shapes.addLast(shapeText);
			}

			// shapeText.setStyle("-fx-font-size:40; -fx-stroke-width:1.5px; -fx-stroke:black; -fx-fill:white;");

			// shapeText.setStroke(Color.BLACK);
			// shapeText.setFill(Color.WHITE);
		}
		return shapes;
	}

	private List<Shape> getShapesPipes() {
		LinkedList<Shape> shapes = new LinkedList<Shape>();
		LinkedList<Integer> writtenJunctions = new LinkedList<Integer>();

		Map<Integer, Map<?, ?>> pipeJunctions = getCommunicator().getData(MapLink.PIPE_JUNCTIONS);
		Map<Integer, Map<?, ?>> pipes = getCommunicator().getData(MapLink.PIPES);

		List<Integer> activeJunctions = new LinkedList<Integer>();
		if (pipesDisplayType.equals(DisplayType.ALL)) {
			activeJunctions.addAll(pipeJunctions.keySet());
		} else {
			Map<Integer, Map<?, ?>> pipeClusters = getCommunicator().getData(MapLink.PIPE_CLUSTERS);
			Map<Integer, Map<?, ?>> pipeLoads = getCommunicator().getData(MapLink.PIPE_LOADS);

			for (Map<?, ?> cluster : pipeClusters.values()) {
				if (!cluster.get(CONNECTED_STATE).equals(READY)) {
					continue;
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

					Integer junctionID = (Integer) load.get(JUNCTIONID);
					if (junctionID == null) {
						Log.warning("Load " + load.get(ID) + " " + JUNCTIONID + " not parsable.");
						continue;
					}
					activeJunctions.add(junctionID);
				}
			}
		}

		Collection<Integer> activePipes = getConnectingPipes(activeJunctions, pipes, pipeJunctions);

		for (Map<?, ?> pipe : pipes.values()) {
			// Map<?, ?> pipe = pipes.get(pipeID);
			Integer pipeID = (Integer) pipe.get(ID);
			Integer startJunctionID = (Integer) pipe.get(STARTJUNCTIONID);
			Integer endJunctionID = (Integer) pipe.get(ENDJUNCTIONID);
			if (startJunctionID == null || endJunctionID == null) {
				continue;
			}

			Line line = new Line();
			line.setStroke(Color.BLACK);
			line.setStrokeWidth(10.0);
			if (!activePipes.contains(pipeID)) {
				if (pipesDisplayType.equals(DisplayType.CONNECTED)) {
					continue;
				}
				if (pipesDisplayType.equals(DisplayType.PARTIAL)) {
					line.setOpacity(0.5);
					line.setStrokeWidth(line.getStrokeWidth() / 2);
				}
			}

			try {
				String[] start = pipeJunctions.get(startJunctionID).get(POINT).toString().split("[\\s ()]");
				String[] end = pipeJunctions.get(endJunctionID).get(POINT).toString().split("[\\s ()]");
				line.setStartX(Double.parseDouble(start[2]));
				line.setStartY(Double.parseDouble(start[3]));
				line.setEndX(Double.parseDouble(end[2]));
				line.setEndY(Double.parseDouble(end[3]));

				if (junctionIDBox.isSelected()) {
					if (!writtenJunctions.contains(startJunctionID)) {
						Text junctionText = new Text(startJunctionID.toString());
						junctionText.setTranslateX(line.getStartX());
						junctionText.setTranslateY(line.getStartY());
						junctionText.setStyle(textStyleField.getText());

						// junctionText.setScaleX(0.5);
						// junctionText.setScaleY(0.5);

						shapes.addLast(junctionText);
					}
					if (!writtenJunctions.contains(endJunctionID)) {
						Text junctionText = new Text(endJunctionID.toString());
						junctionText.setTranslateX(line.getEndX());
						junctionText.setTranslateY(line.getEndY());
						junctionText.setStyle(textStyleField.getText());

						// junctionText.setScaleX(0.5);
						// junctionText.setScaleY(0.5);

						shapes.addLast(junctionText);
					}
				}
			} catch (Exception e) {
				Log.warning("Failed to parse start and end points of pipe " + pipe.get(ID) + ".");
				continue;
			}
			shapes.addFirst(line);
		}

		return shapes;
	}

	private Color getStakeholderColor(final int stakeholderID) {
		try {
			if (!stakeholderColors.containsKey(stakeholderID)) {
				Map<Integer, Map<?, ?>> stakeholders = getCommunicator().getData(MapLink.STAKEHOLDERS);
				Map<?, ?> stakeholder = stakeholders.get(stakeholderID);
				stakeholderColors.put(stakeholderID,
						ShapeUtils.intToColor(((Map<String, Integer>) stakeholder.get(COLOR)).get(RGBA)));
			}

		} catch (Exception e) {
			Log.warning("Stakeholder " + stakeholderID + " did not have a parseable color");
			stakeholderColors.put(stakeholderID, Color.BLACK);
		}
		return stakeholderColors.get(stakeholderID);
	}

	@Override
	public boolean isFunctional() {
		Map<Integer, Map<?, ?>> pipeClusters = getCommunicator().getData(MapLink.PIPE_CLUSTERS);
		Map<Integer, Map<?, ?>> pipeLoads = getCommunicator().getData(MapLink.PIPE_LOADS);
		Map<Integer, Map<?, ?>> pipeJunctions = getCommunicator().getData(MapLink.PIPE_JUNCTIONS);
		Map<Integer, Map<?, ?>> pipes = getCommunicator().getData(MapLink.PIPES);
		Map<Integer, Map<?, ?>> buildings = getCommunicator().getData(MapLink.BUILDINGS);
		Map<Integer, Map<?, ?>> stakeholders = getCommunicator().getData(MapLink.STAKEHOLDERS);

		return !(pipeClusters == null || pipeLoads == null || pipeJunctions == null || pipes == null
				|| buildings == null || stakeholders == null);
	}

	private void render() {
		List<Shape> shapes = getShapesPipes();
		shapes.addAll(getShapesBuildings());

		Collections.sort(shapes, new Comparator<Shape>() {
			@Override
			public int compare(Shape shape0, Shape shape1) {
				if (shape0 instanceof Text == shape1 instanceof Text) {
					return 0;
				}
				return shape0 instanceof Text ? 1 : -1;
			}

		});

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				getRenderManager().displayUserDefinedPolygon(shapes);
			}
		});
	}

}
