package dblp.communities.generateweb;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.neo4j.graphdb.Node;

import dblp.communities.db_interface.DBConnector;
import dblp.communities.db_interface.DoubleValueWrapper;
import dblp.communities.db_interface.NamePair;

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
	
	

	

	private static DBConnector dbconnector;

	public static void main(String[] args) throws IOException {

		Map<Character, SortedSet<NamePair>> index;
		if (args.length == 3) {
			String outfile = args[2];
			String templatedir = args[1];
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
			
			// Generate Nodes.html && Index
			
			while (nodeiterator.hasNext()) {
				if (num % 1000 == 0) {
					System.out.println(num);
					if(num==1000)
						break;
				}

				Node n = nodeiterator.next();
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
						map.put(COMMUNITY_FIELD,new CommunityNodeController(n));
						yearengine.generateTemplate(map, String.valueOf(n.getId()));
					} else if(n.getId()==0) {
						//Root node
						map.put(ROOTNODE_FIELD, new NodeController(n));
						map.put(COMMUNITY_FIELD,new CommunityNodeController(n));
						yearengine.generateTemplate(map, String.valueOf(n.getId()));
					} else {
						map.put(NODE_FIELD, new NodeController(n));
						map.put(COMMUNITY_FIELD,new CommunityNodeController(n));
						engine.generateTemplate(map, String.valueOf(n.getId()));
					}
					
				}
				
				
				map.clear();

				num++;
			}

			TemplateEngine indexengine = new TemplateEngine(indexout, template,
					TemplateEngine.INDEXTEMPLATE);

			// Index out
			
			for (Character k : index.keySet()) {
				SortedSet<NamePair> list = index.get(k);
				map.put(INDEX_FIELD, list);
				map.put(SINGLE_CHARACTER_FIELD, k);
				indexengine.generateTemplate(map, String.valueOf(k));
				map.clear();
			}
			
			Map<String,String> links=new HashMap<String,String>();
			
			//Toplists
			//p
			TemplateEngine toplistEngine = new TemplateEngine(out, template, TemplateEngine.DOUBLETOPLIST);
			List<DoubleValueWrapper> list=Toplists.maxAuthorValue(dbconnector.getGraphDb().getReferenceNode(), PARTICIPATIONCOEFFICIENT, 10);
			map.put(TOP_FIELD, list);
			map.put(TITLE_FIELD, TOP_P_TITLE);
			toplistEngine.generateTemplate(map, TOP_P_HTML);
			links.put(TOP_P_HTML, TOP_P_TITLE);
			//z
			list=Toplists.maxAuthorValue(dbconnector.getGraphDb().getReferenceNode(), WITHINMODULEDEGREE, 10);
			map.put(TOP_FIELD, list);
			map.put(TITLE_FIELD, TOP_Z_TITLE);
			toplistEngine.generateTemplate(map, TOP_Z_HTML);
			links.put(TOP_Z_HTML, TOP_Z_TITLE);
			
			// Frameset
			
			TemplateEngine charsEngine = new TemplateEngine(out, template, TemplateEngine.FRAMESETTEMPLATE);
			map.put(CHARS_FIELD, index.keySet());
			map.put(LINKS_FIELD, links);
			charsEngine.generateTemplate(map, INDEX_HTML);
			map.clear();

		} else {

			System.out
					.println("Usage: Author <db_path> <templatedir> <htmldir>");
		}

	}

}
