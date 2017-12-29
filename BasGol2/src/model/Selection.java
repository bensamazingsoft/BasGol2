
package model;

import java.util.TreeSet;

public class Selection
        {

	      private Coord	         from, to;
	      private TreeSet<Coord> coords = new TreeSet<>();
	      private int	         xSize, ySize;


	      public Selection()
		    {


		    }


	      public Selection(Coord start, Coord end)
		    {
			  if (!start.equals(end))
				{
				        this.from = (start.compareTo(end) < 0) ? start : end;
				        this.to = (start.compareTo(end) > 0) ? start : end;

				        if (from.getX() >= to.getX() && from.getY() <= to.getY())
					      {

						    int dX = from.getX() - to.getX();

						    from = new Coord(from.getX() - dX, from.getY());
						    to = new Coord(to.getX() + dX, to.getY());
					      }

				        for (int x = from.getX(); x <= to.getX(); x++)
					      {
						    for (int y = from.getY(); y <= to.getY(); y++)
							  {
								coords.add(new Coord(x, y));
							  }
					      }

				        xSize = coords.last().getX() - coords.first().getX() + 1;
				        ySize = coords.last().getY() - coords.first().getY() + 1;

				} else
				{
				        this.from = this.to = start;
				        coords.add(from);
				        xSize = 1;
				        ySize = 1;
				}
		    }


	      // {{{ fold start
	      /**
	       * @return the from
	       */
	      public Coord getFrom()
		    {

			  return from;
		    }


	      /**
	       * @param from
	       *                the from to set
	       */
	      public void setFrom(Coord from)
		    {

			  this.from = from;
		    }


	      /**
	       * @return the to
	       */
	      public Coord getTo()
		    {

			  return to;
		    }


	      /**
	       * @param to
	       *                the to to set
	       */
	      public void setTo(Coord to)
		    {

			  this.to = to;
		    }


	      /**
	       * @return the coords
	       */
	      public TreeSet<Coord> getCoords()
		    {

			  return coords;
		    }


	      /**
	       * @param coords
	       *                the coords to set
	       */
	      public void setCoords(TreeSet<Coord> coords)
		    {

			  this.coords = coords;
		    }
	      // }}}


	      /**
	       * @return the xSize
	       */
	      public int getxSize()
		    {

			  return xSize;
		    }


	      /**
	       * @param xSize
	       *                the xSize to set
	       */
	      public void setxSize(int xSize)
		    {

			  this.xSize = xSize;
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

        }
