
package model;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Pattern
        {

	      private int		    xSize, ySize;
	      private String	    name;

	      private Map<Coord, Boolean> coords = new HashMap<>();


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




	      /**
	       * @return the name
	       */
	      public String getName()
		    {

			  return name;
		    }


	      /**
	       * @param name
	       *                the name to set
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
	       *                the coords to set
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
	       * @param ySize the ySize to set
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
	       * @param xSize the xSize to set
	       */
	      public void setxSize(int xSize)
	              {
	      
	      	      this.xSize = xSize;
	              }
        }
