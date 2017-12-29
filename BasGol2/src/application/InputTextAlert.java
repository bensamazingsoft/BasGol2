
package application;

import javafx.scene.control.TextInputDialog;

public class InputTextAlert

        {

	      private String	reponse = "";
	      private TextInputDialog	dialog;


	      public InputTextAlert(String title)
		    {
			  dialog = new TextInputDialog();
			  dialog.setTitle(title);
			  dialog.setContentText(title);
			  //TODO filter textfield for special char
			  dialog.showAndWait();
		    }


	      public String reponse()
		    {

			  reponse = dialog.getEditor().getText().trim().replaceAll(" ", "_");
			  return reponse;
		    }
        }