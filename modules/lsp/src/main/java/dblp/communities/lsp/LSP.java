package dblp.communities.lsp;

import java.util.HashMap;
import java.util.HashSet;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.RelationshipExpander;

import dblp.communities.db_interface.InCommunityExpander;
import dblp.communities.db_interface.LinkedNodeList;
import dblp.communities.db_interface.LinkedNodeListContainer;

public class LSP {
	
	public LSP() {
		resultcache=new HashMap<Long, HashMap<Long,Long>>();
	}
	
	public HashMap<Long,HashMap<Long,Long>> resultcache;
	

	

	public long getLSP(LinkedNodeList community) {

		LinkedNodeListContainer iterator;
		RelationshipExpander expander=new InCommunityExpander(getNodeIds(community));
		iterator=community.getStartnode();
		long tmp_longestShortestPath = 0;
		int num=1;
		while(iterator!=null) {
			if(num%10==0) {
				System.out.println(num);
			}
			num++;
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
