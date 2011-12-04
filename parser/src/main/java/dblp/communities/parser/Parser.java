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
	 */
	private HashMap<Integer, HashMap<Integer, HashSet<Integer>>> relations;
	/**
	 * author id counter
	 */
	private int authorNumber = 0;
	private int minYear=Integer.MAX_VALUE;
	private int maxYear=Integer.MIN_VALUE;
	private int pubYear;

	private class ConfigHandler extends DefaultHandler {

		private String Value;

		private String recordTag;

		
		private boolean insideTag;

		public void startElement(String namespaceURI, String localName,
				String rawName, Attributes atts) throws SAXException {
			
			if (insideTag = (rawName.equals("author") || rawName
					.equals("editor")) || rawName.equals("year")) {
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
					Integer author_id=new Integer(authorNumber);
					authorMappings.put(Value, author_id);
					authorsOnPublication.add(author_id);
					authorNumber++;
				} else {
					authorsOnPublication.add(authorMappings.get(Value));
				}

			}
			if(rawName.equals("year")) {
				pubYear=Integer.parseInt(Value);
				if(pubYear>maxYear) {
					maxYear=pubYear;
				}
				if(pubYear<minYear) {
					minYear=pubYear;
				}
			}
			if (rawName.equals(recordTag)) {
				// Record Tag gefunden fuege tmp personen zusammen
				for (Integer author_from : authorsOnPublication) {
					for (Integer author_to : authorsOnPublication) {
						// Verbindung nur in eine Richtung werten und nicht mit sich selber verbinden
						if (author_from < author_to) {
							if (relations.containsKey(author_from)) {
								HashMap<Integer, HashSet<Integer>> author_from_map = relations
										.get(author_from);
								if (author_from_map.containsKey(author_to)) {
									author_from_map.get(author_to).add(pubYear);
								} else {
									HashSet<Integer> pub=new HashSet<Integer>();
									pub.add(pubYear);
									author_from_map.put(author_to, pub);
								}
							} else {
								HashMap<Integer,HashSet<Integer>> relation=new HashMap<Integer,HashSet<Integer>>();
								HashSet<Integer> pub=new HashSet<Integer>();
								pub.add(pubYear);
								relation.put(author_to, pub);
								relations.put(author_from, relation);
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

	public Parser(String uri) {
		authorMappings=new  HashMap<String, Integer>();
		authorsOnPublication=new HashSet<Integer>();
		relations=new HashMap<Integer, HashMap<Integer,HashSet<Integer>>>();
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

	public HashMap<Integer, HashMap<Integer, HashSet<Integer>>> getRelations() {
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

	public void printRelations(PrintStream s) {
		for (Integer author_from : relations.keySet()) {
			for(Integer author_to :relations.get(author_from).keySet()) {
				s.print(author_from);
				s.print(" ");
				s.print(author_to);
				s.print(" ");
				s.print(relations.get(author_from).get(author_to).size());
				s.println();
			}
		}
	}
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: java -jar dblp_communities.jar [input]");
			System.exit(0);
		}
		Parser p = new Parser(args[0]);
		p.printRelations(System.out);
		HashMap<String, Integer> authors=p.getAuthorMappings();
		for (String key : authors.keySet()) {
			System.err.println(authors.get(key)+ " --> "+ key);
		}
		System.out.println("Min Year: " + p.getMinYear());
		System.out.println("Max Year: " + p.getMaxYear());
	
	}
}
