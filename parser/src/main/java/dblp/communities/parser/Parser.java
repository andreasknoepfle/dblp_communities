package dblp.communities.parser;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class Parser {

	/**
	 * How found authors (names) map to their ids
	 */
	private HashMap<String, Integer> authorMappings;
	/**
	 * Parser storage - tempurary saves the authors of a parsed publication
	 */
	private HashSet<Integer> authorsOnPublication;
	/**
	 * Stores relations of the author ids
	 * year>author_from>author_to>num_publications
	 */
	private HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> relations;
	/**
	 * author id counter
	 */
	private int authorNumber = 0;
	// Year Splitting MetaInfo
	private int minYear = Integer.MAX_VALUE;
	private int maxYear = Integer.MIN_VALUE;
	/**
	 * Parser storage
	 */
	private int pubYear;
	/**
	 * Split Result into X parts sorted after the year
	 */
	private int yearSplitter;

	private class ConfigHandler extends DefaultHandler {

		private String Value;

		private String recordTag;

		private boolean insideTag;

		public void startElement(String namespaceURI, String localName,
				String rawName, Attributes atts) throws SAXException {

			if (insideTag = (rawName.equals("author") || rawName
					.equals("editor"))
					|| rawName.equals("year")) {
				Value = "";
				return;
			}
			if ((atts.getLength() > 0) && (atts.getValue("key") != null)) {
				recordTag = rawName;
			}
		}

		public void endElement(String namespaceURI, String localName,
				String rawName) throws SAXException {
			if (rawName.equals("author") || rawName.equals("editor")) {

				// Author gefunden "Value"
				if (authorMappings.get(Value) == null) {
					Integer author_id = new Integer(authorNumber);
					authorMappings.put(Value, author_id);
					authorsOnPublication.add(author_id);
					authorNumber++;
				} else {
					authorsOnPublication.add(authorMappings.get(Value));
				}

			}
			if (rawName.equals("year")) {
				pubYear = Integer.parseInt(Value);
				if (pubYear > maxYear) {
					maxYear = pubYear;
				}
				if (pubYear < minYear) {
					minYear = pubYear;
				}
			}
			if (rawName.equals(recordTag)) {
				// Record Tag gefunden fuege tmp personen zusammen
				for (Integer author_from : authorsOnPublication) {
					for (Integer author_to : authorsOnPublication) {
						
						// Verbindung nur in eine Richtung werten und nicht mit
						// sich selber verbinden
						if (author_from < author_to) {
							if (!relations.containsKey(pubYear)) {
								HashMap<Integer, HashMap<Integer, Integer>> newYear = new HashMap<Integer, HashMap<Integer, Integer>>();
								relations.put(pubYear, newYear);
							}
							if (relations.get(pubYear).containsKey(author_from)) {
								HashMap<Integer, Integer> author_from_map = relations
										.get(pubYear).get(author_from);
								if (author_from_map.containsKey(author_to)) {
									author_from_map.put(author_to,
											author_from_map.get(author_to) + 1);
								} else {
									author_from_map.put(author_to, new Integer(
											1));
								}
							} else {
								HashMap<Integer, Integer> relation = new HashMap<Integer, Integer>();

								relation.put(author_to, new Integer(1));
								relations.get(pubYear).put(author_from,
										relation);
							}

						}
					}
				}
				// Eintragen in Neo4J
				authorsOnPublication.clear();
			}
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (insideTag)
				Value += new String(ch, start, length);
		}

		private void Message(String mode, SAXParseException exception) {
			System.err.println(mode + " Line: " + exception.getLineNumber()
					+ " URI: " + exception.getSystemId() + "\n" + " Message: "
					+ exception.getMessage());
		}

		public void warning(SAXParseException exception) throws SAXException {

			Message("**Parsing Warning**\n", exception);
			throw new SAXException("Warning encountered");
		}

		public void error(SAXParseException exception) throws SAXException {

			Message("**Parsing Error**\n", exception);
			throw new SAXException("Error encountered");
		}

		public void fatalError(SAXParseException exception) throws SAXException {

			Message("**Parsing Fatal Error**\n", exception);
			throw new SAXException("Fatal Error encountered");
		}
	}

	public Parser(String uri) {
		this(uri, 0);
	}

	public Parser(String uri, int splitter) {

		authorMappings = new HashMap<String, Integer>();
		authorsOnPublication = new HashSet<Integer>();
		relations = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>();
		yearSplitter = splitter;
		try {
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			SAXParser parser = parserFactory.newSAXParser();
			ConfigHandler handler = new ConfigHandler();
			parser.getXMLReader().setFeature(
					"http://xml.org/sax/features/validation", true);
			parser.parse(new File(uri), handler);
		} catch (IOException e) {
			System.err.println("Error reading URI: " + e.getMessage());
		} catch (SAXException e) {
			System.err.println("Error in parsing: " + e.getMessage());
		} catch (ParserConfigurationException e) {
			System.err.println("Error in XML parser configuration: "
					+ e.getMessage());
		}

	}

	public HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> getRelations() {
		return relations;
	}

	public HashMap<String, Integer> getAuthorMappings() {
		return authorMappings;
	}

	public int getMinYear() {
		return minYear;
	}

	public int getMaxYear() {
		return maxYear;
	}

	public void printRelations(File parent) throws FileNotFoundException {

		int fileNum = 0;
		PrintStream p = null;
		for (int i = 0; i <= maxYear - minYear; i++) {
			int year = minYear + i;
			if (relations.containsKey(year)) {
				int pieceNum;
				if (yearSplitter != 0) {
					pieceNum = i / yearSplitter;
				} else {
					pieceNum = 0;
				}
				
				if (p == null || pieceNum != fileNum) {
					if (p != null) {
						p.flush();
						p.close();
					}
					p = new PrintStream(new File(parent, yearSplitter==0?new String(minYear+"-"+maxYear):String.valueOf(year + "-" + (year + yearSplitter - 1))));
					fileNum = pieceNum;
				}

				for (Integer author_from : relations.get(year).keySet()) {
					for (Integer author_to : relations.get(year).get(
							author_from).keySet()) {
						p.print(author_from);
						p.print(" ");
						p.print(author_to);
						p.print(" ");
						p.print(relations.get(year).get(author_from).get(
								author_to));
						p.println();
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		Parser p = null;
		if (args.length < 1) {
			System.err
					.println("Usage: java -jar dblp_communities.jar [input] [output-folder] [[splitter]] ");
			System.exit(0);
		} else if (args.length == 2) {
			p = new Parser(args[0]);
		} else if (args.length == 3) {
			int splitter = 0;
			try {
				splitter = Integer.valueOf(args[2]);
			} catch (NumberFormatException e) {
				System.err.println("Use an Integer splitter!");
				System.exit(0);
			}
			p = new Parser(args[0], splitter);
		}
		System.out.println("Printing community algorithm files.");
		File input = new File(args[1]);

		try {
			p.printRelations(input);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
}
