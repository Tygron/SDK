package com.tygron.tools.explorer.map.parsers;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import com.tygron.pub.api.enums.MapLink;
import com.tygron.pub.utils.StringUtils;
import com.tygron.tools.explorer.gui.MapPaneSubPane;

public class RemoteMeasureModule extends AbstractMapModule {

	private MapPaneSubPane pane = null;

	public RemoteMeasureModule() {
	}

	@Override
	public String getName() {
		return "Admin measures";
	}

	@Override
	public MapPaneSubPane getPane() {
		if (pane != null) {
			selectStakeholder(5);
			// return pane;
		}

		pane = new MapPaneSubPane();

		Map<Integer, Map<?, ?>> measures = getCommunicator().getData(MapLink.MEASURES);

		int i = 0;
		GridPane grid = new GridPane();

		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(0, 10, 10, 10));

		Button cinematicButton = new Button("Cinematics");
		cinematicButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						Map<Integer, Map<?, ?>> cinematics = getCommunicator().getData(
								MapLink.CINEMATIC_DATAS);
						for (Map<?, ?> cinematic : cinematics.values()) {
							List<Map<String, Object>> keyPoints = (List<Map<String, Object>>) cinematic
									.get("keyPoints");
							for (int i = 0; i < keyPoints.size(); i++) {
								Map<String, Object> keyPoint = keyPoints.get(i);
								try {
									String description = (String) keyPoint.get("description");
									if (StringUtils.isEmpty(description)) {
										continue;
									}
									description.replaceAll("<.*>", "");
								} catch (ClassCastException e) {
								}
							}
						}
					}
				}.start();

			}
		});
		// grid.add(cinematicButton, 4, i);

		List<Map<?, ?>> orderedMeasures = new LinkedList<Map<?, ?>>();
		if (measures != null) {
			for (Map<?, ?> m : measures.values()) {
				try {
					if (((String) m.get("name")).startsWith("ADMIN")) {
						orderedMeasures.add(m);
					}
				} catch (Exception e) {
					this.setStatus("Failed to parse at least one measure.");
				}
			}
		}

		Collections.sort(orderedMeasures, new Comparator<Map<?, ?>>() {
			@Override
			public int compare(Map<?, ?> arg0, Map<?, ?> arg1) {
				try {
					return ((String) arg0.get("name")).compareTo((String) arg1.get("name"));
				} catch (Exception e) {
					setStatus("Failed to compare at least one measure.");
					return 0;
				}
			}

		});

		for (Map<?, ?> m : orderedMeasures) {
			try {
				i++;
				grid.add(new Text((String) m.get("name")), 0, i);
				Button measureActivate = new Button("Activate");
				measureActivate.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						new Thread() {
							@Override
							public void run() {
								measurePlan((Integer) m.get("id"));
							}
						}.start();

					}
				});
				grid.add(measureActivate, 1, i);

				Button measureDeactivate = new Button("Deactivate");
				measureDeactivate.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						new Thread() {
							@Override
							public void run() {
								measureCancel((Integer) m.get("id"));
							}
						}.start();

					}
				});
				grid.add(measureDeactivate, 2, i);

				Button measureForce = new Button("Force");
				measureForce.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						new Thread() {
							@Override
							public void run() {
								measureCancel((Integer) m.get("id"));
								measurePlan((Integer) m.get("id"));
							}
						}.start();

					}
				});
				grid.add(measureForce, 3, i);
				grid.add(measureDeactivate, 2, i);
			} catch (Exception e) {
				continue;
			}
		}
		pane.getChildren().add(grid);
		return pane;
	}

	@Override
	public boolean isFunctional() {
		return true;
	}

	private void measureCancel(int measureID) {
		getCommunicator().fireEvent("PlayerEventType/MEASURE_CANCEL_CONSTRUCTION/", Integer.toString(0),
				Integer.toString(measureID));
	}

	private void measurePlan(int measureID) {
		getCommunicator().fireEvent("PlayerEventType/MEASURE_PLAN_CONSTRUCTION/", Integer.toString(0),
				Integer.toString(measureID));
	}

	private void selectStakeholder(int stakeholderID) {
		getCommunicator().selectStakeholder(stakeholderID);
	}
}
