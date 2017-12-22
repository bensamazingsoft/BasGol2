
package xml;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import model.Pattern;

public class FileManager
        {

	      private JAXBContext  context;
	      private Marshaller   marshaller;
	      private Unmarshaller unMarshaller;
	      private Pattern      pattern;
	      private File	       xmlFile;


	      public FileManager(File xmlFile) throws JAXBException
		    {
			  this.xmlFile = xmlFile;

			  context = JAXBContext.newInstance(Pattern.class);
			  marshaller = context.createMarshaller();
			  unMarshaller = context.createUnmarshaller();

		    }


	      public FileManager(Pattern pattern) throws JAXBException
		    {
			  this.pattern = pattern;
			  xmlFile = new File(".\\grid_files\\" + pattern.getName() + ".xml");
			  xmlFile.getParentFile().mkdirs();

			  context = JAXBContext.newInstance(Pattern.class);
			  marshaller = context.createMarshaller();
			  unMarshaller = context.createUnmarshaller();

		    }


	      public void write() throws JAXBException
		    {

			  marshaller.marshal(pattern, xmlFile);

		    }


	      public Pattern read() throws JAXBException
		    {

				        Object obj = unMarshaller.unmarshal(xmlFile);
				        if (obj instanceof Pattern)
					      {

						    return (Pattern) obj;
					      }
		

	      return null;
        }}
