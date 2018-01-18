
package alert;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ErrorAlert
        {

	      private Alert alert;


	      public ErrorAlert()
		    {
			  alert = new Alert(AlertType.ERROR, "An error has occured \n" );
			  alert.showAndWait();
		    }
	      
	      public ErrorAlert(Exception error)
		    {
			  alert = new Alert(AlertType.ERROR, "An error has occured \n" + error.toString());
			  alert.showAndWait();
		    }

        }
