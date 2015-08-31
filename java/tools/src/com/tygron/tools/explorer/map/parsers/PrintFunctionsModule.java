package com.tygron.tools.explorer.map.parsers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import com.tygron.pub.api.data.item.AbstractFunctionBase.FunctionCategory;
import com.tygron.pub.api.data.item.Function;
import com.tygron.pub.api.data.item.Item;
import com.tygron.pub.api.enums.MapLink;
import com.tygron.pub.utils.DataUtils;
import com.tygron.pub.utils.StringUtils;
import com.tygron.tools.explorer.gui.MapPaneSubPane;

public class PrintFunctionsModule extends AbstractMapModule {

	private TextField fileNameField = new TextField();

	private MapPaneSubPane pane = null;

	public PrintFunctionsModule() {
	}

	@Override
	public String getName() {
		return "Print functions to file.";
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

		HBox fileDetails = new HBox();
		fileDetails.setPadding(new Insets(10, 10, 0, 10));
		AnchorPane.setLeftAnchor(fileDetails, 0.0);
		AnchorPane.setRightAnchor(fileDetails, 0.0);

		Text text = new Text("File name: ");
		HBox.setHgrow(text, Priority.NEVER);
		fileNameField.setText("Functions");
		HBox.setHgrow(fileNameField, Priority.ALWAYS);
		Button button = new Button("Save file");
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						saveMapHandle(fileNameField);
					}
				}.start();

			}
		});
		HBox.setHgrow(button, Priority.NEVER);

		fileDetails.getChildren().addAll(text, fileNameField, button);

		verticalBox.getChildren().addAll(fileDetails);
		pane.getChildren().add(verticalBox);
		return pane;
	}

	@Override
	public boolean isFunctional() {
		return true;
	}

	public void saveMapHandle(final TextField fileNameField) {
		String name = fileNameField.getText();

		if (StringUtils.isEmpty(name)) {
			setStatus("Filename required");
			return;
		}

		Map<FunctionCategory, List<Function>> functions = new HashMap<FunctionCategory, List<Function>>();

		for (Map<?, ?> entry : this.getCommunicator().getData(MapLink.FUNCTIONS).values()) {
			Function f = DataUtils.castToItemObject(entry, Item.get(Function.class));
			if (f.isDeprecated()) {
				continue;
			}
			for (FunctionCategory category : f.getCategoryValues(null).keySet()) {
				if (!functions.containsKey(category)) {
					functions.put(category, new LinkedList<Function>());
				}
				functions.get(category).add(f);
			}
		}

		FileWriter writer = null;
		try {
			writer = new FileWriter(new File(name + ".txt"));

			for (FunctionCategory category : FunctionCategory.VALUES) {
				if (!functions.containsKey(category)) {
					continue;
				}
				functions.get(category).sort(new Comparator<Function>() {
					@Override
					public int compare(Function arg0, Function arg1) {
						return arg0.getName(null).compareTo(arg1.getName(null));
					}
				});
				;
				writer.write(category.toString() + StringUtils.LINEBREAK);
				for (Function f : functions.get(category)) {
					String SEPARATOR = " :: ";
					String functionName = f.getName(null);
					String description = f.getDescription(null);
					String categories = StringUtils.EMPTY;
					writer.write(functionName + SEPARATOR + description + SEPARATOR + categories
							+ StringUtils.LINEBREAK);
				}
				writer.write(StringUtils.LINEBREAK + StringUtils.LINEBREAK);
			}
		} catch (IOException e) {
			setStatus("Failed to write file: " + e.getMessage());
			return;
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				// Ignore
			}
		}
		setStatus("Done writing: " + name);
		return;
	}
}
