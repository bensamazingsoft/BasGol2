
package model;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Pattern
        {

	      private int		    size;
	      private String	    name;

	      private Map<Coord, Boolean> coords = new HashMap<>();


	      public Pattern()
		    {
			  this.name = "default";
			  this.size = 10;

		    }


	      public Pattern(String name, int size, Map<Coord, Boolean> coords)
		    {

			  this.name = name;
			  this.size = size;
			  this.coords = coords;

		    }


	      /**
	       * @return the size
	       */
	      public int getSize()
		    {

			  return size;
		    }


	      /**
	       * @param size
	       *                the size to set
	       */
	      public void setSize(int size)
		    {

			  this.size = size;
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
        }
