
package application;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBException;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
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
	    private static final String	title	     = "Ben's Amazing Soft - Game of Life 2 " + VERSION;
	    private final String	STEPCOUNT    = "STEPCOUNT";
	    private final String	CELLCOUNT    = "CELLCOUNT";
	    private final int		XSTARTFRAME  = 900;
	    private final int		YSTARTFRAME  = 600;
	    private Path		gridFilesDir = Paths.get(".\\grid_files\\");
	    private int			xFrame, yFrame, xGrid, yGrid, stepCount, cellCount;

	    // enum
	    private enum tools
		  {
		  DEFAULT, CLEAR, FILL, SAVE
		  }

	    private tools			   tool		= tools.DEFAULT;

	    // collections
	    private Map<String, Integer>	   infos	= new HashMap<>();
	    private ObservableMap<String, Integer> obsInfos	= FXCollections.observableMap(infos);
	    private DirectoryStream<Path>	   dirStream;

	    // javafx Parent and nodes
	    private Scene			   scene;
	    private BorderPane			   root		= new BorderPane();
	    private ScrollPane			   patternsSp;
	    private Label			   stepCountLbl	= new Label("Step count : ");
	    private Label			   cellCountLbl	= new Label("Cell count : ");
	    private ScrollPane			   scrollP	= new ScrollPane();
	    private VBox			   patternsVb	= new VBox();
	    private BasGolCanvas		   frame	= new BasGolCanvas();
	    private VBox			   info;


	    @Override
	    public void start(Stage stage)
		  {

			xFrame = xGrid = XSTARTFRAME;
			yFrame = yGrid = YSTARTFRAME;
			stepCount = cellCount = 0;
			frame = new BasGolCanvas(this, xFrame, yFrame, xGrid, yGrid);

			scrollP.setContent(frame);
			scrollP.setPannable(false);
			scrollP.setHbarPolicy(ScrollBarPolicy.NEVER);
			scrollP.setVbarPolicy(ScrollBarPolicy.NEVER);
			scrollP.setVvalue(0.5);
			scrollP.setHvalue(0.5);
			scrollP.addEventFilter(ScrollEvent.ANY, new ZoomControl());
			scrollP.setMaxHeight(frame.getHeight());
			scrollP.setMaxWidth(frame.getWidth());

			root = new BorderPane();
			root.setPrefSize(XSTARTFRAME, YSTARTFRAME);
			root.setCenter(scrollP);
			root.setStyle("-fx-background-color : white");

			// set info/control panel
			obsInfos.addListener((MapChangeListener<String, Integer>) change -> {
			      stepCountLbl.setText("Step count : " + obsInfos.get(STEPCOUNT));
			      cellCountLbl.setText("Cell count : " + obsInfos.get(CELLCOUNT));
			});

			obsInfos.put(STEPCOUNT, stepCount);
			obsInfos.put(CELLCOUNT, cellCount);

			info = new VBox(stepCountLbl, cellCountLbl);
			info.setMinWidth(200);
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

			scene = new Scene(root);
			scene.addEventFilter(KeyEvent.ANY, new KeyControls());
			stage.setTitle(title);
			stage.setScene(scene);
			stage.show();

		  }


	    /**
	     * 
	     */
	    private void loadGridFiles()
		  {

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
										    patternsVb.getChildren().add(new PatternFrame(pattern));
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
								  frame.stepForward();
								  obsInfos.replace(STEPCOUNT, ++stepCount);
								  obsInfos.replace(CELLCOUNT, frame.getPattern().getCoords().values().size());
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
								  frame.getPattern().getCoords().clear();
								  frame.drawGrid();
							    }
						      break;
						}
					  case SPACE:
						{
						      if (event.getEventType() == KeyEvent.KEY_RELEASED)
							    {
								  scrollP.setPannable(!scrollP.isPannable());
								  frame.setMouseTransparent(!frame.isMouseTransparent());
								  scrollP.setCursor(scrollP.isPannable() ? Cursor.MOVE : Cursor.DEFAULT);

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
							    }
						      break;
						}

					  case L:
						{
						      if (event.getEventType() == KeyEvent.KEY_RELEASED)
							    {
								  if (new File("./grid_files/temp.xml").exists())
									{
									      try
										    {
											  FileManager fileManager = new FileManager(new Pattern("temp", frame.getGrid().length,
												      frame.getGrid()[0].length, frame.getPattern().getCoords()));

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
						      break;
						}

					  case V:
						{
						      if (event.getEventType() == KeyEvent.KEY_RELEASED)
							    {
								  frame.setSelectionMode(!frame.isSelectionMode());
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
	     * @return the obsInfos
	     */
	    public ObservableMap<String, Integer> getObsInfos()
		  {

			return obsInfos;
		  }


	    /**
	     * @param obsInfos
	     *              the obsInfos to set
	     */
	    public void setObsInfos(ObservableMap<String, Integer> obsInfos)
		  {

			this.obsInfos = obsInfos;
		  }

      }
