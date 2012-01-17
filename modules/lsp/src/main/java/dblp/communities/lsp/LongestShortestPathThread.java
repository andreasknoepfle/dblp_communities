package dblp.communities.lsp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import dblp.communities.db_interface.AuthorGraphRelationshipType;
import dblp.communities.db_interface.DBConnector;
import dblp.communities.db_interface.LinkedNodeList;


public class LongestShortestPathThread extends Thread {
	private Node startnode;
	final static String longestshortestpath = "lsp";
	Map<Long, Object> lsps;
	DBConnector dbConnector;
	

	public LongestShortestPathThread(Node startnode, DBConnector dbConnector) {

		this.dbConnector=dbConnector;
		this.startnode=startnode;
		
	}

	private void insert(Node node, long tmp_longestShortestPath) {
		lsps.put(node.getId(), tmp_longestShortestPath);
	}
	@Override
	public void run() {
		lsps = new HashMap<Long, Object>();
		System.out.println("Thread: "+Thread.currentThread().getId() + " started");
		Iterable<Relationship> relations = startnode.getRelationships(
				AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
		Iterator<Relationship> iterator=relations.iterator();
		int num=1;
		while(relations.iterator().hasNext()) {
			Relationship relation=iterator.next();
			// Call on all communities
			Long count=(Long) relation.getStartNode().getProperty("count");
			System.out.println("Thread: "+Thread.currentThread().getId() + " Num:"+num+ " Count:"+count);
			LSP lspmodule=new LSP();
			LinkedNodeList nodelist=DBConnector.collect(relation.getStartNode());
			long lsp = lspmodule.getLSP(nodelist);
			insert(relation.getStartNode(), lsp);

			num++;
		}
		dbConnector.propertyImport(lsps, longestshortestpath);
		System.out.println(Thread.currentThread().getId() + " finished");
	}

	
	
	/*
	private Node startnode;
	final static String longestshortestpath = "lsp";
	Map<Long, Object> lsps;
	DBConnector dbConnector;
	public HashMap<Long, HashSet<Long>> resultcache;
	
	public LongestShortestPathThread(Node startnode, DBConnector dbConnector) {
		
		this.dbConnector=dbConnector;
		this.startnode=startnode;
		resultcache=new HashMap<Long, HashSet<Long>>();
	}

	@Override
	public void run() {
		lsps = new HashMap<Long, Object>();
		System.out.println("Thread: "+Thread.currentThread().getId() + " started");
		Iterable<Relationship> relations = startnode.getRelationships(
				AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
		Iterator<Relationship> iterator=relations.iterator();
		int num=1;
		while(relations.iterator().hasNext()) {
			Relationship relation=iterator.next();
			// Call on all communities
			Long count=(Long) relation.getStartNode().getProperty("count");
			System.out.println("Thread: "+Thread.currentThread().getId() + " Num:"+num+ " Count:"+count);
			
			LinkedNodeList nodelist=collect(relation.getStartNode());
			System.out.println("Collected");
			long lsp;
			try {
				lsp = getLSP(nodelist);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			System.out.println("Longest Shortest Path:"+lsp);
			insert(relation.getStartNode(), lsp);
			
			num++;
		}
		dbConnector.propertyImport(lsps, longestshortestpath);
		System.out.println(Thread.currentThread().getId() + " finished");
	}

	public LinkedNodeList collect(Node node) {
		
		Iterable<Relationship> relations = node.getRelationships(
				AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
		if(!relations.iterator().hasNext()) {
			//author node
			return new LinkedNodeList(node);
		} 
		//community node
		
		Iterator<Relationship> iterator=relations.iterator();
		Relationship relation=iterator.next();
		LinkedNodeList nodelist=collect(relation.getStartNode());
		
		
		//call on all children and get their communities
		while(iterator.hasNext()) {
			relation=iterator.next();
			LinkedNodeList list=collect(relation.getStartNode());
			
			nodelist.add(list); //add to our community
			
		}
			
		
		
		return nodelist;
	}

	private void insert(Node node, long tmp_longestShortestPath) {
		lsps.put(node.getId(), tmp_longestShortestPath);
	}

	public long getLSP(LinkedNodeList community) throws Exception {
		
		Set<Long> nodes=new HashSet<Long>();
		Set<Pair> relationships=new HashSet<Pair>();
		LinkedNodeListContainer iterator;
		iterator=community.getStartnode();
		while(iterator!=null) {
			//Collect Nodes
			nodes.add(iterator.getNode().getId());
			iterator=iterator.getNextNode();
		}
		iterator=community.getStartnode();
		while(iterator!=null) {
			//Collect Relations
			Node node=iterator.getNode();
			Iterator<Relationship> reliterator=node.getRelationships().iterator();
			while(reliterator.hasNext()) {
				Relationship rel=reliterator.next();
				Node other=rel.getOtherNode(node);
				long from_id=node.getId()>other.getId()?node.getId():other.getId();
				long to_id=node.getId()>other.getId()?other.getId():node.getId();
				if(!alreadyThere(from_id,to_id)) {
					if(rel.isType(AuthorGraphRelationshipType.PUBLICATED_TOGETHER) && nodes.contains(other.getId())  ) {
						relationships.add(new Pair(from_id, to_id));
						addToCache(from_id,to_id);
					}
				}
				
			}
			iterator=iterator.getNextNode();
		}
		
		System.out.println("Calculating Paths for "+ nodes.size()+ " Nodes with "+relationships.size()+" Relations");
		
		FloydWarshallShort floyd=new FloydWarshallShort(nodes, relationships);
		floyd.run(); 
		
		
		return floyd.maxCost();
	}
	
	private void addToCache(long node, long node2) {
		if(resultcache.containsKey(node)) {
			HashSet<Long> innermap=resultcache.get(node);
			innermap.add(node2);
		} else {
			HashSet<Long> newinnermap=new HashSet<Long>();
			newinnermap.add(node2);
			resultcache.put(node, newinnermap);
		}

	}

	private boolean alreadyThere(long node, long node2) {
		if(resultcache.containsKey(node)) {
			HashSet<Long> innermap=resultcache.get(node);
			if(innermap.contains(node2)) {
				return true;
			}
		}
		return false;
	}
	*/

}
