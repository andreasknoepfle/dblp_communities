package dblp.communities.db_interface;

import org.neo4j.graphdb.RelationshipType;

public enum AuthorGraphRelationshipType  implements RelationshipType {
	PUBLICATED_TOGETHER,
	BELONGS_TO
}
