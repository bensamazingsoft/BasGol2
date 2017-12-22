
package application;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.bind.JAXBException;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Coord;
import model.ErrorAlert;
import model.Pattern;
import model.PatternFrame;
import xml.FileManager;

public class Main extends Application
        {

	      private static final String	     VERSION      = "v0.1";
	      private static final String	     title        = "Ben's Amazing Soft - Game of Life 2 " + VERSION;
	      private static final String	     STEPCOUNT    = "STEPCOUNT";
	      private static final String	     CELLCOUNT    = "CELLCOUNT";
	      private static final int	     XSTARTFRAME  = 900;
	      private static final int	     YSTARTFRAME  = 600;

	      private static final int	     XGRID        = 900;

	      private int			     xFrame, yFrame, xGrid, yGrid, xCell, stepCount, cellCount, resizer;
	      private boolean[][]		     grid;
	      private Map<Coord, Boolean>	     buffer       = new HashMap<>();
	      private Set<Coord>		     toTest       = new HashSet<>();
	      private Map<String, Integer>	     infos        = new HashMap<>();
	      private ObservableMap<String, Integer> obsInfos     = FXCollections.observableMap(infos);
	      private Label			     stepCountLbl = new Label("Step count : ");
	      private Label			     cellCountLbl = new Label("Cell count : ");
	      private ScrollPane		     scrollP      = new ScrollPane();
	      private VBox			     patternsVb   = new VBox();
	      private Canvas		     frame        = new Canvas();
	      private VBox			     info;
	      private GraphicsContext		     graphic;
	      private Path			     gridFilesDir = Paths.get(".\\grid_files\\");
	      private DirectoryStream<Path>	     dirStream;


	      @Override
	      public void start(Stage stage)
		    {

			  resizer = 1;
			  xFrame = XSTARTFRAME;
			  yFrame = YSTARTFRAME;
			  xGrid = yGrid = XGRID;
			  xCell = xFrame / xGrid;
			  stepCount = cellCount = 0;
			  frame = new Canvas(xFrame, yFrame);
			  grid = new boolean[xGrid][yGrid];

			  // set grid image

			  frame.setOnMouseDragged(new MouseDragControls());
			  frame.setOnMousePressed(new MouseClickControls());
			  graphic = frame.getGraphicsContext2D();
			  graphic.setStroke(Color.GRAY);

			  scrollP.setContent(frame);
			  scrollP.setPannable(false);
			  scrollP.setHbarPolicy(ScrollBarPolicy.NEVER);
			  scrollP.setVbarPolicy(ScrollBarPolicy.NEVER);
			  scrollP.setVvalue(0.5);
			  scrollP.setHvalue(0.5);
			  scrollP.addEventFilter(ScrollEvent.ANY, new ZoomControl());

			  BorderPane root = new BorderPane();
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
			  info.setStyle("-fx-font-size : 15;" + "-fx-border-color : darkgrey");
			  info.setAlignment(Pos.TOP_LEFT);
			  info.setPadding(new Insets(10, 10, 10, 10));

			  // Load pattern files, if any exist in the folder
			  ScrollPane patternsSp = new ScrollPane(patternsVb);
			  patternsVb.setSpacing(5);
			  patternsSp.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
			  patternsSp.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
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
									      alert.setContentText(alert.getContentText() + "\n\t- " + path.toString());
									      alert.hide();
									      alert.show();
									      File file = path.toFile();
									      Pattern pattern = new FileManager(file).read();
									      PatternFrame frame = new PatternFrame(pattern);
									      patternsVb.getChildren().add(frame);

								        }
								alert.hide();
							  }
					      } catch (Exception e)
					      {
						    new ErrorAlert(e);
						    e.printStackTrace();
					      }
				}
			  info.getChildren().add(patternsSp);
			  root.setRight(info);

			  Scene scene = new Scene(root);
			  scene.addEventFilter(KeyEvent.ANY, new Controls());
			  stage.setTitle(title);
			  stage.setScene(scene);
			  stage.show();

		    }


	      public void stepForward()
		    {

			  stepCount++;
			  obsInfos.replace(STEPCOUNT, stepCount);

			  // store black cells position and all white
			  // cells surrounding (as only those have a
			  // chance to go black)
			  buffer.forEach((coord, bool) -> {
				int x = coord.getX();
				int y = coord.getY();

				toTest.add(new Coord(x - 1, y - 1));
				toTest.add(new Coord(x - 1, y));
				toTest.add(new Coord(x - 1, y + 1));
				toTest.add(coord);
				toTest.add(new Coord(x, y - 1));
				toTest.add(new Coord(x, y + 1));
				toTest.add(new Coord(x + 1, y - 1));
				toTest.add(new Coord(x + 1, y));
				toTest.add(new Coord(x + 1, y + 1));

			  });

			  toTest.forEach(coord -> {

				if (coord.getX() >= 0 && coord.getX() < xGrid && coord.getY() >= 0 && coord.getY() < yGrid)
				        {
					      testPosition(coord);
				        }
			  });

			  toTest.clear();

			  drawGrid();

		    }


	      // count black cells around tested position in grid and apply
	      // game of life rules to cell in buffer
	      private void testPosition(Coord coord)
		    {

			  int x = coord.getX();
			  int y = coord.getY();

			  int count = 0;
			  try
				{
				        count += (grid[x - 1][y - 1]) ? 1 : 0;
				} catch (ArrayIndexOutOfBoundsException e)
				{
				}
			  try
				{
				        count += (grid[x - 1][y]) ? 1 : 0;
				} catch (ArrayIndexOutOfBoundsException e)
				{
				}
			  try
				{
				        count += (grid[x - 1][y + 1]) ? 1 : 0;
				} catch (ArrayIndexOutOfBoundsException e)
				{
				}
			  try
				{
				        count += (grid[x][y - 1]) ? 1 : 0;
				} catch (ArrayIndexOutOfBoundsException e)
				{
				}
			  try
				{
				        count += (grid[x][y + 1]) ? 1 : 0;
				} catch (ArrayIndexOutOfBoundsException e)
				{
				}
			  try
				{
				        count += (grid[x + 1][y - 1]) ? 1 : 0;
				} catch (ArrayIndexOutOfBoundsException e)
				{
				}
			  try
				{
				        count += (grid[x + 1][y]) ? 1 : 0;
				} catch (ArrayIndexOutOfBoundsException e)
				{
				}
			  try
				{
				        count += (grid[x + 1][y + 1]) ? 1 : 0;
				} catch (ArrayIndexOutOfBoundsException e)
				{
				}

			  if (count < 2 || count > 3)
				{
				        buffer.put(new Coord(x, y), false);
				}
			  if (count == 3)
				{
				        buffer.put(new Coord(x, y), true);
				}
			  count = 0;
		    }


	      // flush buffer in grid and draw cells
	      public void drawGrid()
		    {

			  buffer.forEach((coord, bool) -> {

				int x = coord.getX();
				int y = coord.getY();

				grid[x][y] = bool;

				if (!bool)
				        {
					      graphic.setFill(Color.WHITE);
					      graphic.fillRect(x * xCell, y * xCell, xCell, xCell);
				        } else
				        {
					      graphic.setFill(Color.BLACK);
					      graphic.fillRect(x * xCell, y * xCell, xCell, xCell);
				        }
			  });

			  // Keep only black cells for next step
			  buffer.entrySet().removeIf(entry -> !entry.getValue());
			  obsInfos.replace(CELLCOUNT, buffer.values().size());
		    }


	      public void randomGrid()
		    {

			  for (int x = 0; x < xGrid; x++)
				{
				        for (int y = 0; y < yGrid; y++)
					      {

						    buffer.put(new Coord(x, y), new Random().nextBoolean());

					      }
				}
			  drawGrid();
		    }


	      public static void main(String[] args)
		    {

			  launch(args);
		    }

	      // filter all KeyEvent to block scrolPane implemented key binding, which sucks big times
	      public class Controls implements EventHandler<KeyEvent>
		    {

			  @SuppressWarnings("incomplete-switch")
			  @Override
			  public void handle(KeyEvent event)
				{

				        switch (event.getCode())
					      {
					      case RIGHT:
						    {

							  // KEY PRESSED enables repeat if key is kept down
							  if (event.getEventType() == KeyEvent.KEY_PRESSED)
								{
								        stepForward();
								}
							  break;
						    }

					      // KEY RELEASED is used because KEY TYPED keyCode is always UNDEFINED
					      case R:
						    {

							  if (event.getEventType() == KeyEvent.KEY_RELEASED)
								{
								        randomGrid();
								}
							  break;
						    }
					      case C:
						    {
							  if (event.getEventType() == KeyEvent.KEY_RELEASED)
								{
								        buffer.replaceAll((coord, bool) -> bool = false);
								        drawGrid();
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
										    Pattern pattern = new Pattern("temp", grid.length, buffer);
										    FileManager fileManager;
										    fileManager = new FileManager(pattern);
										    fileManager.write();
									      } catch (JAXBException e)
									      {
										    new ErrorAlert(e);
										    e.printStackTrace();
									      }
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
												FileManager fileManager;
												fileManager = new FileManager(new Pattern("temp", grid.length, buffer));
												buffer.replaceAll((coord, bool) -> bool = false);
												drawGrid();
												buffer.clear();
												buffer = fileManager.read().getCoords();
												drawGrid();
											  } catch (JAXBException e)
											  {
												new ErrorAlert(e);
												e.printStackTrace();
											  }
									      }
								}
							  break;
						    }

					      case P:
						    {
							  if (event.getEventType() == KeyEvent.KEY_PRESSED)
								{

								        zoomIn();

								}
							  break;
						    }
					      case M:
						    {
							  if (event.getEventType() == KeyEvent.KEY_PRESSED)
								{

								        zoomOut();
								}
							  break;
						    }
					      }
				        event.consume();

				}

		    }


	      private void zoomIn()
		    {

			  double xTrans = scrollP.getHvalue();
			  double yTrans = scrollP.getVvalue();

			  incResizer();
			  xFrame = XSTARTFRAME * resizer;
			  yFrame = YSTARTFRAME * resizer;
			  xCell = xFrame / xGrid;
			  yGrid = yFrame / xCell;
			  frame.setWidth(xFrame);
			  frame.setHeight(yFrame);
			  graphic.setFill(Color.WHITE);
			  graphic.fillRect(0, 0, xFrame, yFrame);

			  drawGrid();

			  scrollP.setHvalue(xTrans);
			  scrollP.setVvalue(yTrans);

		    }


	      private void zoomOut()
		    {

			  double xTrans = scrollP.getHvalue();
			  double yTrans = scrollP.getVvalue();

			  decResizer();

			  xFrame = XSTARTFRAME * resizer;
			  yFrame = YSTARTFRAME * resizer;
			  xCell = xFrame / xGrid;
			  yGrid = yFrame / xCell;
			  frame.setWidth(xFrame);
			  frame.setHeight(yFrame);
			  graphic.setFill(Color.WHITE);
			  graphic.fillRect(0, 0, xFrame, yFrame);

			  drawGrid();

			  scrollP.setHvalue(xTrans);
			  scrollP.setVvalue(yTrans);

		    }


	      public void incResizer()
		    {

			  if (resizer < 9)
				{
				        resizer++;
				}

		    }


	      public void decResizer()
		    {

			  if (resizer > 1)
				{
				        resizer--;
				}
		    }

	      public class MouseDragControls implements EventHandler<MouseEvent>
		    {

			  @Override
			  public void handle(MouseEvent e)
				{

				        int eX = (int) e.getX() / xCell;
				        int eY = (int) e.getY() / xCell;
				        Coord coord = new Coord(eX, eY);
				        if (coord.getX() >= 0 && coord.getX() < xGrid && coord.getY() >= 0 && coord.getY() < yGrid)
					      {
						    buffer.put(coord, (e.getButton() == MouseButton.PRIMARY) ? true : false);
					      }

				        drawGrid();
				}

		    }

	      public class MouseClickControls implements EventHandler<MouseEvent>
		    {

			  @Override
			  public void handle(MouseEvent e)
				{

				        int eX = (int) e.getX() / xCell;
				        int eY = (int) e.getY() / xCell;

				        buffer.put(new Coord(eX, eY), !grid[eX][eY]);

				        drawGrid();

				}

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

        }
