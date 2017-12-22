
package model;

public class Coord implements Comparable<Object>
        {

	      private int x, y;


	      public Coord(){
		    this.x = 0;
		    this.y = 0;
	      }
	      
	      public Coord(int x, int y)
		    {
			  this.x = x;
			  this.y = y;
		    }


	      /*
	       * (non-Javadoc)
	       * 
	       * @see java.lang.Object#toString()
	       */
	      @Override
	      public String toString()
		    {

			  return "X = " + x + " Y = " + y;
		    }


	      @Override
	      public boolean equals(Object arg)
		    {

			  Coord other = (Coord) arg;
			  return (this.x == other.getX() && this.y == other.getY());
		    }


	      /**
	       * @return the x
	       */
	      public int getX()
		    {

			  return x;
		    }


	      /**
	       * @param x
	       *                the x to set
	       */
	      public void setX(int x)
		    {

			  this.x = x;
		    }


	      /**
	       * @return the y
	       */
	      public int getY()
		    {

			  return y;
		    }


	      /**
	       * @param y
	       *                the y to set
	       */
	      public void setY(int y)
		    {

			  this.y = y;
		    }


	      @Override
	      public int compareTo(Object arg)
		    {

			  Coord other = (Coord) arg;

			  return (this.x - other.getX() != 0) ? this.x - other.getX() : this.y - other.getY();

		    }


	      /* (non-Javadoc)
	       * @see java.lang.Object#hashCode()
	       */
	      @Override
	      public int hashCode()
		    {
			return (toString()).hashCode();
		    }

        }
