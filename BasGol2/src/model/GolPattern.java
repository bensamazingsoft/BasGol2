
package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlRootElement;

import alert.ErrorAlert;
import exceptions.InvalidRleException;

@XmlRootElement
public class GolPattern implements Serializable
      {

	    private static final long	serialVersionUID = 2144121534639643283L;
	    private static final String	NAME		 = "name";
	    private static final String	AUTHOR		 = "author";
	    private static final String	COMMENT		 = "comment";
	    private static final String	RULE		 = "rule";
	    private static final String	RULES		 = "rules";
	    private static final String	COORDS		 = "coords";
	    private static final String	SIZE		 = "size";

	    private int			xSize, ySize;
	    private String		name, author, comment, rule, source;

	    private Map<Coord, Boolean>	coords		 = new HashMap<>();
	    private Map<Coord, Boolean>	tempCoords	 = new HashMap<>();


	    public GolPattern()
		  {
			this.name = "new Pattern";
			this.author = "Ben's Amazing Software";
			this.comment = "Created " + LocalDate.now();
			this.xSize = 0;
			this.ySize = 0;

		  }


	    public GolPattern(String name, int xSize, int ySize, Map<Coord, Boolean> coords)
		  {

			this.name = name;
			this.xSize = xSize;
			this.ySize = ySize;
			this.coords = coords;

		  }


	    public GolPattern(File rleFile) throws InvalidRleException
		  {
			this();
			try
			      {
				    source = makeSource(rleFile);
			      } catch (IOException e)
			      {
				    new ErrorAlert(e);
			      }
			Map<String, String> matchResults = new HashMap<>();
			Map<String, String> regexMatchers = new HashMap<>();
			regexMatchers.put(NAME, "#N .*");
			regexMatchers.put(COMMENT, "#[cC] .*");
			regexMatchers.put(AUTHOR, "#O .*");
			regexMatchers.put(RULES, "#r .*");
			regexMatchers.put(RULE, "#rule +.*");
			regexMatchers.put(SIZE, "x = \\d+, y = \\d+");
			regexMatchers.put(COORDS, "[\\dob]*[\\$][\\dob\\n\\r]*");

			// cut text in relevant parts
			regexMatchers.forEach((elem, regex) -> {

			      String result = "";
			      Pattern pattern = Pattern.compile(regex);
			      Matcher matcher = pattern.matcher(source);

			      while (matcher.find())
				    {
					  result += matcher.group();
				    }
			      matchResults.put(elem, result);

			});

			if (matchResults.get(COORDS).length() == 0)
			      throw new InvalidRleException();

			this.name = matchResults.get(NAME).length() > 0 ? matchResults.get(NAME).replace("#N ", "").trim() : this.name;
			this.author = matchResults.get(AUTHOR).length() > 0 ? matchResults.get(AUTHOR).replace("#O ", "").trim() : this.author;
			this.comment = matchResults.get(COMMENT).length() > 0 ? matchResults.get(COMMENT).replaceAll("#[cC] ", "").trim() : this.comment;
			this.comment = matchResults.get(RULE).length() > 0 ? matchResults.get(RULE).replaceAll("#rule ", "").trim() : this.rule;
			this.comment = matchResults.get(RULES).length() > 0 ? matchResults.get(RULES).replaceAll("#r ", "").trim() : this.rule;

			this.xSize = matchResults.get(SIZE).length() > 0 ? Integer.valueOf(matchResults.get(SIZE).split(",")[0].replaceAll("x = ", "").trim()) : 0;
			this.ySize = matchResults.get(SIZE).length() > 0 ? Integer.valueOf(matchResults.get(SIZE).split(",")[1].replaceAll("y = ", "").trim()) : 0;

			// read live cells coordinates for each rows
			String[] rows = matchResults.containsKey(COORDS) ? matchResults.get(COORDS).split("\\$") : new String[0];
			int y = 0;
			for (int rowId = 0; rowId < rows.length; rowId++)
			      {
				    int index = 0;

				    Matcher matcher = Pattern.compile("\\d*[ob]").matcher(rows[rowId]);
				    Matcher endOfRow = Pattern.compile("\\d+$").matcher(rows[rowId]);

				    while (matcher.find())
					  {
						int delta = matcher.group().length() > 1 ? Integer.valueOf(matcher.group().replaceAll("\\D", "").trim()) : 1;
						if (matcher.group().contains("o"))
						      {
							    for (int x = index; x < index + delta; x++)
								  {
									coords.put(new Coord(x, y), true);
								  }
						      }
						index += delta;
					  }
				    // add blank lines if needed
				    while (endOfRow.find())
					  {
						y += Integer.valueOf(endOfRow.group()) - 1;
					  }
				    y++;
			      }
		  }


	    private String makeSource(File rleFile) throws IOException
		  {

			String str = "";
			BufferedReader br = new BufferedReader(new FileReader(rleFile));

			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null)
			      {
				    if (line.length() != 0)
					  {
						sb.append(line);
						sb.append("\n");
					  }
				    line = br.readLine();
			      }
			str = sb.toString();

			br.close();
			return str;
		  }


	    // rotates pattern by 90° clockwise
	    public void rotate()
		  {

			tempCoords.clear();

			// make pattern shape a square, it won't work if it isn't
			xSize = Math.max(xSize, ySize);
			ySize = xSize;

			coords.forEach((coord, bool) -> {

			      tempCoords.put(new Coord(ySize - coord.getY() + 1, coord.getX()), bool);

			});

			coords = new HashMap<>(tempCoords);

		  }


	    // shift coordinates to new origin
	    public void shiftOrigin(Coord ghostOg)
		  {

			tempCoords.clear();

			coords.forEach((coord, bool) -> {

			      tempCoords.put(new Coord(coord.getX() + ghostOg.getX(), coord.getY() + ghostOg.getY()), bool);

			});

			coords = new HashMap<>(tempCoords);

		  }


	    /**
	     * @return the name
	     */
	    public String getName()
		  {

			return name;
		  }


	    /**
	     * @param name
	     *              the name to set
	     */
	    public void setName(String name)
		  {

			this.name = name;
		  }


	    /**
	     * @return the coords
	     */
	    public Map<Coord, Boolean> getCoords()
		  {

			return coords;
		  }


	    /**
	     * @param coords
	     *              the coords to set
	     */
	    public void setCoords(Map<Coord, Boolean> coords)
		  {

			this.coords = coords;
		  }


	    /**
	     * @return the ySize
	     */
	    public int getySize()
		  {

			return ySize;
		  }


	    /**
	     * @param ySize
	     *              the ySize to set
	     */
	    public void setySize(int ySize)
		  {

			this.ySize = ySize;
		  }


	    /**
	     * @return the xSize
	     */
	    public int getxSize()
		  {

			return xSize;
		  }


	    /**
	     * @param xSize
	     *              the xSize to set
	     */
	    public void setxSize(int xSize)
		  {

			this.xSize = xSize;
		  }


	    /**
	     * @return the author
	     */
	    public String getAuthor()
		  {

			return author;
		  }


	    /**
	     * @param author
	     *              the author to set
	     */
	    public void setAuthor(String author)
		  {

			this.author = author;
		  }


	    /**
	     * @return the comment
	     */
	    public String getComment()
		  {

			return comment;
		  }


	    /**
	     * @param comment
	     *              the comment to set
	     */
	    public void setComment(String comment)
		  {

			this.comment = comment;
		  }


	    /**
	     * @return the rule
	     */
	    public String getRule()
		  {

			return rule;
		  }


	    /**
	     * @param rule
	     *              the rule to set
	     */
	    public void setRule(String rule)
		  {

			this.rule = rule;
		  }

      }
