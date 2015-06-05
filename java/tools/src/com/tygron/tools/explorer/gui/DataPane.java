package com.tygron.tools.explorer.gui;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.StringUtils;
import com.tygron.tools.explorer.logic.ExplorerCommunicator;

public class DataPane extends GameExplorerSubPane {

	private class KeyValueCell extends ListCell<Entry<?, ?>> {

		@Override
		public void updateItem(Entry<?, ?> item, boolean empty) {
			super.updateItem(item, empty);

			if (empty || item == null) {
				setText(null);
				setGraphic(null);
			} else {
				setText(generateName(item.getKey(), item.getValue()));
			}
		}
	}

	private interface SelectionListener {
		public void updateSelection(Object selection);
	}

	private class SelectionPane extends Pane {
		private Object selection = null;
		private SelectionListener listener = null;

		public void changeSelection(Object selection) {
			this.selection = selection;
			if (listener != null) {
				listener.updateSelection(selection);
			}
		}

		public Object getSelection() {
			return this.selection;
		}

		public void setSelection(Object selection) {
			this.selection = selection;
		}

		public void setSelectionListener(SelectionListener listener) {
			this.listener = listener;
		}
	}

	private static String generateName(final Object key, final Object value) {
		String name = StringUtils.EMPTY;

		if (key != null && key instanceof String) {
			if (key instanceof String) {
				return (String) key;
			} else if (key instanceof Integer) {
				name = key.toString() + ": ";
			}
		}

		if (value != null) {
			if (value instanceof Map) {
				Object valueName = ((Map<?, ?>) value).get("name");
				if (valueName != null) {
					name += valueName.toString();
				}
			}
		}

		if (name.equals(StringUtils.EMPTY)) {
			name = "Nameless data";
		}
		return name;
	}

	private final ScrollPane scrollPane = new ScrollPane();
	private final HBox horizontalPane = new HBox();

	public DataPane(ExplorerCommunicator communicator) {
		super(communicator);

		GameExplorerPane.fill(scrollPane, 0.0);

		scrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
		scrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
		scrollPane.setContent(horizontalPane);
		scrollPane.setFitToHeight(true);

		// selectionPane.resize(scrollPane.getWidth(), scrollPane.getHeight());
		horizontalPane.minWidthProperty().bind(scrollPane.widthProperty());
		horizontalPane.maxHeightProperty().bind(scrollPane.heightProperty());
		// horizontalPane.prefWidthProperty().bind(scrollPane.widthProperty().subtract(1));

		horizontalPane.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue,
					Number newValue) {
				scrollPane.setHvalue(scrollPane.getHmax());
			}
		});
		this.getChildren().add(scrollPane);
	}

	public void displayData(Map<String, Map<Integer, Map<?, ?>>> map) {
		Node node = getDisplayMap(0, map);
		insertData(0, node);
	}

	public void displayDataUpdate(Map<String, Map<Integer, Map<?, ?>>> map) {
		List<Object> selections = getSelections();
		SelectionPane node = getDisplayMap(0, map);

		insertData(0, node, selections);
		boolean nextSelectionExists = true;
		for (int i = 0; nextSelectionExists; i++) {
			nextSelectionExists = setSelection(i, selections);
		}
	}

	private void displayEntry(final int index, final Entry<?, ?> entry) {
		SelectionPane newContent = null;
		setStatus("Selected: " + entry.getKey().toString());
		Object value = entry.getValue();

		if (value == null) {
			Log.info("Content is null");
			return;
		} else if (value instanceof Map<?, ?>) {
			newContent = getDisplayMap(index, (Map<?, ?>) value);
		} else {
			setStatus("Reached max depth. Content is: " + value.toString());
			Log.info("Reached max depth. Content is: " + value.toString());
		}
		if (newContent == null) {
			return;
		}
		newContent.prefHeightProperty().bind(heightProperty());

		insertData(index, newContent);
	}

	private SelectionPane getDisplayMap(final int index, final Map<? extends Object, ? extends Object> map) {
		final SelectionPane pane = new SelectionPane();

		final ListView<Entry<?, ?>> dataListView = new ListView<Entry<?, ?>>();

		dataListView
				.setCellFactory(new Callback<ListView<Entry<?, ?>>, javafx.scene.control.ListCell<Entry<?, ?>>>() {
					@Override
					public ListCell<Entry<?, ?>> call(ListView<Entry<?, ?>> listView) {
						return new KeyValueCell();
					}
				});

		dataListView.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				Entry<?, ?> selectedEntry = dataListView.getSelectionModel().getSelectedItem();
				if (selectedEntry == null) {
					return;
				}
				String selection = StringUtils.EMPTY;

				if (selectedEntry.getKey() != null) {
					selection = selectedEntry.getKey().toString();
				}
				if (selectedEntry.toString().length() < 500 && selectedEntry.getValue() != null) {
					if (!selection.equals(StringUtils.EMPTY)) {
						selection += ": ";
					}
					selection += selectedEntry.getValue().toString();
				}
				if (selection.equals(StringUtils.EMPTY)) {
					selection = "Nameless";
				}
				Log.verbose("Clicked on " + selection);

				pane.setSelection(selectedEntry.getKey());
				displayEntry(index + 1, selectedEntry);
				getCommunicator().selectData(selectedEntry.getValue());
			}
		});

		dataListView.getItems().setAll(map.entrySet());

		java.util.Collections.sort(dataListView.getItems(), new java.util.Comparator<Entry<?, ?>>() {
			@Override
			public int compare(Entry<? extends Object, ? extends Object> o1,
					Entry<? extends Object, ? extends Object> o2) {
				int result = 0;
				result = generateName(o1.getKey(), o1.getValue()).compareTo(
						generateName(o2.getKey(), o2.getValue()));
				return result;
			}
		});

		pane.setSelectionListener(new SelectionListener() {
			@Override
			public void updateSelection(Object selection) {
				pane.setSelection(null);
				dataListView.getSelectionModel().clearSelection();
				for (Entry<?, ?> entry : dataListView.getItems()) {
					if (entry.getKey().equals(selection)) {
						dataListView.getSelectionModel().select(entry);
						pane.setSelection(selection);
						dataListView.scrollTo(entry);
						displayEntry(index + 1, entry);
						break;
					}
				}
			}
		});

		pane.minWidthProperty().bind(dataListView.minWidthProperty());
		pane.prefWidthProperty().bind(dataListView.prefWidthProperty());
		pane.maxWidthProperty().bind(dataListView.maxWidthProperty());

		dataListView.minHeightProperty().bind(pane.heightProperty());
		dataListView.maxHeightProperty().bind(pane.heightProperty());

		pane.getChildren().add(dataListView);

		return pane;
	}

	private List<Object> getSelections() {
		List<Object> selections = new LinkedList<Object>();
		for (Node node : horizontalPane.getChildren()) {
			if (!(node instanceof SelectionPane)) {
				Log.warning("Node encountered not of type " + SelectionPane.class.getSimpleName()
						+ ". Stopping retrieving selections...");
				return selections;
			}
			Object selection = ((SelectionPane) node).getSelection();
			if (selection != null) {
				selections.add(selection);
			}
		}
		return selections;
	}

	private void insertData(final int index, Node node) {
		insertData(index, node, null);
	}

	private void insertData(final int index, final Node node, List<Object> selections) {
		if (node == null) {
			return;
		}

		if (horizontalPane.getChildren().size() > index) {
			horizontalPane.getChildren().subList(index, horizontalPane.getChildren().size()).clear();
			horizontalPane.getChildren().add(index, node);
		} else {
			horizontalPane.getChildren().add(node);
		}

		scrollPane.setHvalue(scrollPane.getHmax());

		if (!(node instanceof SelectionPane)) {
			Log.warning(this.getClass().getSimpleName() + "'s child at index " + index
					+ " is not a SelectionPane.");
			return;
		}
	}

	private boolean setSelection(final int index, List<Object> selections) {
		if (selections == null) {
			return false;
		}
		if (selections.size() <= index) {
			return false;
		}
		if (selections.get(index) == null) {
			return false;
		}
		if (horizontalPane.getChildren().size() <= index) {
			return false;
		}

		SelectionPane selectionPane = (SelectionPane) horizontalPane.getChildren().get(index);
		selectionPane.changeSelection(selections.get(index));
		if (selectionPane.getSelection() == null) {
			return false;
		}
		return true;
	}
}
