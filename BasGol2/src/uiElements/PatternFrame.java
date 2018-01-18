
package uiElements;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import model.GolPattern;

public class PatternFrame extends StackPane
      {

	    private static final int SIZE	= 100;

	    private GolPattern	     pattern;
	    private Canvas	     canvas;
	    private GraphicsContext  graphic;
	    private File	     imageFile;
	    private Image	     image;
	    private ImageView	     rotateImg	= new ImageView(new Image(getClass().getResourceAsStream("/images/rotateIcon2.png")));
	    public static DataFormat dataFormat	= new DataFormat("BasGol2.Pattern");

	    private BorderPane	     bp		= new BorderPane();
	    private BorderPane	     top	= new BorderPane();
	    private HBox	     topBut	= new HBox();
	    private Button	     rotateBut;
	    private Label	     nameLbl;

	    
	    public PatternFrame(){
		  
	    }

	    public PatternFrame(GolPattern pattern) throws IOException
		  {

			this.setStyle("-fx-background-color : white");

			this.setOnDragDetected(new DragControls());

			this.pattern = pattern;
			imageFile = new File(".\\grid_files\\" + pattern.getName() + ".png");
			imageFile.getParentFile().mkdirs();
			nameLbl = new Label(pattern.getName() + "\t");
			bp.setTop(top);
			top.setLeft(nameLbl);
			rotateBut = new Button("", rotateImg);
			rotateBut.setPadding(new Insets(0, 0, 0, 0));
			topBut.getChildren().add(rotateBut);

			top.setRight(topBut);
			if (!imageFile.exists())
			      {
				    createImage();
			      }

			image = new Image(imageFile.toURI().toURL().toString(), SIZE, SIZE, true, false);
			ImageView view = new ImageView(image);

			bp.setCenter(view);

			this.getChildren().add(bp);

			rotateBut.setOnAction(e -> {

			      view.setRotate(view.getRotate() + 90);

			      pattern.rotate();

			});
		  }


	    private void createImage() throws IOException
		  {

			// check size
			canvas = new Canvas(pattern.getxSize() * 5, pattern.getySize() * 5);
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

	    public class DragControls implements EventHandler<MouseEvent>
		  {

			@Override
			public void handle(MouseEvent evt)
			      {

				    Dragboard dragBoard = startDragAndDrop(TransferMode.ANY);
				    ClipboardContent dragboardContent = new ClipboardContent();
				    dragboardContent.put(dataFormat, pattern);
				    dragBoard.setContent(dragboardContent);

				    evt.consume();
			      }

		  }


	    /**
	     * @return the topBut
	     */
	    public HBox getTopBut()
		  {

			return topBut;
		  }


	    /**
	     * @param topBut
	     *              the topBut to set
	     */
	    public void setTopBut(HBox topBut)
		  {

			this.topBut = topBut;
		  }

	    
	    /**
	     * @return the pattern
	     */
	    public GolPattern getPattern()
	          {
	    
	    	    return pattern;
	          }

	    
	    /**
	     * @param pattern the pattern to set
	     */
	    public void setPattern(GolPattern pattern)
	          {
	    
	    	    this.pattern = pattern;
	          }

      }
