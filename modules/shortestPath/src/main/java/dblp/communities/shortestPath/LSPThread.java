package dblp.communities.shortestPath;

import java.util.Map;
import java.util.concurrent.Callable;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.kernel.Traversal;

import dblp.communities.db_interface.AuthorGraphRelationshipType;

public class LSPThread implements Callable<Long> {

	private Node startnode;

	Map<Long, Object> lsps;
	private long longestShortestPath = 0;
	
	public long getResult() {
		return longestShortestPath;
	}
	
	public LSPThread(Node startnode, Map<Long, Object> lsps) {
		
		this.lsps = lsps;
		this.startnode=startnode;
	}

	@Override
	public Long call() {
		System.out.println(Thread.currentThread().getId() + " started");
		long lsp=getLongestShortestPath(startnode);
		lsps.put(startnode.getId(), lsp ) ;
		System.out.println(Thread.currentThread().getId() + " finished");
		return lsp;
	}

	public long getLongestShortestPath(Node node) {
		
		Iterable<Relationship> rel = node.getRelationships(
				AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
		
		while (rel.iterator().hasNext()) {
			long lsp = 0;
			// prove if we got the last community level
			Relationship element=rel.iterator().next();
			Iterable<Relationship> t = element.getStartNode()
					.getRelationships(AuthorGraphRelationshipType.BELONGS_TO,
							Direction.INCOMING);
			if (!t.iterator().hasNext()) {

				Iterable<Relationship> rel_authors = node
						.getRelationships(Direction.INCOMING,
								AuthorGraphRelationshipType.BELONGS_TO);
				Iterable<Relationship> rel_authors2 = node
						.getRelationships(Direction.INCOMING,
								AuthorGraphRelationshipType.BELONGS_TO);

				while (rel_authors.iterator().hasNext()) {

					long tmp_longestShortestPath = 0;
					Relationship r = rel_authors.iterator().next();
					// lsps.put(r.getStartNode().getId(),"0");

					PathFinder<Path> finder = GraphAlgoFactory
							.shortestPath(
									Traversal
											.expanderForTypes(
													AuthorGraphRelationshipType.PUBLICATED_TOGETHER,
													Direction.BOTH),
									Integer.MAX_VALUE);

					while (rel_authors2.iterator().hasNext()) {

						Relationship r2 = rel_authors2.iterator().next();
						Path shortestPath = finder.findSinglePath(r
								.getStartNode(), r2.getStartNode());

						if (shortestPath != null
								&& shortestPath.length() > tmp_longestShortestPath) {
							tmp_longestShortestPath = shortestPath.length();
						}

					}
					if (tmp_longestShortestPath > longestShortestPath) {
						longestShortestPath = tmp_longestShortestPath;
					}

				}

				lsps.put(element.getEndNode().getId(), longestShortestPath);

				return longestShortestPath;

			} else {
				lsp = getLongestShortestPath(element.getStartNode());

				if (lsps.containsKey(element.getEndNode().getId())) {
					if ((Long) lsps.get(element.getEndNode().getId()) < lsp) {
						lsps.put(element.getEndNode().getId(), lsp);
					} else {
						lsp = (Long) lsps.get(element.getEndNode().getId());
					}

				} else {
					lsps.put(element.getEndNode().getId(), lsp);
				}

			}
			
		}
		
		return (Long) lsps.get(node.getId());
	}

}
