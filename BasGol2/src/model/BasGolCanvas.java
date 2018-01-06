
package model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import application.Main;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;

public class BasGolCanvas extends Canvas
      {

	    private int				    resizer	     = 1;
	    private int				    xCell;
	    private int				    xFrame;
	    private int				    yFrame;
	    private int				    startXFrame;
	    private int				    startYFrame;
	    private SimpleIntegerProperty	    cellCountIntProp = new SimpleIntegerProperty(0);
	    private Pattern			    pattern	     = new Pattern();
	    private Pattern			    ghostPattern     = new Pattern();
	    private boolean[][]			    grid;
	    private boolean			    selectionMode    = false;
	    private boolean			    gridOn	     = false;

	    private Selection			    selection	     = new Selection();
	    private Coord			    selStart;

	    private GraphicsContext		    graphic;

	    private Set<Coord>			    toTest	     = new HashSet<>();

	    private LinkedList<Map<Coord, Boolean>> history	     = new LinkedList<>();


	    public BasGolCanvas()
		  {
			super();

			this.setOnMouseDragged(new MouseDragControls());
			this.setOnMousePressed(new MouseClickControls());
			this.setOnDragOver(new DragOverControls());
			this.setOnDragDropped(new DragDroppedControls());

			pattern = new Pattern();
			pattern.setName("temp");

			graphic = this.getGraphicsContext2D();
		  }


	    public BasGolCanvas(int x, int y)
		  {
			this();
			this.setWidth(x);
			this.setHeight(y);
		  }


	    public BasGolCanvas(Main app, int xFrame, int yFrame, int xGrid, int yGrid)
		  {

			this(xFrame, yFrame);

			this.xFrame = startXFrame = xFrame;
			this.yFrame = startYFrame = yFrame;

			pattern.setxSize(xGrid);
			pattern.setySize(yGrid);
			grid = new boolean[xGrid][yGrid];
			xCell = xFrame / xGrid;

		  }


	    public void stepForward()
		  {
			
			history.add(new HashMap<Coord, Boolean>(pattern.getCoords()));
			
			// store black cells position and all white
			// cells surrounding (as only those have a
			// chance to go black)
			pattern.getCoords().forEach((coord, bool) -> {
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

			      if (coord.getX() >= 0 && coord.getX() < grid.length && coord.getY() >= 0 && coord.getY() < grid.length)
				    {
					  testPosition(coord);
				    }
			});

			toTest.clear();

			drawGrid();

			
		  }


	    public void stepBackward()
		  {

			if (!history.isEmpty())
			      {
				    pattern.getCoords().forEach((coord, bool) -> {
					  grid[coord.getX()][coord.getY()] = false;
				    });
				    pattern.setCoords(history.pollLast());
				    selection = new Selection();
				    drawGrid();
			      }
		  }


	    // count black cells around tested position in grid and apply
	    // game of life rules to cell in buffer
	    private void testPosition(Coord coord)
		  {

			int x = coord.getX();
			int y = coord.getY();

			int count = 0;

			// catch ArrayIndexOutOfBoundsException but do nothing cause result is still ok
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

			if ((count < 2 || count > 3) && (x < grid.length && y < grid[x].length))
			      {
				    pattern.getCoords().put(new Coord(x, y), false);
			      }
			if (count == 3 && x < grid.length && y < grid[x].length)
			      {
				    pattern.getCoords().put(new Coord(x, y), true);
			      }
			count = 0;
		  }


	    // flush buffer in grid and draw cells
	    public void drawGrid()
		  {

			graphic.setFill(Color.WHITE);
			graphic.fillRect(0, 0, getWidth(), getHeight());

			pattern.getCoords().forEach((coord, bool) -> {

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
			pattern.getCoords().entrySet().removeIf(entry -> !entry.getValue());

			// draw selection, if any
			if (!selection.getCoords().isEmpty())
			      {
				    graphic.setFill(Color.rgb(255, 0, 0, 0.2));
				    selection.getCoords().forEach(coord -> {

					  graphic.fillRect(coord.getX() * xCell, coord.getY() * xCell, xCell, xCell);
				    });

			      }

			cellCountIntProp.setValue(pattern.getCoords().values().size());

			// draw ghost, if any
			if (!ghostPattern.getCoords().isEmpty())
			      {

				    graphic.setFill(Color.rgb(128, 128, 128, 0.5));
				    ghostPattern.getCoords().forEach((coord, bool) -> {

					  graphic.fillRect(coord.getX() * xCell, coord.getY() * xCell, xCell, xCell);
				    });
			      }

			// draw gridlines, if turned on
			if (gridOn)
			      {
				    graphic.setStroke(Color.GRAY);
				    for (int x = 0; x < xFrame; x += xCell)
					  {

						graphic.strokeLine(x, 0, x, yFrame);

					  }
				    for (int y = 0; y < yFrame; y += xCell)
					  {

						graphic.strokeLine(0, y, xFrame, y);
					  }
			      }
		  }


	    public void zoomIn()
		  {

			incResizer();
			xFrame = startXFrame * resizer;
			yFrame = startYFrame * resizer;
			xCell = xFrame / grid.length;
			this.setWidth(xFrame);
			this.setHeight(yFrame);
			graphic.setFill(Color.WHITE);
			graphic.fillRect(0, 0, xFrame, yFrame);

			drawGrid();

		  }


	    public void zoomOut()
		  {

			decResizer();

			xFrame = startXFrame * resizer;
			yFrame = startYFrame * resizer;
			xCell = xFrame / grid.length;
			this.setWidth(xFrame);
			this.setHeight(yFrame);
			graphic.setFill(Color.WHITE);
			graphic.fillRect(0, 0, xFrame, yFrame);

			drawGrid();

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


	    public void randomGrid()
		  {

			for (int x = 0; x < grid.length; x++)
			      {
				    for (int y = 0; y < grid[x].length; y++)
					  {

						pattern.getCoords().put(new Coord(x, y), new Random().nextBoolean());

					  }
			      }
			drawGrid();
		  }

	    public class MouseDragControls implements EventHandler<MouseEvent>
		  {

			@Override
			public void handle(MouseEvent e)
			      {

				    int eX = (int) e.getX() / xCell;
				    int eY = (int) e.getY() / xCell;
				    Coord coord = new Coord(eX, eY);
				    if (coord.getX() >= 0 && coord.getX() < grid.length && coord.getY() >= 0 && coord.getY() < grid[0].length)
					  {
						if (!selectionMode)
						      {
							    pattern.getCoords().put(coord, (e.getButton() == MouseButton.PRIMARY) ? true : false);
						      } else
						      {
							    selection = new Selection(selStart, coord);
						      }

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

				    if (!selectionMode)
					  {
						pattern.getCoords().put(new Coord(eX, eY), !grid[eX][eY]);
					  } else
					  {
						selStart = new Coord(eX, eY);
						selection = new Selection(selStart, selStart);
					  }

				    drawGrid();

			      }

		  }

	    public class DragOverControls implements EventHandler<DragEvent>
		  {

			@Override
			public void handle(DragEvent evt)
			      {

				    if (evt.getGestureSource() != this && evt.getDragboard().hasContent(PatternFrame.dataFormat))
					  {
						evt.acceptTransferModes(TransferMode.ANY);
						Coord ghostOg = new Coord((int) evt.getX() / xCell, (int) evt.getY() / xCell);
						ghostPattern = (Pattern) evt.getDragboard().getContent(PatternFrame.dataFormat);
						ghostPattern.shiftOrigin(ghostOg);

						drawGrid();
						evt.consume();
					  }

			      }

		  }

	    public class DragDroppedControls implements EventHandler<DragEvent>
		  {

			@Override
			public void handle(DragEvent evt)
			      {

				    if (evt.getGestureSource() != this && evt.getDragboard().hasContent(PatternFrame.dataFormat))
					  {
						evt.acceptTransferModes(TransferMode.ANY);

						ghostPattern.getCoords().forEach((coord, bool) -> {

						      if (ghostPattern.getCoords().values().size() != 0 && coord.getX() < grid.length && coord.getY() < grid[0].length)
							    {
								  pattern.getCoords().put(coord, bool);
							    }

						});

						ghostPattern = new Pattern();

						drawGrid();

						evt.consume();
					  }

			      }

		  }


	    // {{{ fold start

	    /**
	     * @return the selectionMode
	     */
	    public boolean isSelectionMode()
		  {

			return selectionMode;
		  }


	    /**
	     * @param selectionMode
	     *              the selectionMode to set
	     */
	    public void setSelectionMode(boolean selectionMode)
		  {

			this.selectionMode = selectionMode;
		  }


	    /**
	     * @return the grid
	     */
	    public boolean[][] getGrid()
		  {

			return grid;
		  }


	    /**
	     * @param grid
	     *              the grid to set
	     */
	    public void setGrid(boolean[][] grid)
		  {

			this.grid = grid;
		  }


	    /**
	     * @return the pattern
	     */
	    public Pattern getPattern()
		  {

			return pattern;
		  }


	    /**
	     * @param pattern
	     *              the pattern to set
	     */
	    public void setPattern(Pattern pattern)
		  {

			this.pattern = pattern;
		  }


	    /**
	     * @return the resizer
	     */
	    public int getResizer()
		  {

			return resizer;
		  }


	    /**
	     * @param resizer
	     *              the resizer to set
	     */
	    public void setResizer(int resizer)
		  {

			this.resizer = resizer;
		  }
	    // }}}


	    /**
	     * @return the selection
	     */
	    public Selection getSelection()
		  {

			return selection;
		  }


	    /**
	     * @param selection
	     *              the selection to set
	     */
	    public void setSelection(Selection selection)
		  {

			this.selection = selection;
		  }


	    /**
	     * @return the cellCount
	     */
	    public SimpleIntegerProperty getCellCountIntProp()
		  {

			return cellCountIntProp;
		  }


	    /**
	     * @return the gridOn
	     */
	    public boolean isGridOn()
		  {

			return gridOn;
		  }


	    /**
	     * @param gridOn
	     *              the gridOn to set
	     */
	    public void setGridOn(boolean gridOn)
		  {

			this.gridOn = gridOn;
		  }


	    /**
	     * @return the history
	     */
	    public LinkedList<Map<Coord, Boolean>> getHistory()
		  {

			return history;
		  }


	    /**
	     * @param history
	     *              the history to set
	     */
	    public void setHistory(LinkedList<Map<Coord, Boolean>> history)
		  {

			this.history = history;
		  }

      }
