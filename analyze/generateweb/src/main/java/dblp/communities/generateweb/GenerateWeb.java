package dblp.communities.generateweb;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.Traverser.Order;

import dblp.communities.db_interface.AuthorGraphRelationshipType;
import dblp.communities.db_interface.DBConnector;
import dblp.communities.db_interface.DoubleValueWrapper;
import dblp.communities.db_interface.NamePair;
import dblp.communities.generateweb.template.AuthorController;
import dblp.communities.generateweb.template.CommunityNodeController;
import dblp.communities.generateweb.template.NodeController;
import dblp.communities.generateweb.template.TemplateEngine;

public class GenerateWeb {

	
	
	
	
	
	
	
	private static final String NODE_FIELD = "node";
	private static final String AUTHOR_FIELD = "author";
	private static final String YEAR_FIELD = "year";
	private static final String COMMUNITY_FIELD = "community";
	private static final String ROOTNODE_FIELD = "root";
	
	//Index & Links
	
	private static final String INDEX_FIELD = "index";
	private static final String SINGLE_CHARACTER_FIELD = "char";
	private static final String CHARS_FIELD = "chars";
	
	private static final String TITLE_FIELD = "title";
	private static final String LINKS_FIELD = "links";
	
	private static final String INDEX_HTML = "index";
	
	// Toplists
	private static final String TOP_FIELD = "top";
	
	private static final String PARTICIPATIONCOEFFICIENT = "p";
	private static final String TOP_P_HTML = "top_p";
	private static final String TOP_P_TITLE = "Participation coefficient";
	
	private static final String WITHINMODULEDEGREE = "z";
	private static final String TOP_Z_TITLE = "Within-module degree";
	private static final String TOP_Z_HTML = "top_z";
	
	private static final String CONDUCTANCE = "conductance";
	private static final String TOP_C_TITLE = "Conductance";
	private static final String TOP_C_HTML = "top_c";
	private static final String CONDUCTANCE_DISTRIBUTION_FIELD = "conductance";

	private static DBConnector dbconnector;

	public static void main(String[] args) throws IOException {

		Map<Character, SortedSet<NamePair>> index;
		if (args.length == 4) {
			String outfile = args[2];
			String templatedir = args[1];
			boolean yearOnly=Boolean.valueOf(args[3]);
			File template = new File(templatedir);
			File out = new File(outfile);
			File nodeout = new File(out, "nodes");
			File indexout = new File(out, "index");
			if(!nodeout.exists()) {
				nodeout.mkdir();
			}
			if(!indexout.exists()) {
				indexout.mkdir();
			}
			
			dbconnector = DBConnector.getInstance(args[0]);
			index = new TreeMap<Character, SortedSet<NamePair>>();
			TemplateEngine engine = new TemplateEngine(nodeout, template,
					TemplateEngine.NODETEMPLATE);
			TemplateEngine yearengine = new TemplateEngine(nodeout, template,
					TemplateEngine.YEARTEMPLATE);
			Iterable<Node> nodes = dbconnector.getGraphDb().getAllNodes();
			Iterator<Node> nodeiterator = nodes.iterator();
			int num = 0;
			Map<String, Object> map = new HashMap<String, Object>();
			
			
			Map<String,String> links=new HashMap<String,String>();
			if(!yearOnly) {
			//Toplists
			//p
			TemplateEngine toplistEngine = new TemplateEngine(out, template, TemplateEngine.DOUBLETOPLIST);
			List<DoubleValueWrapper> max=Toplists.maxAuthorValue(dbconnector.getGraphDb().getReferenceNode(), PARTICIPATIONCOEFFICIENT, 10,dbconnector);
			map.put(TOP_FIELD, max);
			map.put(TITLE_FIELD, TOP_P_TITLE);
			toplistEngine.generateTemplate(map, TOP_P_HTML);
			links.put(TOP_P_HTML, TOP_P_TITLE);
			//z
			max=Toplists.maxAuthorValue(dbconnector.getGraphDb().getReferenceNode(), WITHINMODULEDEGREE, 10,dbconnector);
			map.put(TOP_FIELD, max);
			map.put(TITLE_FIELD, TOP_Z_TITLE);
			toplistEngine.generateTemplate(map, TOP_Z_HTML);
			links.put(TOP_Z_HTML, TOP_Z_TITLE);
			//conductance
			max=Toplists.maxTopLevelCommunityValue(dbconnector.getGraphDb().getReferenceNode(), CONDUCTANCE, 10);
			map.put(TOP_FIELD, max);
			map.put(TITLE_FIELD, TOP_C_TITLE);
			toplistEngine.generateTemplate(map, TOP_C_HTML);
			links.put(TOP_C_HTML, TOP_C_TITLE);
			} else {
				Traverser traverse_years=dbconnector.getGraphDb().getReferenceNode().traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
				nodeiterator=traverse_years.iterator();
			}
			// Generate Nodes.html && Index
			
			while (nodeiterator.hasNext()) {
				if (num % 1000 == 0) {
					System.out.println(num);
				}

				Node n = nodeiterator.next();
				if(yearOnly) {
					if(!n.hasProperty("fromYear")) {
						continue;
					}
				}
				// Node Information
				
				if (n.hasProperty(NodeController.NAME)) {
					String name = (String) n.getProperty(NodeController.NAME);

					Character c = Character.toUpperCase(name.charAt(0));
					SortedSet<NamePair> list = index.get(c);
					if (list == null) {
						list = new TreeSet<NamePair>();
						index.put(c, list);
					}
					list.add(new NamePair(name, n.getId()));
					map.put(NODE_FIELD, new NodeController(n));
					map.put(AUTHOR_FIELD, new AuthorController(n));
					engine.generateTemplate(map, String.valueOf(n.getId()));
				} else {
					// We have a special node
					if(n.hasProperty("fromYear")) {
						//Year node
						map.put(YEAR_FIELD,new NodeController(n));
						map.put(COMMUNITY_FIELD,new CommunityNodeController(n,true));
						map.put(CONDUCTANCE_DISTRIBUTION_FIELD, getConductanceDistribution(n));
						yearengine.generateTemplate(map, String.valueOf(n.getId()));
					} else if(n.getId()==0) {
						//Root node
						map.put(ROOTNODE_FIELD, new NodeController(n));
						map.put(COMMUNITY_FIELD,new CommunityNodeController(n,false));
						yearengine.generateTemplate(map, String.valueOf(n.getId()));
					} else {
						map.put(NODE_FIELD, new NodeController(n));
						map.put(COMMUNITY_FIELD,new CommunityNodeController(n,false));
						engine.generateTemplate(map, String.valueOf(n.getId()));
					}
					
				}
				
				
				map.clear();

				num++;
			}

			TemplateEngine indexengine = new TemplateEngine(indexout, template,
					TemplateEngine.INDEXTEMPLATE);
			if(!yearOnly) {
			// Index out
			
			for (Character k : index.keySet()) {
				SortedSet<NamePair> list = index.get(k);
				map.put(INDEX_FIELD, list);
				map.put(SINGLE_CHARACTER_FIELD, k);
				indexengine.generateTemplate(map, String.valueOf(k));
				map.clear();
			}
			
			
			
			// Frameset
			
			TemplateEngine charsEngine = new TemplateEngine(out, template, TemplateEngine.FRAMESETTEMPLATE);
			
			Traverser traverser=dbconnector.getGraphDb().getReferenceNode().traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
			Collection<Node> years=traverser.getAllNodes();
			// For Frontpage Linking
			if(years.size()==1) {
				map.put(NODE_FIELD,new NodeController(years.iterator().next()));
			} else {
				map.put(NODE_FIELD,new NodeController(dbconnector.getGraphDb().getReferenceNode()));
			}
			
			map.put(CHARS_FIELD, index.keySet());
			map.put(LINKS_FIELD, links);
			charsEngine.generateTemplate(map, INDEX_HTML);
			map.clear();
			}
		} else {

			System.out
					.println("Usage: Author <db_path> <templatedir> <htmldir>");
		}

	}

	
	
	private static Map<String,Integer> getConductanceDistribution(Node n) {
		
		Traverser communities=n.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
		Map<String,Integer> map=new TreeMap<String, Integer>();
		map.put("0.00", new Integer(0));
		map.put("0.1 >", new Integer(0));
		map.put("0.1-0.2", new Integer(0));
		map.put("0.2-0.3", new Integer(0));
		map.put("0.3-0.4", new Integer(0));
		map.put("0.4-0.5", new Integer(0));
		map.put(">= 0.5", new Integer(0));
		
		for (Node node : communities) {
			if(node.hasProperty("conductance")) { 
				double conductance=(Double) node.getProperty("conductance");
				if(conductance == 0.00d ) {
					increment("0.00",map);
				}
				if(conductance > 0.00d && conductance <0.1d) {
					increment("0.1 >",map);
				}
				if(conductance >= 0.1d && conductance <0.2d) {
					increment("0.1-0.2",map);
				}
				if(conductance >= 0.2d && conductance <0.3d) {
					increment("0.2-0.3",map);
				}
				if(conductance >= 0.3d && conductance <0.4d) {
					increment("0.3-0.4",map);
				}
				if(conductance >= 0.4d && conductance <0.5d) {
					increment("0.4-0.5",map);
				}
				if(conductance >= 0.5d ) {
					increment(">= 0.5",map);
				}
			}
		}
		
		return map;
	}



	private static void increment(String string, Map<String, Integer> map) {
		map.put(string, map.get(string)+1);		
	}

}
