
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
import javafx.stage.Stage;
import model.BasGolCanvas;
import model.Coord;
import model.ErrorAlert;
import model.Pattern;
import model.PatternFrame;
import model.Selection;
import xml.FileManager;

public class Main extends Application
      {

	    // fields
	    private static final String	VERSION	     = "v0.1";
	    private static final String	TITLE	     = "Ben's Amazing Soft - Game of Life 2 " + VERSION;
	    private final int		SLEEPTIME    = 100;
	    private final int		XSTARTFRAME  = 900;
	    private final int		YSTARTFRAME  = 600;
	    private Path		gridFilesDir = Paths.get(".\\grid_files\\");
	    private int			xFrame, yFrame, xGrid, yGrid;
	    private IntegerProperty	steps	     = new SimpleIntegerProperty(0);

	    // enum
	    private enum tools
		  {
		  DEFAULT, CLEAR, FILL, SAVE, SELECT, PAN
		  }

	    private tools		  tool	     = tools.DEFAULT;

	    // collections
	    private DirectoryStream<Path> dirStream;
	    private Map<String, Pattern>  patterns   = new HashMap<>();

	    // javafx Parent and nodes
	    private Scene		  scene;
	    private BorderPane		  root	     = new BorderPane();
	    private ScrollPane		  patternsSp;

	    private ScrollPane		  scrollP    = new ScrollPane();
	    private VBox		  patternsVb = new VBox();
	    private BasGolCanvas	  frame	     = new BasGolCanvas();
	    private VBox		  info;
	    private Button		  selectBut, panBut, saveBut, gridOnBut, clearBut, forwardBut, backwardBut, playBut, playBackBut;
	    private ToolBar		  toolBar;
	    private Boolean		  play	     = false;
	    private Boolean		  playBack   = false;


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

			selectBut = new Button("", new ImageView("./images/selectIcon.png"));
			selectBut.setOnAction((evt) -> toggleSelect());
			panBut = new Button("", new ImageView("./images/moveIcon.png"));
			panBut.setOnAction(evt -> togglePan());
			gridOnBut = new Button("", new ImageView("./images/gridOnIcon.png"));
			gridOnBut.setOnAction(evt -> toggleGridLines());
			saveBut = new Button("", new ImageView("./images/saveIcon.png"));
			saveBut.setOnAction(evt -> savePattern());
			clearBut = new Button("", new ImageView("./images/clearIcon.png"));
			clearBut.setOnAction(evt -> clearGrid());
			forwardBut = new Button("", new ImageView("./images/forwardIcon.png"));
			forwardBut.setOnAction(evt -> forward());
			backwardBut = new Button("", new ImageView("./images/backwardIcon.png"));
			backwardBut.setOnAction(evt -> backward());
			playBut = new Button("", new ImageView("./images/playIcon.png"));
			playBut.setOnAction(evt -> play());
			playBackBut = new Button("", new ImageView("./images/playBackIcon.png"));
			playBackBut.setOnAction(evt -> playBack());

			toolBar = new ToolBar(selectBut, panBut, gridOnBut, saveBut, clearBut, backwardBut, playBackBut, playBut, forwardBut);

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
										    Pattern pattern = new FileManager(file).read();
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

									      Pattern pattern = selectionToPattern(event.isControlDown() ? new InputTextAlert("Enter Pattern name").reponse() : "temp");
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


	    public Pattern selectionToPattern(String name)
		  {

			Pattern pattern = new Pattern();
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
			scrollP.setCursor(scrollP.isPannable() ? Cursor.MOVE : Cursor.DEFAULT);

			tool = scrollP.isPannable() ? tools.PAN : tools.DEFAULT;
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

						Pattern pattern = selectionToPattern(newName);
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
						FileManager fileManager = new FileManager(new Pattern("temp", frame.getGrid().length, frame.getGrid()[0].length, frame.getPattern().getCoords()));

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
			      }
		  }


	    /**
	     * 
	     */
	    private void toggleGridLines()
		  {

			frame.setGridOn(!frame.isGridOn());
			frame.drawGrid();
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
								  break;

						      }
					  }
			      }).start();
		  }


	    private void playBack()
		  {

			play = false;
			playBack = !playBack;

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
								  break;

						      }
					  }
			      }).start();
		  }
      }
