
package xml;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import model.GolPattern;

public class FileManager
        {

	      private JAXBContext  context;
	      private Marshaller   marshaller;
	      private Unmarshaller unMarshaller;
	      private GolPattern      pattern;
	      private File	       xmlFile;


	      public FileManager(File xmlFile) throws JAXBException
		    {
			  this.xmlFile = xmlFile;

			  context = JAXBContext.newInstance(GolPattern.class);
			  marshaller = context.createMarshaller();
			  unMarshaller = context.createUnmarshaller();

		    }


	      public FileManager(GolPattern pattern) throws JAXBException
		    {
			  this.pattern = pattern;
			  xmlFile = new File(".\\grid_files\\" + pattern.getName() + ".xml");
			  xmlFile.getParentFile().mkdirs();

			  context = JAXBContext.newInstance(GolPattern.class);
			  marshaller = context.createMarshaller();
			  unMarshaller = context.createUnmarshaller();

		    }


	      public void write() throws JAXBException
		    {

			  marshaller.marshal(pattern, xmlFile);

		    }


	      public GolPattern read() throws JAXBException
		    {

				        Object obj = unMarshaller.unmarshal(xmlFile);
				        if (obj instanceof GolPattern)
					      {

						    return (GolPattern) obj;
					      }
		

	      return null;
        }}
