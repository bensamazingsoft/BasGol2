
package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Pattern implements Serializable
      {

	    /**
       * 
       */
      private static final long serialVersionUID = 2144121534639643283L;
	    private int			xSize, ySize;
	    private String		name;

	    private Map<Coord, Boolean>	coords	   = new HashMap<>();
	    private Map<Coord, Boolean>	tempCoords = new HashMap<>();


	    public Pattern()
		  {
			this.name = "default";
			this.xSize = 0;
			this.ySize = 0;

		  }


	    public Pattern(String name, int xSize, int ySize, Map<Coord, Boolean> coords)
		  {

			this.name = name;
			this.xSize = xSize;
			this.ySize = ySize;
			this.coords = coords;

		  }


	    // rotates pattern by 90° clockwise
	    public void rotate()
		  {

			tempCoords.clear();
			
			//make pattern shape a square, it won't work if it isn't
			xSize = Math.max(xSize, ySize);
			ySize = xSize;

			coords.forEach((coord, bool) -> {

			      tempCoords.put(new Coord(ySize - coord.getY() + 1, coord.getX()), bool);

			
			});

			coords = new HashMap<>(tempCoords);

		  }


	    // shift coordinates to new origin
	    public void shiftOrigin(Coord ghostOg)
		  {

			tempCoords.clear();

			coords.forEach((coord, bool) -> {

			      tempCoords.put(new Coord(coord.getX() + ghostOg.getX(), coord.getY() + ghostOg.getY()), bool);

			});

			coords = new HashMap<>(tempCoords);

		  }


	    /**
	     * @return the name
	     */
	    public String getName()
		  {

			return name;
		  }


	    /**
	     * @param name
	     *              the name to set
	     */
	    public void setName(String name)
		  {

			this.name = name;
		  }


	    /**
	     * @return the coords
	     */
	    public Map<Coord, Boolean> getCoords()
		  {

			return coords;
		  }


	    /**
	     * @param coords
	     *              the coords to set
	     */
	    public void setCoords(Map<Coord, Boolean> coords)
		  {

			this.coords = coords;
		  }


	    /**
	     * @return the ySize
	     */
	    public int getySize()
		  {

			return ySize;
		  }


	    /**
	     * @param ySize
	     *              the ySize to set
	     */
	    public void setySize(int ySize)
		  {

			this.ySize = ySize;
		  }


	    /**
	     * @return the xSize
	     */
	    public int getxSize()
		  {

			return xSize;
		  }


	    /**
	     * @param xSize
	     *              the xSize to set
	     */
	    public void setxSize(int xSize)
		  {

			this.xSize = xSize;
		  }

      }
