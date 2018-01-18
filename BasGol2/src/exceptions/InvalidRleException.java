
package exceptions;

public class InvalidRleException extends Exception
      {

      private static final long serialVersionUID = 1L;




	    @Override
	    public String getMessage()
		  {

			return "The rle file is invalid";
		  }

      }
