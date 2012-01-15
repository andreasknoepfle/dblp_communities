package dblp.communities.db_interface;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipExpander;

public class InCommunityExpander implements RelationshipExpander {
	

	private Collection<Long> communityNodes;
	
	public InCommunityExpander(Collection<Long> community) {
		this.communityNodes=community;
		
	}
	
	
	@Override
	public Iterable<Relationship> expand(Node node) {
		Iterable<Relationship> iterable=node.getRelationships();
		Iterator<Relationship> iterator=iterable.iterator();
		LinkedList<Relationship> retVal=new LinkedList<Relationship>();
		while(iterator.hasNext()) {
			Relationship rel=iterator.next();
			Node other=rel.getOtherNode(node);
			if(rel.isType(AuthorGraphRelationshipType.PUBLICATED_TOGETHER) && communityNodes.contains(other.getId())  ) {
				retVal.add(rel);
			}
			
		}
		
		return retVal;
		
	}

	@Override
	public RelationshipExpander reversed() {
		
		return new InCommunityExpander(this.communityNodes);
	}

}
