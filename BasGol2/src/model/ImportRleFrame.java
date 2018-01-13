
package model;

import java.io.File;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class ImportRleFrame
      {

	    boolean success = false;
	    
	    GolPattern pattern;
	    File    file;

	    
	    FileChooser chooser = new FileChooser();

	    
	    public ImportRleFrame(){
		  
		  chooser.setTitle("Please select .rle file to import");
		  chooser.setSelectedExtensionFilter(new ExtensionFilter("RLE", "*.rle"));
		  
		  file = chooser.showOpenDialog(null);
		  
	    }
	    
	    
	    
	    
	    /**
	     * @return the pattern
	     */
	    public GolPattern getPattern()
		  {

			return pattern;
		  }


	    /**
	     * @param pattern
	     *              the pattern to set
	     */
	    public void setPattern(GolPattern pattern)
		  {

			this.pattern = pattern;
		  }




	    
	    /**
	     * @return the success
	     */
	    public boolean isSuccess()
	          {
	    
	    	    return success;
	          }




	    
	    /**
	     * @param success the success to set
	     */
	    public void setSuccess(boolean success)
	          {
	    
	    	    this.success = success;
	          }

      }
