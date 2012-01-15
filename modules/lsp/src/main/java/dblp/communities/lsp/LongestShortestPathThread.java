package dblp.communities.lsp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipExpander;

import dblp.communities.db_interface.AuthorGraphRelationshipType;
import dblp.communities.db_interface.DBConnector;
import dblp.communities.db_interface.InCommunityExpander;

public class LongestShortestPathThread extends Thread {

	private Node startnode;
	final static String longestshortestpath = "lsp";
	Map<Long, Object> lsps;
	DBConnector dbConnector;
	public HashMap<Long,HashMap<Long,Long>> resultcache;
	
	public LongestShortestPathThread(Node startnode, DBConnector dbConnector) {
		
		this.dbConnector=dbConnector;
		this.startnode=startnode;
		resultcache=new HashMap<Long, HashMap<Long,Long>>();
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
			
			collect(relation.getStartNode());
			
			
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
			
		long lsp = getLSP(nodelist);
		insert(relation.getStartNode(), lsp);
		
		return nodelist;
	}

	private void insert(Node node, long tmp_longestShortestPath) {
		lsps.put(node.getId(), tmp_longestShortestPath);
	}

	public long getLSP(LinkedNodeList community) {
		
		LinkedNodeListContainer iterator;
		RelationshipExpander expander=new InCommunityExpander(getNodeIds(community));
		iterator=community.getStartnode();
		long tmp_longestShortestPath = 0;
		
		while(iterator!=null) {
			LinkedNodeListContainer iterator2=community.getStartnode();
			Node from=iterator.getNode();
			while(iterator2!=null) {
				Node to=iterator2.getNode();
				if(!from.equals(to)) {
					long from_id=from.getId();
					long to_id=to.getId();
					long result=getCachedResult(from_id<to_id?from_id:to_id,from_id<to_id?to_id:from_id); // only once for every nodepair
					if(result==-1) { // Cache Miss
						PathFinder<Path> finder = GraphAlgoFactory
						.shortestPath(expander,
								Integer.MAX_VALUE);
						Path shortestPath=finder.findSinglePath(from, to);
						if (shortestPath != null) {
							if(shortestPath.length() > tmp_longestShortestPath) {
								tmp_longestShortestPath = shortestPath.length();
							}
							addToCache(from_id<to_id?from_id:to_id,from_id<to_id?to_id:from_id,shortestPath.length());
						}
					} else { // Cache Hit
						if(result>tmp_longestShortestPath) {
							tmp_longestShortestPath = result;
						}
					}
					
					
				}
				iterator2=iterator2.getNextNode();
			}
			iterator=iterator.getNextNode();
				
		}
		return tmp_longestShortestPath;
	}

	private void addToCache(long node, long node2, long length) {
		if(resultcache.containsKey(node)) {
			HashMap<Long, Long> innermap=resultcache.get(node);
			innermap.put(node2, length);
		} else {
			HashMap<Long,Long> newinnermap=new HashMap<Long, Long>();
			newinnermap.put(node2, length);
			resultcache.put(node, newinnermap);
		}
		
	}

	private long getCachedResult(long node, long node2) {
		if(resultcache.containsKey(node)) {
			HashMap<Long, Long> innermap=resultcache.get(node);
			if(innermap.containsKey(node2)) {
				return innermap.get(node2);
			}
		}
		return -1;
	}

	private HashSet<Long> getNodeIds(LinkedNodeList community) {
		HashSet<Long> node_ids = new HashSet<Long>();
		LinkedNodeListContainer iterator=community.getStartnode();
		while(iterator!=null) {
			node_ids.add(iterator.getNode().getId());
			iterator=iterator.getNextNode();
		}
		return node_ids;
	}

	

}
