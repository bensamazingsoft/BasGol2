
package model;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class PatternFrame extends StackPane
        {

	      private static final int    SIZE	= 100;
	      private static final String BLACK	= "black";
	      private static final String WHITE	= "WHITE";

	      private Pattern	    pattern;
	      private Canvas	    canvas;
	      private GraphicsContext	    graphic;
	      private File		    imageFile;
	      private Image		    image;

	      private BorderPane	    bp	= new BorderPane();
	      private Label		    nameLbl;


	      public PatternFrame(Pattern pattern) throws IOException
		    {

			  this.setStyle("-fx-background-color : white");

			  this.pattern = pattern;
			  imageFile = new File(".\\grid_files\\" + pattern.getName() + ".png");
			  imageFile.getParentFile().mkdirs();
			  nameLbl = new Label(pattern.getName() + "\t");
			  bp.setTop(nameLbl);
			  if (!imageFile.exists())
				{
				        createImage();
				}

			  image = new Image(imageFile.toURI().toURL().toString(), SIZE, SIZE, false, false);

			  bp.setCenter(new ImageView(image));

			  this.getChildren().add(bp);

		    }


	      private void createImage() throws IOException
		    {

			  // check size
			  canvas = new Canvas(pattern.getSize() * 5, pattern.getSize() * 5);
			  graphic = canvas.getGraphicsContext2D();

			  pattern.getCoords().forEach((coord, bool) -> {

				int x = coord.getX();
				int y = coord.getY();

				if (!bool)
				        {
					      graphic.setFill(Color.WHITE);
					      graphic.fillRect((x) * 5, (y) * 5, 5, 5);
				        } else
				        {
					      graphic.setFill(Color.BLACK);
					      graphic.fillRect((x) * 5, (y) * 5, 5, 5);
				        }
			  });

			  WritableImage wi = canvas.snapshot(null, null);
			  ImageIO.write(SwingFXUtils.fromFXImage(wi, null), "png", imageFile);

		    }

        }
