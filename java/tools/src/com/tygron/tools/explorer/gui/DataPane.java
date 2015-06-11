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

		private Callback<ListView<Entry<?, ?>>, javafx.scene.control.ListCell<Entry<?, ?>>> getKeyValueCellFactory() {
			return new Callback<ListView<Entry<?, ?>>, javafx.scene.control.ListCell<Entry<?, ?>>>() {
				@Override
				public ListCell<Entry<?, ?>> call(ListView<Entry<?, ?>> listView) {
					return new KeyValueCell();
				}
			};
		}

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
		private Object selectionKey = null;
		private Object selectionValue = null;
		private SelectionListener listener = null;

		public Object getSelectionKey() {
			return this.selectionKey;
		}

		public Object getSelectionValue() {
			return this.selectionValue;
		}

		public void setSelection(Object selectionKey, Object selectionValue) {
			this.selectionKey = selectionKey;
			this.selectionValue = selectionValue;
		}

		public void setSelectionAndUpdateListener(Object selection) {
			this.selectionKey = selection;
			if (listener != null) {
				listener.updateSelection(selection);
			}
		}

		public void setSelectionListener(SelectionListener listener) {
			this.listener = listener;
		}
	}

	private static String generateName(final Object key, final Object value) {
		String name = StringUtils.EMPTY;

		if (key != null) {
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

		if (StringUtils.isEmpty(name)) {
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

	/**
	 * The first display of data
	 */
	public void displayData(Map<String, Map<Integer, Map<?, ?>>> map) {
		Node node = getPaneWithMap(0, map);
		insertData(0, node);
	}

	/**
	 * The processing of data updates. It gets a list of current selections, rerenders the datalists, and then
	 * select the proper entry in each list if possible.
	 */
	public void displayDataUpdate(Map<String, Map<Integer, Map<?, ?>>> map) {
		List<Object> selections = getSelections();
		SelectionPane node = getPaneWithMap(0, map);

		insertData(0, node);
		boolean nextSelectionExists = true;
		for (int i = 0; nextSelectionExists; i++) {
			nextSelectionExists = setSelection(i, selections);
			if (!nextSelectionExists) {
				getCommunicator().selectDataByUpdate(getMapLink(), getSelection(i - 1, true));
			}
		}
	}

	/**
	 * Most (if not all) selectable data will be in the form of map entries.
	 */
	private void displayEntry(final int index, final Entry<?, ?> entry) {
		SelectionPane newContent = null;
		setStatus("Selected: " + entry.getKey().toString());
		Object value = entry.getValue();

		if (value == null) {
			Log.info("Content is null");
			return;
		} else if (value instanceof Map<?, ?>) {
			newContent = getPaneWithMap(index, (Map<?, ?>) value);
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

	private String getMapLink() {
		return (String) getSelection(0, false);
	}

	private SelectionPane getPaneWithMap(final int index, final Map<? extends Object, ? extends Object> map) {
		final SelectionPane pane = new SelectionPane();
		final ListView<Entry<?, ?>> dataListView = new ListView<Entry<?, ?>>();

		dataListView.setCellFactory(new KeyValueCell().getKeyValueCellFactory());

		dataListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Entry<?, ?> selectedEntry = dataListView.getSelectionModel().getSelectedItem();
				handleSelectedEntry(pane, selectedEntry, index);
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
			// What to do when the selection is changed with side effects.
			public void updateSelection(Object selection) {
				pane.setSelection(null, null);
				dataListView.getSelectionModel().clearSelection();
				for (Entry<?, ?> entry : dataListView.getItems()) {
					if (entry.getKey().equals(selection)) {
						dataListView.getSelectionModel().select(entry);
						pane.setSelection(selection, entry.getValue());
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

	private Object getSelection(final int index, boolean getValue) {
		Object selection = null;
		if (index < 0) {
			return selection;
		}
		if (horizontalPane.getChildren().size() <= index) {
			return selection;
		}
		Node node = horizontalPane.getChildren().get(index);
		selection = getValue ? ((SelectionPane) node).getSelectionValue() : ((SelectionPane) node)
				.getSelectionKey();
		return selection;
	}

	private List<Object> getSelections() {
		List<Object> selections = new LinkedList<Object>();
		for (Node node : horizontalPane.getChildren()) {
			if (!(node instanceof SelectionPane)) {
				Log.warning("Node encountered not of type " + SelectionPane.class.getSimpleName()
						+ ". Stopping retrieving selections...");
				return selections;
			}
			Object selection = ((SelectionPane) node).getSelectionKey();
			if (selection != null) {
				selections.add(selection);
			}
		}
		return selections;
	}

	private void handleSelectedEntry(final SelectionPane pane, final Entry<?, ?> selectedEntry,
			final int index) {
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

		pane.setSelection(selectedEntry.getKey(), selectedEntry.getValue());
		displayEntry(index + 1, selectedEntry);
		getCommunicator().selectData(null, selectedEntry.getValue());
	}

	/**
	 * Insert the new node into the scene graph
	 */
	private void insertData(final int index, final Node node) {
		// TODO: insertData should be run on the javafx thread
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

	/**
	 * Set the entry at a given index. This will trigger the next SelectionPane to be recreated.
	 */
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
		selectionPane.setSelectionAndUpdateListener(selections.get(index));

		Log.info("Selection " + index + " set to " + selections.get(index));
		if (selectionPane.getSelectionKey() == null) {
			return false;
		}
		return true;
	}
}
