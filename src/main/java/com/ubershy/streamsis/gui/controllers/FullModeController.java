/** 
 * StreamSis
 * Copyright (C) 2015 Eva Balycheva
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ubershy.streamsis.gui.controllers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import org.controlsfx.control.NotificationPane;

import com.ubershy.streamsis.CuteConfig;
import com.ubershy.streamsis.LowLevel;
import com.ubershy.streamsis.SuperSoundManager;
import com.ubershy.streamsis.Util;
import com.ubershy.streamsis.elements.CuteElement;
import com.ubershy.streamsis.elements.SisScene;
import com.ubershy.streamsis.elements.actors.Actor;
import com.ubershy.streamsis.gui.GUIManager;
import com.ubershy.streamsis.gui.helperclasses.CuteColor;
import com.ubershy.streamsis.gui.helperclasses.GUIUtil;
import com.ubershy.streamsis.gui.helperclasses.OpenRecentManager;
import com.ubershy.streamsis.networking.ConnectionStatus;
import com.ubershy.streamsis.networking.StreamingProgramManager;
import com.ubershy.streamsis.project.CuteProject;
import com.ubershy.streamsis.project.ProjectManager;
import com.ubershy.streamsis.project.ProjectSerializator;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

//Under construction. Not working. Please unsee.
public class FullModeController implements Initializable {
	
    @FXML
    private VBox rootNode;
    @FXML
    private Menu openRecentMenu;
    @FXML
    private Button allActorsButton;
    @FXML
    private Button allVariablesButton;
    @FXML
    private MenuItem allActorsMenuItem;
    @FXML
    private MenuItem allVariablesMenuItem;
    @FXML
    private Button startStopButton;
    @FXML
    private ToggleButton onTopToggleButton;
    @FXML
    private Button stopAllSoundsButton;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Label volumeLabel;
    @FXML
    private Slider opacitySlider;
    @FXML
    private Label opacityLabel;
    @FXML
    private Button loveButton;
    @FXML
    private Button testButton;
    @FXML
    private VBox notificationPaneContainer;
    @FXML
    private VBox editableContentBox;
    @FXML
    private GridPane gridPane;
    @FXML
    private Accordion editorAccordion;
    @FXML
    private Label projectPathLabel;
    @FXML
    private Label numberOfElementsLabel;
    @FXML
    private ProgressBar projectInitProgressBar;
    @FXML
    private Label SPStatusLabel;
    @FXML
    private Label SPNameLabel;
    @FXML
    private ProgressBar SPStatusBar;
	
	private int sisSceneToRenameIndex = -1;
	private NotificationPane notificationPane;
	private CuteProject project;

	public Node getView() {
		return rootNode;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		projectPathLabel.textProperty().bind(ProjectManager.projectFilePathProperty());
		// Putting editableContentBox into Notification Pane
		notificationPane = new NotificationPane(editableContentBox);
//		notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
		GUIManager.setFullModeNotificationPane(notificationPane);
		notificationPane.setShowFromTop(true);
		notificationPane.setOnShowing(event -> {
			Thread hideThread = new Thread() {
				public void run() {
					try {
						Thread.sleep(4000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Platform.runLater(() -> {
						notificationPane.hide();
					});
				}
			};
			hideThread.start();
		});
		notificationPaneContainer.getChildren().clear();
		notificationPaneContainer.getChildren().add(notificationPane);
		VBox.setVgrow(notificationPane, Priority.ALWAYS);
	}

	private void initializeActorListView(ListView<Actor> actorsView) {
		gridPane.add(actorsView, 1, 1);
	}

	private void initializeCheckerTreeView(TreeView<CuteElement> treeView) {
		gridPane.add(treeView, 2, 1);
	}

	private void initializeSisSceneListView(ListView<SisScene> scenesView) {
		gridPane.add(scenesView, 0, 1);
	}

	private void initializeEditorPanel(ElementEditorController elementEditor) {
		editorAccordion.getPanes().add((TitledPane) elementEditor.getView());
	}

	private void initializeOffActionsTreeView(TreeView<CuteElement> treeView) {
		gridPane.add(treeView, 4, 1);
	}

	private void initializeOnActionsTreeView(TreeView<CuteElement> treeView) {
		gridPane.add(treeView, 3, 1);
	}
	
	private void initializeStatusBar() {
		// Set projectInitProgressBar behavior
		projectInitProgressBar.setOpacity(1.0);
		projectInitProgressBar.setProgress(-1.0);
		project.allElementsNumberProperty().addListener((o, oldVal, newVal) -> {
			if (newVal.intValue() == 0) {
				// Counting started, setting indeterminate value for progressbar.
				Platform.runLater(() -> {
					projectInitProgressBar.setOpacity(1.0);
					projectInitProgressBar.setProgress(-1.0);
				});
			}
		});
		project.initElementsNumberProperty().addListener((o, oldVal, newVal) -> {
			Platform.runLater(() -> {
				int allElements = project.getAllElementsNumber();
				numberOfElementsLabel.setText(""+newVal.intValue());
				if (allElements != 0) {
					if (allElements != newVal.intValue()) {
						double progress = newVal.doubleValue()/allElements;
						projectInitProgressBar.setProgress(progress);
					} else {
						projectInitProgressBar.setOpacity(0.0);
					}
				}
			});
		});
	}

	private void initializeOpenRecentMenu() {
		ObservableList<String> actualList = OpenRecentManager.getRecentProjectPathsList();
		ObservableList<MenuItem> menuItems = openRecentMenu.getItems();
		menuItems.clear();
		for (String path : actualList) {
			if (!path.isEmpty()) {
				MenuItem item = new MenuItem(path);
				item.setOnAction((ActionEvent event) -> {
					project.stopProject();
					CuteConfig.setString(CuteConfig.UTILGUI, "LastFileDirectory", path);
					OpenRecentManager.setRecentProject(path);
					GUIManager.loadProject(path, false);
				});
				menuItems.add(item);
			}
		}
		if (!menuItems.isEmpty()) {
			MenuItem clearButton = new MenuItem("Clear list");
			clearButton.setOnAction((ActionEvent event) -> {
				OpenRecentManager.clear();
				initializeOpenRecentMenu();
			});
			menuItems.add(clearButton);
		} else {
			MenuItem emptyItem = new MenuItem("Empty");
			emptyItem.setDisable(true);
			menuItems.add(emptyItem);
		}
	}

	private void initializeToolBar() {
		// "Love button"
		// Replace heart rendered with system font with the awesome font's heart.
		FontAwesomeIconView newHeart = new FontAwesomeIconView(FontAwesomeIcon.HEART);
		newHeart.setGlyphSize(30);
		newHeart.setFill(CuteColor.GENTLEPINK);
		loveButton.setGraphic(newHeart);
		
		// Volume slider and label
		double initialVolume = CuteConfig.getDouble(CuteConfig.CUTE, "GlobalVolume") * 100;
		volumeLabel.setText(String.format("Global Volume: %.0f", initialVolume) + "%");
		volumeSlider.setValue(initialVolume);
		volumeSlider.valueProperty().addListener((o, oldVal, newVal) -> {
			volumeLabel.setText(String.format("Global Volume: %.0f", newVal) + "%");
		});
		
		// Opacity slider and label
		double initialOpacity = CuteConfig.getDouble(CuteConfig.USERGUI, "MainWindowOpacity") * 100;
		if (initialOpacity < 20.0) {
			initialOpacity = 20.0;
		}
		if (initialOpacity > 100.0) {
			initialOpacity = 100.0;
		}
		opacitySlider.setValue(initialOpacity);
		setOpacity(initialOpacity);
		opacitySlider.valueProperty().addListener((o, oldVal, newVal) -> {
			setOpacity(newVal.doubleValue());
		});
		
		// "On Top" button
		boolean stayOnTop = CuteConfig.getBoolean(CuteConfig.USERGUI, "OnTop");
		onTopToggleButton.setSelected(stayOnTop);
		GUIManager.getPrimaryStage().setAlwaysOnTop(stayOnTop);
		onTopToggleButton.selectedProperty().addListener((o, oldVal, newVal) -> {
			CuteConfig.setBoolean(CuteConfig.USERGUI, "OnTop", newVal);
			GUIManager.getPrimaryStage().setAlwaysOnTop(newVal);
		});

		// Start/Stop button
		startStopButton.setStyle(
				"-fx-effect: dropshadow(gaussian, rgba(102,204,255,0.75), 50,0,0,0); -fx-focus-color: #0093ff; -fx-faint-focus-color: #039ED322;");
		final SimpleIntegerProperty shadowSize = new SimpleIntegerProperty(0);
		final Color startFocusColor = Color.web("#0093ff");
		final Color endFocusColor = Color.web("#ff75d0");
		final Color startFaintColor = Color.web("#039ED322");
		final Color endFaintColor = Color.web("#ff75d022");
		final Color startShadowColor = Color.web("#66CCFF");
		final Color endShadowColor = Color.web("#FF75D0");
		final ObjectProperty<Color> focusColor = new SimpleObjectProperty<Color>(startFocusColor);
		final ObjectProperty<Color> faintColor = new SimpleObjectProperty<Color>(startFaintColor);
		final ObjectProperty<Color> shadowColor = new SimpleObjectProperty<Color>(startShadowColor);
		final StringBinding cssStyle = Bindings.createStringBinding(new Callable<String>() {
			@Override
			public String call() throws Exception {
				// Warning: .toString() might not work the same in other
				// operating systems.
				String focus = focusColor.getValue().toString().replaceFirst("0x", "");
				String faint = faintColor.getValue().toString().replaceFirst("0x", "");
				String shadow = shadowColor.getValue().toString().replaceFirst("0x", "");
				String finalStyle = String.format(
						"-fx-effect: dropshadow(gaussian, #%s, %d,0,0,0); -fx-focus-color: #%s; -fx-faint-focus-color: #%s;",
						shadow, shadowSize.get(), focus, faint);
				return finalStyle;
			}
		}, shadowSize, shadowColor, focusColor, faintColor);

		startStopButton.styleProperty().bind(cssStyle);

		// Animation of work. (When CuteProject is started)
		final Timeline workingAnimation = new Timeline();
		workingAnimation.setCycleCount(Timeline.INDEFINITE);
		workingAnimation.setAutoReverse(true);
		final KeyValue wv = new KeyValue(shadowSize, 15);
		final KeyFrame wf = new KeyFrame(Duration.millis(1000), wv);
		workingAnimation.getKeyFrames().add(wf);

		// TODO: Optimize later
		// Animation of Starting CuteProject
		final Timeline startingAnimation = new Timeline();
		startingAnimation.setCycleCount(Timeline.INDEFINITE);
		startingAnimation.setAutoReverse(false);
		final KeyValue sv1 = new KeyValue(focusColor, endFocusColor);
		final KeyFrame sf1 = new KeyFrame(Duration.millis(10000), sv1);
		final KeyValue sv2 = new KeyValue(faintColor, endFaintColor);
		final KeyFrame sf2 = new KeyFrame(Duration.millis(10000), sv2);
		final KeyValue sv3 = new KeyValue(shadowColor, endShadowColor);
		final KeyFrame sf3 = new KeyFrame(Duration.millis(25000), sv3);
		startingAnimation.getKeyFrames().addAll(sf1, sf2, sf3);

		// Animation of Stopping CuteProject
		final Timeline stoppingAnimation = new Timeline();
		stoppingAnimation.setCycleCount(Timeline.INDEFINITE);
		stoppingAnimation.setAutoReverse(false);
		final KeyValue ssv1 = new KeyValue(focusColor, startFocusColor);
		final KeyFrame ssf1 = new KeyFrame(Duration.millis(20000), ssv1);
		final KeyValue ssv2 = new KeyValue(faintColor, startFaintColor);
		final KeyFrame ssf2 = new KeyFrame(Duration.millis(5000), ssv2);
		final KeyValue ssv3 = new KeyValue(shadowColor, startShadowColor);
		final KeyFrame ssf3 = new KeyFrame(Duration.millis(10000), ssv3);
		stoppingAnimation.getKeyFrames().addAll(ssf1, ssf2, ssf3);

		if (project.isStarted()) {
			startStopButton.setText("Stop");
			// workingAnimation.play();
			// stoppingAnimation.stop();
			// startingAnimation.playFromStart();
		}
		project.startedProperty().addListener((observable, oldValue, newValue) -> {
			Platform.runLater(() -> {
				if (newValue) {
					startStopButton.setText("Stop");
					// workingAnimation.play();
					// stoppingAnimation.stop();
					// startingAnimation.playFromStart();
				} else {
					startStopButton.setText("Start");
					// workingAnimation.pause();
					// startingAnimation.stop();
					// stoppingAnimation.playFromStart();
				}
			});
		});
		
		// "All Actors" button.
		Stage aaStage = GUIManager.getAllActorsStage();
		allActorsButton.disableProperty().bind(aaStage.showingProperty());
		
		// "All Variables" button.
		Stage avStage = GUIManager.getAllVariablesStage();
		allVariablesButton.disableProperty().bind(avStage.showingProperty());
		
		// Networking stuff (Communication with Streaming Program).
		SPNameLabel.setText(StreamingProgramManager.getClientType().toString());
		StreamingProgramManager.clientTypeProperty().addListener((o, oldVal, newVal) -> {
			Platform.runLater(() -> {
				SPNameLabel.setText(newVal.toString());
			});
		});
		SPStatusLabel.setText(StreamingProgramManager.getStatus().toString());
		setSPStatusBar(StreamingProgramManager.getStatus());
		StreamingProgramManager.statusProperty().addListener((o, oldVal, newVal) -> {
			Platform.runLater(() -> {
				SPStatusLabel.setText(newVal.toString());
				setSPStatusBar(newVal);
			});
		});
	}
	
	private void setSPStatusBar(ConnectionStatus status) {
		switch (status) {
		case AUTHENTICATING:
			SPStatusBar.setProgress(-1.0);
			break;
		case AUTHENTICATIONFAIL:
			SPStatusBar.setProgress(-1.0);
			break;
		case CONNECTING:
			SPStatusBar.setProgress(-1.0);
			break;
		case CONNECTIONERROR: 
			SPStatusBar.setProgress(-1.0);
			break;
		case ERROR:
			SPStatusBar.setProgress(-1.0);
			break;
		case OFFLINE:
			SPStatusBar.setProgress(0.0);
			break;
		case ONLINE:
			SPStatusBar.setProgress(1.0);
			break;
		default:
			throw new RuntimeException("What is this status?");
		}
	}
	
	public void setOpacity(double opacity) {
		opacityLabel.setText(String.format("Opacity: %.0f", opacity) + "%");
		Window window = GUIManager.getPrimaryStage();
		window.setOpacity(opacity / 100.0);
		// See also setOpacityToConfig() method which is invoked on mouse release.
	}

	@FXML
	public void makeSomeLove() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Love Button");
		alert.setHeaderText("Dear user, this button is intended just to make you smile.");
		alert.setContentText("I think you are smiling right now. =)\n\nAre you smiling?");
		GUIUtil.cutifyAlert(alert);
		ButtonType yesButton = new ButtonType("Yes");
		ButtonType noButton = new ButtonType("No");
		alert.getButtonTypes().setAll(yesButton, noButton);
		Optional<ButtonType> result = GUIUtil.showAlertInPrimaryStageCenter(alert);
		if (result.get() == yesButton) {
			Alert subAlert = new Alert(AlertType.INFORMATION);
			subAlert.setTitle("Love Button");
			subAlert.setHeaderText("Yes! You are smiling! =)");
			subAlert.setContentText(
					"Love Button is working as expected.\nMaybe I'm a good programmer.");
			GUIUtil.cutifyAlert(subAlert);
			GUIUtil.showAlertInPrimaryStageCenter(subAlert);
		} else if (result.get() == noButton) {
			Alert subAlert = new Alert(AlertType.ERROR);
			GUIUtil.cutifyAlert(subAlert);
			subAlert.setTitle("Love Button");
			subAlert.setHeaderText("Oh.");
			subAlert.setContentText(
					"Love Button seems not to be working...\nAnd shamefully goes away. =(");
			GUIUtil.showAlertInPrimaryStageCenter(subAlert);
			loveButton.setManaged(false);
			loveButton.setDisable(true);
			loveButton.setVisible(false);
		}
	}

	@FXML
	private void newProject(ActionEvent event) {
		GUIManager.createNewProject();
	}

	@FXML
	void openDataFolder(ActionEvent event) {
		try {
			Desktop.getDesktop().open(new File(LowLevel.getAppDataPath()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	private void openProject(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		// Set extension filter
		fileChooser.setTitle("Open StreamSis Project");
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
				"StreamSis Project files (*.streamsis)", "*.streamsis");
		fileChooser.getExtensionFilters().add(extFilter);
		String lastDir = CuteConfig.getString(CuteConfig.UTILGUI, "LastFileDirectory");
		if (Util.checkDirectory(lastDir)) {
			fileChooser.setInitialDirectory(new File(lastDir));
		}
		File file = fileChooser.showOpenDialog(rootNode.getScene().getWindow());
		if (file != null) {
			if (project != null)
				project.stopProject();
			CuteConfig.setString(CuteConfig.UTILGUI, "LastFileDirectory",
					file.getParentFile().getAbsolutePath());
			OpenRecentManager.setRecentProject(file.getAbsolutePath());
			initializeOpenRecentMenu();
			GUIManager.loadProject(file.getAbsolutePath(), false);
		}
	}

	public void bindToProject(CuteProject project) {
		this.project = project;
		if (project != null) {
			initializeSisSceneListView(GUIManager.sisSceneList);
			initializeActorListView(GUIManager.actorList);
			initializeCheckerTreeView(GUIManager.checkerTree);
			initializeOnActionsTreeView(GUIManager.onActionsTree);
			initializeOffActionsTreeView(GUIManager.offActionsTree);
			initializeEditorPanel(GUIManager.elementEditor);
			initializeToolBar();
			initializeStatusBar();
			initializeMenuBar();
		}
	}

	private void initializeMenuBar() {
		initializeOpenRecentMenu();
		// "All Actors" menu item.
		Stage aaStage = GUIManager.getAllActorsStage();
		allActorsMenuItem.disableProperty().bind(aaStage.showingProperty());
		// "All Variables" menu item.
		Stage avStage = GUIManager.getAllVariablesStage();
		allVariablesMenuItem.disableProperty().bind(avStage.showingProperty());
	}

	@FXML
	private void quitApplication() {
		GUIManager.saveCoordinatesOfAllWindows();
		Platform.exit();
	}

	@FXML
	private void saveProject(ActionEvent event) {
		String projectFilePath = ProjectManager.getProjectFilePath();
		if (projectFilePath == null) {
			saveProjectAs(null);
		} else {
			try {
				ProjectSerializator.serializeToFile(project, projectFilePath);
				OpenRecentManager.setRecentProject(projectFilePath);
				initializeOpenRecentMenu();
			} catch (IOException e) {
				Alert alert = new Alert(AlertType.ERROR);
				GUIUtil.cutifyAlert(alert);
				alert.setTitle("Can't save Project");
				alert.setHeaderText(
						"Dear user, try to save the Project in another location to prevent data loss.");
				alert.setContentText(
						"Original file is inaccessible.\nMaybe you have disconnected a memory device where Project file was stored.\n"
								+ "Or your Project file is currently opened in another program that is blocking access.\n\nWho knows? ¯\\_(ツ)_/¯");
				GUIUtil.showAlertInPrimaryStageCenter(alert);
			}
		}
		GUIManager.saveCoordinatesOfAllWindows();
	}

	@FXML
	private void saveProjectAs(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save StreamSis Project");
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
				"StreamSis Project files (*.streamsis)", "*.streamsis");
		fileChooser.getExtensionFilters().add(extFilter);
		String lastDir = CuteConfig.getString(CuteConfig.UTILGUI, "LastFileDirectory");
		if (Util.checkDirectory(lastDir)) {
			fileChooser.setInitialDirectory(new File(lastDir));
		}
		File file = fileChooser.showSaveDialog(rootNode.getScene().getWindow());
		if (file != null) {
			try {
				String absolutePath = file.getAbsolutePath();
				CuteConfig.setString(CuteConfig.UTILGUI, "LastFileDirectory",
						file.getParentFile().getAbsolutePath());
				ProjectSerializator.serializeToFile(project, absolutePath);
				OpenRecentManager.setRecentProject(absolutePath);
				ProjectManager.setProjectFilePath(absolutePath);
				initializeOpenRecentMenu();
			} catch (IOException e) {
				Alert alert = new Alert(AlertType.ERROR);
				GUIUtil.cutifyAlert(alert);
				alert.setTitle("Can't save Project");
				alert.setHeaderText(
						"Project can't be saved in the chosen location. Dear user, please choose another.");
				alert.setContentText(
						"Maybe you don't have rights to write there.\nOr location is not currently available to use.\n"
								+ "Or you are trying to rewrite a file that is currently opened in another program that is blocking access.\n\nWho knows? ¯\\(°_o)/¯");
				GUIUtil.showAlertInPrimaryStageCenter(alert);
				saveProjectAs(null);
			}
		}
		GUIManager.saveCoordinatesOfAllWindows();
	}

//	private void searchSisScenes(ActionEvent event) {
//		final javafx.concurrent.Task<List<SisScene>> searchSisScenesTask = new javafx.concurrent.Task<List<SisScene>>() {
//			protected List<SisScene> call() throws Exception {
//				System.out.println("Searching SisScenes...");
//				return project.getSisScenes();
//			}
//		};
//		searchSisScenesTask.stateProperty().addListener(new ChangeListener<Worker.State>() {
//			public void changed(ObservableValue<? extends Worker.State> source,
//					Worker.State oldState, Worker.State newState) {
//				if (newState.equals(Worker.State.SUCCEEDED)) {
//					GUIManager.sisSceneList.getItems().setAll(searchSisScenesTask.getValue());
//				}
//			}
//		});
//		new Thread(searchSisScenesTask).start();
//	}

	@FXML
	private void setVolumeToConfigAndLoadedSounds() { // Is invoked on mouse release on slider.
		double volumeToSet = volumeSlider.getValue() / 100.0;
		CuteConfig.setDouble(CuteConfig.CUTE, "GlobalVolume", volumeToSet);
		SuperSoundManager.setVolumeForAllSounds(volumeToSet);
	}

	@FXML
	private void setOpacityToConfig() { // Is invoked on mouse release on slider.
		CuteConfig.setDouble(CuteConfig.USERGUI, "MainWindowOpacity",
				opacitySlider.getValue() / 100.0);
	}

//	@FXML
//	private void showCompactMode() {
//		GUIManager.mainController.useCompactMode();
//	}

	@FXML
	private void startStopProject() {
		if (project.isStarted()) {
			project.stopProject();
		} else {
			if (sisSceneToRenameIndex == -1) // Means user is not editing cell
				try {
					ProjectManager.startProjectFromGUI();
				} catch (RuntimeException e) {
					Alert alert = new Alert(AlertType.ERROR);
					GUIUtil.cutifyAlert(alert);
					alert.setTitle("Can't start the Project");
					alert.setHeaderText("Dear user, StreamSis can't start the Project.");
					if (e.getMessage().equals("globalActors are empty")) {
						alert.setContentText("You have to add at least one Actor to this Project");
					}
					if (e.getMessage().equals("sisScenes are empty")) {
						alert.setContentText(
								"You have to add at least one SisScene to this Project");
					}
					GUIUtil.showAlertInPrimaryStageCenter(alert);
				}
		}
	}

	@FXML
	private void testThing() {
		String text = "This is a very important text message.";
		// Label love = new Label("❤");
		Label love = new Label("💘💓 ♥ 💔🐞");
		love.setTextFill(Color.web("#ff75d0"));
		love.setStyle(" -fx-font-size: 25");
		GUIManager.showNotification(love, text);
		// notificationPane.show();
		// System.out.println(LowLevel.getApplicationName());
//		GUIManager.sisSceneList.getFocusModel().focus(3);
		// GUIManager.getPrimaryStage().hide();
		// Playground.testNewMTRegionChecker(10000, 0, false);
		// Playground.testOldMTRegionChecker(100, 0, false);
	}
	
    @FXML
    void stopAllSounds(ActionEvent event) {
    	SuperSoundManager.stopAllSounds();
    }

    @FXML
    void showSettings(ActionEvent event) {
    	GUIUtil.showSettingsWindow();
    }

    @FXML
    void showAllActorsWindow(ActionEvent event) {
    	Stage aaStage = GUIManager.getAllActorsStage();
    	aaStage.show();
    	aaStage.requestFocus();
    }

    @FXML
    void showAllVariablesWindow(ActionEvent event) {
    	Stage avStage = GUIManager.getAllVariablesStage();
    	avStage.show();
    	avStage.requestFocus();
    }

    @FXML
    void showAboutWindow(ActionEvent event) {
    	GUIUtil.showAboutWindow();
    }

}
