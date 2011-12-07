package dblp.communities.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.neo4j.graphdb.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import dblp.communities.db_interface.DBConnector;

public class Parser {

	/**
	 * How found authors (names) map to their ids
	 */
	private HashMap<String, Long> authorMappings;
	/**
	 * Parser storage - tempurary saves the authors of a parsed publication
	 */
	private HashSet<Long> authorsOnPublication;
	/**
	 * Stores relations of the author ids
	 * year>author_from>author_to>num_publications
	 */
	private HashMap<Integer, HashMap<Long, HashMap<Long, Integer>>> relations;
	
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
	/**
	 * Neo4J Connector
	 * 
	 */
	private DBConnector dbconnector;
	private long authorNumber=0;

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
					Long author_id;
					if(dbconnector!=null) {
						Node n=dbconnector.createAuthor(Value);
						author_id = n.getId();
					} else {
						author_id = authorNumber;
						authorNumber++;
					}
					authorMappings.put(Value, author_id);
					authorsOnPublication.add(author_id);
					
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
				for (Long author_from : authorsOnPublication) {
					for (Long author_to : authorsOnPublication) {
						
						// Verbindung nur in eine Richtung werten und nicht mit
						// sich selber verbinden
						if (author_from < author_to) {
							if (!relations.containsKey(pubYear)) {
								HashMap<Long, HashMap<Long, Integer>> newYear = new HashMap<Long, HashMap<Long, Integer>>();
								relations.put(pubYear, newYear);
							}
							if (relations.get(pubYear).containsKey(author_from)) {
								HashMap<Long, Integer> author_from_map = relations
										.get(pubYear).get(author_from);
								if (author_from_map.containsKey(author_to)) {
									author_from_map.put(author_to,
											author_from_map.get(author_to) + 1);
									if(dbconnector!=null) {
										dbconnector.addPublicationYear(author_from, author_to, pubYear);
									}
								} else {
									author_from_map.put(author_to, new Integer(
											1));
									if(dbconnector!=null) {
										dbconnector.createPublication(author_from, author_to,pubYear);
									}
								}
							} else {
								HashMap<Long, Integer> relation = new HashMap<Long, Integer>();

								relation.put(author_to, new Integer(1));
								relations.get(pubYear).put(author_from,
										relation);
								if(dbconnector!=null) {
									dbconnector.createPublication(author_from, author_to,pubYear);
								}
							}

						}
					}
				}
			
				
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


	public Parser(String uri, int splitter, DBConnector connector) {

		dbconnector=connector;
		authorMappings = new HashMap<String, Long>();
		authorsOnPublication = new HashSet<Long>();
		relations = new HashMap<Integer, HashMap<Long, HashMap<Long, Integer>>>();
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

	public HashMap<Integer, HashMap<Long, HashMap<Long, Integer>>> getRelations() {
		return relations;
	}

	public HashMap<String, Long> getAuthorMappings() {
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

				for (Long author_from : relations.get(year).keySet()) {
					for (Long author_to : relations.get(year).get(
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
		String dbPath = null;
		DBConnector connector=null;
		if(args.length==4) {
			dbPath=args[3];
			File f=new File(dbPath);
			System.out.println(f.getAbsolutePath());
			connector=DBConnector.getInstance("/home/andi/dblp_communities/dblp/neo4j");
		}
		
		Parser p = null;
		if (args.length < 3) {
			System.err
					.println("Usage: java -jar dblp_communities.jar [input] [output-folder] [splitter] [[db-path]]");
			System.exit(0);
		
		} else  {
			int splitter = 0;
			try {
				splitter = Integer.valueOf(args[2]);
			} catch (NumberFormatException e) {
				System.err.println("Use an Integer splitter!");
				System.exit(0);
			}
			p = new Parser(args[0], splitter,connector);
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
