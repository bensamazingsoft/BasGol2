
package model;

import java.util.Optional;
import java.util.function.UnaryOperator;

import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.TextInputDialog;

public class InputTextAlert

      {

	    private TextInputDialog dialog;


	    public InputTextAlert(String title)
		  {
			dialog = new TextInputDialog();
			dialog.setTitle(title);
			dialog.setContentText(title);
			dialog.getEditor().setTextFormatter(new TextFormatter<String>(getFilter()));

		  }


	    public String reponse()
		  {

			Optional<String> result = dialog.showAndWait();

			return result.isPresent() ? result.get().trim().replaceAll(" ", "_") : "";
		  }


	    private static UnaryOperator<Change> getFilter()
		  {

			return change -> {
			      String text = change.getText();

			      if (!change.isContentChange())
				    {
					  return change;
				    }

			      if (text.matches("[a-zA-Z0-9 ]*") || text.isEmpty())
				    {
					  return change;
				    }
			      return null;
			};
		  }

      }