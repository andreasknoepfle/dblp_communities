package dblp.communities.parser;
import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class Parser {

	private HashMap<String, Integer> authorMappings;
	private HashSet<Integer> authorsOnPublication;
	private HashMap<Integer, HashMap<Integer, Integer>> relations;
	private int authorNumber = 0;

	private class ConfigHandler extends DefaultHandler {

		private String Value;

		private String recordTag;

		
		private boolean insidePerson;

		public void startElement(String namespaceURI, String localName,
				String rawName, Attributes atts) throws SAXException {
			
			if (insidePerson = (rawName.equals("author") || rawName
					.equals("editor"))) {
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
				if(Value.startsWith("Joachim")) {
					System.out.print("");
				}
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
			if (rawName.equals(recordTag)) {
				// Record Tag gefunden fuege tmp personen zusammen
				for (Integer author_from : authorsOnPublication) {
					for (Integer author_to : authorsOnPublication) {
						// Verbindung nur in eine Richtung werten und nicht mit sich selber verbinden
						if (author_from < author_to) {
							if (relations.containsKey(author_from)) {
								HashMap<Integer, Integer> author_from_map = relations
										.get(author_from);
								if (author_from_map.containsKey(author_to)) {
									author_from_map.put(author_to, author_from_map
											.get(author_to) + 1);
								} else {
									author_from_map.put(author_to, new Integer(1));
								}
							} else {
								HashMap<Integer,Integer> relation=new HashMap<Integer,Integer>();
								relation.put(author_to, new Integer(1));
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
			if (insidePerson)
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

	Parser(String uri) {
		authorMappings=new  HashMap<String, Integer>();
		authorsOnPublication=new HashSet<Integer>();
		relations=new HashMap<Integer, HashMap<Integer,Integer>>();
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

	public HashMap<Integer, HashMap<Integer, Integer>> getRelations() {
		return relations;
	}


	public HashMap<String, Integer> getAuthorMappings() {
		return authorMappings;
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: java -jar dblp_communities.jar [input]");
			System.exit(0);
		}
		Parser p = new Parser(args[0]);
		HashMap<Integer,HashMap<Integer,Integer>> relations=p.getRelations();
		for (Integer author_from : relations.keySet()) {
			for(Integer author_to :relations.get(author_from).keySet()) {
				System.out.print(author_from);
				System.out.print(" ");
				System.out.print(author_to);
				System.out.print(" ");
				System.out.print(relations.get(author_from).get(author_to));
				System.out.println();
			}
		}
		HashMap<String, Integer> authors=p.getAuthorMappings();
		for (String key : authors.keySet()) {
			System.err.println(authors.get(key)+ " --> "+ key);
		}
	}
}
