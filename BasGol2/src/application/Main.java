
package application;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import model.BasGolCanvas;
import model.Coord;
import model.ErrorAlert;
import model.GolPattern;
import model.InputTextAlert;
import model.InvalidRleException;
import model.PatternFrame;
import model.Selection;
import xml.FileManager;

public class Main extends Application
      {

	    // fields
	    private static final String	VERSION	       = "v0.1";
	    private static final String	TITLE	       = "Ben's Amazing Soft - Game of Life 2 " + VERSION;
	    private final int		SLEEPTIME      = 100;
	    private final int		XSTARTFRAME    = 900;
	    private final int		YSTARTFRAME    = 600;
	    private Path		gridFilesDir   = Paths.get(".\\grid_files\\");
	    private int			xFrame, yFrame, xGrid, yGrid;
	    private IntegerProperty	steps	       = new SimpleIntegerProperty(0);

	    private final ImageView	SELECT_ICON    = new ImageView("./images/selectIcon.png");
	    private final ImageView	SELECT_ICON_ON = new ImageView("./images/selectIconOn.png");
	    private final ImageView	MOVE_ICON      = new ImageView("./images/moveIcon.png");
	    private final ImageView	MOVE_ICON_ON   = new ImageView("./images/moveIconOn.png");
	    private final ImageView	SAVE_ICON      = new ImageView("./images/saveIcon.png");
	    private final ImageView	GRIDON_ICON    = new ImageView("./images/gridOnIcon.png");

	    private final ImageView	GRIDON_ICON_ON = new ImageView("./images/gridOnIconOn.png");

	    private final ImageView	CLEAR_ICON     = new ImageView("./images/clearIcon.png");
	    private final ImageView	FORWARD_ICON   = new ImageView("./images/forwardIcon.png");
	    private final ImageView	BACKWARD_ICON  = new ImageView("./images/backwardIcon.png");
	    private final ImageView	PLAY_ICON      = new ImageView("./images/playIcon.png");
	    private final ImageView	PLAYBACK_ICON  = new ImageView("./images/playBackIcon.png");
	    private final ImageView	STOP_ICON      = new ImageView("./images/stopIcon.png");
	    private final ImageView	IMPORT_ICON    = new ImageView("./images/importIcon.png");

	    // enum
	    private enum tools
		  {
		  DEFAULT, CLEAR, FILL, SAVE, SELECT, PAN
		  }

	    private tools		    tool       = tools.DEFAULT;

	    // collections
	    private DirectoryStream<Path>   dirStream;
	    private Map<String, GolPattern> patterns   = new HashMap<>();

	    // javafx Parent and nodes
	    private Scene		    scene;
	    private BorderPane		    root       = new BorderPane();
	    private ScrollPane		    patternsSp;

	    private ScrollPane		    scrollP    = new ScrollPane();
	    private VBox		    patternsVb = new VBox();
	    private BasGolCanvas	    frame      = new BasGolCanvas();
	    private VBox		    info;
	    private Button		    selectBut, panBut, saveBut, gridOnBut, clearBut, forwardBut, backwardBut, playBut, playBackBut, importBut;
	    private ToolBar		    toolBar;
	    private Boolean		    play       = false;
	    private Boolean		    playBack   = false;


	    @Override
	    public void start(Stage stage)
		  {

			xFrame = xGrid = XSTARTFRAME;
			yFrame = yGrid = YSTARTFRAME;
			frame = new BasGolCanvas(this, xFrame, yFrame, xGrid, yGrid);

			scrollP.setContent(frame);
			scrollP.setPannable(false);
			scrollP.setHbarPolicy(ScrollBarPolicy.NEVER);
			scrollP.setVbarPolicy(ScrollBarPolicy.NEVER);
			scrollP.setVvalue(0.5);
			scrollP.setHvalue(0.5);
			scrollP.addEventFilter(ScrollEvent.ANY, new ZoomControl());

			root = new BorderPane();
			root.setPrefSize(XSTARTFRAME, YSTARTFRAME);
			root.setCenter(scrollP);
			root.setStyle("-fx-background-color : white");

			// set info/control panel

			Label stepCountLbl2 = new Label();
			stepCountLbl2.textProperty().bind(steps.asString());
			HBox stepCountHb = new HBox(new Label("Steps : "), stepCountLbl2);

			Label cellCountLbl2 = new Label();
			cellCountLbl2.textProperty().bind(frame.getCellCountIntProp().asString());
			HBox cellCountHb = new HBox(new Label("Cells : "), cellCountLbl2);

			info = new VBox(stepCountHb, cellCountHb);
			info.setMinWidth(225);
			info.setStyle("-fx-font-size : 15;" + "-fx-border-color : darkgrey");
			info.setAlignment(Pos.TOP_LEFT);
			info.setPadding(new Insets(10, 10, 10, 10));

			patternsSp = new ScrollPane(patternsVb);
			patternsSp.setFitToWidth(true);
			patternsVb.setSpacing(5);
			patternsVb.setPadding(new Insets(5, 5, 5, 5));
			patternsSp.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
			patternsSp.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

			// Load pattern files, if any exist in the folder
			loadGridFiles();

			info.getChildren().add(patternsSp);
			root.setRight(info);

			// Setup tooolbar

			selectBut = new Button("", SELECT_ICON);
			selectBut.setOnAction((evt) -> toggleSelect());
			panBut = new Button("", MOVE_ICON);
			panBut.setOnAction(evt -> togglePan());
			gridOnBut = new Button("", GRIDON_ICON);
			gridOnBut.setOnAction(evt -> toggleGridLines());
			saveBut = new Button("", SAVE_ICON);
			saveBut.setOnAction(evt -> savePattern());
			clearBut = new Button("", CLEAR_ICON);
			clearBut.setOnAction(evt -> clearGrid());
			forwardBut = new Button("", FORWARD_ICON);
			forwardBut.setOnAction(evt -> forward());
			backwardBut = new Button("", BACKWARD_ICON);
			backwardBut.setOnAction(evt -> backward());
			playBut = new Button("", PLAY_ICON);
			playBut.setOnAction(evt -> play());
			playBackBut = new Button("", PLAYBACK_ICON);
			playBackBut.setOnAction(evt -> playBack());
			importBut = new Button("", IMPORT_ICON);
			importBut.setOnAction(evt -> importRLE());

			toolBar = new ToolBar(selectBut, panBut, gridOnBut, saveBut, clearBut, importBut, backwardBut, playBackBut, playBut, forwardBut);

			root.setTop(toolBar);

			scene = new Scene(root);
			scene.addEventFilter(KeyEvent.ANY, new KeyControls());
			stage.setTitle(TITLE);
			stage.setScene(scene);
			stage.show();

		  }


	    /**
	     * 
	     */
	    private void loadGridFiles()
		  {

			patternsVb.getChildren().clear();
			patterns.clear();

			if (gridFilesDir.toFile().exists())
			      {
				    try
					  {
						dirStream = Files.newDirectoryStream(gridFilesDir, "*.xml");

						if (gridFilesDir.toFile().list().length != 0)
						      {
							    Alert alert = new Alert(AlertType.INFORMATION, "Loading files ");
							    alert.show();

							    Iterator<Path> it = dirStream.iterator();
							    while (it.hasNext())
								  {

									Path path = it.next();
									if (!path.toFile().getName().contains("temp.xml"))
									      {
										    alert.setContentText(alert.getContentText() + "\n\t- " + path.toString());
										    alert.hide();
										    alert.show();
										    File file = path.toFile();
										    GolPattern pattern = new FileManager(file).read();
										    PatternFrame patternFrame = new PatternFrame(pattern);
										    Button delBut = new Button("", new ImageView("./images/deleteIcon.png"));
										    delBut.setPadding(new Insets(0, 0, 0, 0));
										    delBut.setOnAction(e -> {
											  try
												{
												      Files.delete(path);
												      Files.delete(Paths.get(path.toFile().getAbsolutePath().replaceAll(".xml", ".png")));
												} catch (IOException e1)
												{
												      new ErrorAlert(e1);
												}
											  patternsVb.getChildren().remove(patternFrame);
										    });
										    patterns.put(pattern.getName(), pattern);
										    patternFrame.getTopBut().getChildren().add(delBut);
										    patternsVb.getChildren().add(patternFrame);

									      }

								  }
							    alert.hide();
						      }
					  } catch (Exception e)
					  {
						new ErrorAlert(e);
						e.printStackTrace();
					  }
			      }
		  }


	    public static void main(String[] args)
		  {

			launch(args);
		  }

	    // filter all KeyEvent to block scrolPane implemented key binding, which sucks big times
	    // KEY PRESSED enables repeat if key is kept down
	    // KEY RELEASED is used because KEY TYPED keyCode is always UNDEFINED
	    public class KeyControls implements EventHandler<KeyEvent>
		  {

			@SuppressWarnings("incomplete-switch")
			@Override
			public void handle(KeyEvent event)
			      {

				    switch (event.getCode())
					  {
					  case RIGHT:
						{

						      if (event.getEventType() == KeyEvent.KEY_PRESSED)
							    {
								  forward();

							    }
						      break;
						}

					  case LEFT:
						{

						      if (event.getEventType() == KeyEvent.KEY_PRESSED)
							    {

								  backward();

							    }
						      break;
						}

					  case R:
						{

						      if (event.getEventType() == KeyEvent.KEY_RELEASED)
							    {

							    }
						      break;
						}
					  case C:
						{
						      if (event.getEventType() == KeyEvent.KEY_RELEASED)
							    {
								  clearGrid();
							    }
						      break;
						}

					  case SPACE:
						{
						      if (event.getEventType() == KeyEvent.KEY_RELEASED)
							    {
								  if (tool == tools.DEFAULT || tool == tools.PAN)
									{

									      togglePan();

									}

							    }
						      break;
						}

					  case S:
						{
						      if (event.getEventType() == KeyEvent.KEY_RELEASED)
							    {

								  try
									{

									      if (frame.getSelection().getCoords().isEmpty())
										    {
											  frame.setSelection(
												      new Selection(new Coord(0, 0), new Coord(frame.getGrid().length, frame.getGrid()[0].length)));
										    }

									      GolPattern pattern = selectionToPattern(
											  event.isControlDown() ? new InputTextAlert("Enter Pattern name").reponse() : "temp");
									      new FileManager(pattern).write();
									} catch (JAXBException e)
									{
									      new ErrorAlert(e);
									      e.printStackTrace();
									}
								  frame.getSelection().getCoords().clear();
								  loadGridFiles();
								  frame.drawGrid();

							    }
						      break;
						}

					  case L:
						{
						      if (event.getEventType() == KeyEvent.KEY_RELEASED)
							    {
								  loadTempPattern();

							    }
						      break;
						}

					  case V:
						{
						      if (event.getEventType() == KeyEvent.KEY_RELEASED)
							    {

								  toggleSelect();
								  break;
							    }
						}

					  case G:
						{
						      if (event.getEventType() == KeyEvent.KEY_RELEASED)
							    {
								  toggleGridLines();
								  break;
							    }
						}

					  }

				    event.consume();

			      }

		  }


	    public GolPattern selectionToPattern(String name)
		  {

			GolPattern pattern = new GolPattern();
			pattern.setName(name);

			pattern.setxSize(frame.getSelection().getxSize());
			pattern.setySize(frame.getSelection().getySize());

			Map<Coord, Boolean> coords = new HashMap<>(frame.getPattern().getCoords());

			coords.entrySet().removeIf(entry -> !frame.getSelection().getCoords().contains(entry.getKey()));
			coords.forEach((coord, bool) -> {
			      int shiftX = frame.getSelection().getCoords().first().getX();
			      int shiftY = frame.getSelection().getCoords().first().getY();
			      pattern.getCoords().put(new Coord(coord.getX() - shiftX, coord.getY() - shiftY), bool);
			});

			return pattern;
		  }

	    public class ZoomControl implements EventHandler<ScrollEvent>
		  {

			@Override
			public void handle(ScrollEvent event)
			      {

				    if (event.getDeltaY() > 0)
					  {
						zoomIn();
					  } else
					  {
						zoomOut();
					  }
				    event.consume();
			      }

		  }


	    private void zoomIn()
		  {

			Bounds viewPort = scrollP.getViewportBounds();
			Bounds contentSize = frame.getBoundsInParent();

			double centerPosX = (contentSize.getWidth() - viewPort.getWidth()) * scrollP.getHvalue() + viewPort.getWidth() / 2;
			double centerPosY = (contentSize.getHeight() - viewPort.getHeight()) * scrollP.getVvalue() + viewPort.getHeight() / 2;

			frame.zoomIn();

			double newCenterX = centerPosX * frame.getResizer();
			double newCenterY = centerPosY * frame.getResizer();

			scrollP.setHvalue((newCenterX - viewPort.getWidth() / 2) / (contentSize.getWidth() * frame.getResizer() - viewPort.getWidth()));
			scrollP.setVvalue((newCenterY - viewPort.getHeight() / 2) / (contentSize.getHeight() * frame.getResizer() - viewPort.getHeight()));

		  }


	    private void zoomOut()
		  {

			Bounds viewPort = scrollP.getViewportBounds();
			Bounds contentSize = frame.getBoundsInParent();

			double centerPosX = (contentSize.getWidth() - viewPort.getWidth()) * scrollP.getHvalue() + viewPort.getWidth() / 2;
			double centerPosY = (contentSize.getHeight() - viewPort.getHeight()) * scrollP.getVvalue() + viewPort.getHeight() / 2;

			frame.zoomOut();

			double newCenterX = centerPosX * frame.getResizer();
			double newCenterY = centerPosY * frame.getResizer();

			scrollP.setHvalue((newCenterX - viewPort.getWidth() / 2) / (contentSize.getWidth() * frame.getResizer() - viewPort.getWidth()));
			scrollP.setVvalue((newCenterY - viewPort.getHeight() / 2) / (contentSize.getHeight() * frame.getResizer() - viewPort.getHeight()));

		  }


	    /**
	     * 
	     */
	    private void clearGrid()
		  {

			frame.getPattern().getCoords().replaceAll((k, v) -> false);
			frame.drawGrid();
		  }


	    /**
	     * 
	     */
	    private void togglePan()
		  {

			scrollP.setPannable(!scrollP.isPannable());
			frame.setMouseTransparent(!frame.isMouseTransparent());
			scrollP.setCursor(scrollP.isPannable() ? Cursor.MOVE : frame.isSelectionMode() ? Cursor.CROSSHAIR : Cursor.DEFAULT);

			tool = scrollP.isPannable() ? tools.PAN : tools.DEFAULT;
			panBut.setGraphic(scrollP.isPannable() ? MOVE_ICON_ON : MOVE_ICON);
		  }


	    /**
	     * @param event
	     */
	    private void savePattern()
		  {

			String newName = new InputTextAlert("Enter Pattern name").reponse();

			if (newName.length() > 0)
			      {

				    try
					  {

						if (frame.getSelection().getCoords().isEmpty())
						      {
							    frame.setSelection(new Selection(new Coord(0, 0), new Coord(frame.getGrid().length, frame.getGrid()[0].length)));
						      }

						GolPattern pattern = selectionToPattern(newName);
						new FileManager(pattern).write();
					  } catch (JAXBException e)
					  {
						new ErrorAlert(e);
						e.printStackTrace();
					  }
				    frame.getSelection().getCoords().clear();
				    loadGridFiles();
				    frame.drawGrid();
			      }
		  }


	    /**
	     * 
	     */
	    private void loadTempPattern()
		  {

			if (new File("./grid_files/temp.xml").exists())
			      {
				    try
					  {
						FileManager fileManager = new FileManager(new GolPattern("temp", frame.getGrid().length, frame.getGrid()[0].length, frame.getPattern().getCoords()));

						frame.getPattern().getCoords().clear();
						frame.drawGrid();
						frame.getPattern().setCoords(fileManager.read().getCoords());
						frame.drawGrid();
					  } catch (JAXBException e)
					  {
						new ErrorAlert(e);
						e.printStackTrace();
					  }
			      }
		  }


	    /**
	     * 
	     */
	    private void toggleSelect()
		  {

			if (tool == tools.DEFAULT || tool == tools.SELECT)
			      {
				    frame.setSelectionMode(!frame.isSelectionMode());
				    scrollP.setCursor(frame.isSelectionMode() ? Cursor.CROSSHAIR : Cursor.DEFAULT);
				    if (!frame.isSelectionMode())
					  {
						frame.setSelection(new Selection());
						frame.drawGrid();
					  }
				    tool = frame.isSelectionMode() ? tools.SELECT : tools.DEFAULT;
				    selectBut.setGraphic(frame.isSelectionMode() ? SELECT_ICON_ON : SELECT_ICON);
			      }
		  }


	    /**
	     * 
	     */
	    private void toggleGridLines()
		  {

			frame.setGridOn(!frame.isGridOn());
			frame.drawGrid();

			gridOnBut.setGraphic(frame.isGridOn() ? GRIDON_ICON_ON : GRIDON_ICON);
		  }


	    /**
	     * 
	     */
	    private void forward()
		  {

			frame.stepForward();
			steps.set(steps.getValue() + 1);
		  }


	    /**
	     * 
	     */
	    private void backward()
		  {

			frame.stepBackward();
			steps.set(Math.max(steps.getValue() - 1, 0));
			if (steps.getValue() == 0)
			      {
				    playBack = false;
			      }
		  }


	    private void play()
		  {

			playBut.setGraphic(STOP_ICON);
			playBack = false;
			play = !play;

			// play infinite loop
			new Thread(new Runnable()
			      {

				    @Override
				    public void run()
					  {

						while (play)
						      {

							    try
								  {
									Thread.sleep(SLEEPTIME);
								  } catch (InterruptedException e)
								  {
									new ErrorAlert(e);
								  }

							    // run in javafx gui thread
							    Platform.runLater(new Runnable()
								  {

									@Override
									public void run()
									      {

										    forward();
									      }
								  });

							    if (!play)
								  {
									Platform.runLater(() -> {
									      playBut.setGraphic(PLAY_ICON);
									});
									break;
								  }
						      }
					  }
			      }).start();
		  }


	    private void playBack()
		  {

			play = false;
			playBack = !playBack;

			playBackBut.setGraphic(STOP_ICON);

			// playBack infinite loop
			new Thread(new Runnable()
			      {

				    @Override
				    public void run()
					  {

						while (playBack)
						      {

							    try
								  {
									Thread.sleep(SLEEPTIME);
								  } catch (InterruptedException e)
								  {
									new ErrorAlert(e);
								  }

							    // run in javafx gui thread
							    Platform.runLater(new Runnable()
								  {

									@Override
									public void run()
									      {

										    backward();
									      }
								  });

							    if (!playBack)
								  {
									Platform.runLater(() -> {
									      playBackBut.setGraphic(PLAYBACK_ICON);
									});
									break;
								  }
						      }
					  }
			      }).start();
		  }


	    private void importRLE()
		  {

			FileChooser chooser = new FileChooser();

			chooser.setTitle("Please select .rle file to import");
			chooser.setSelectedExtensionFilter(new ExtensionFilter("RLE", "*.rle"));

			File rleFile = chooser.showOpenDialog(null);

			if (rleFile != null && rleFile.exists())
			      {
				    GolPattern pattern;
				    try
					  {
						pattern = new GolPattern(rleFile);
						new FileManager(pattern).write();
						
					  } catch (InvalidRleException | JAXBException e)
					  {
						new ErrorAlert(e);
					  }
				    loadGridFiles();
			      }

		  }

      }
