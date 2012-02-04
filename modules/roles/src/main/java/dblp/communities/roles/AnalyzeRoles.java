package dblp.communities.roles;

import java.util.HashMap;
import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import dblp.communities.db_interface.AuthorGraphRelationshipType;
import dblp.communities.db_interface.DBConnector;

public class AnalyzeRoles {



	private HashMap<Role, Long> analyze;
	private HashMap<Long,Object> roles;

	public void rolesDistribution(Node node,DBConnector dbConnector) {
		analyze = new HashMap<Role, Long>();
		
		roles= new HashMap<Long, Object>();
		rolesDistributionRecursive(node);
		
		for (Role role : analyze.keySet()) {
			
			dbConnector.setAuthorProperty(node.getId(), role.name()+"_count", (Object)analyze.get(role));
		}
		dbConnector.propertyImport(roles, "role");
		
	}

	private void rolesDistributionRecursive(Node node) {

		Iterable<Relationship> rel = node.getRelationships(
				AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
		Iterator<Relationship> iterator = rel.iterator();

		if (!iterator.hasNext()) {
			// Author Node
			if (node.hasProperty("z")) {
				Double z = (Double) node.getProperty("z");
				if (z >= 2.5d) {
					increaseValue(Role.HUBS);
					if (node.hasProperty("p")) {
						Double p = (Double) node.getProperty("p");
						if (p <= 0.30d) {
							increaseValue(Role.R5);
							roles.put(node.getId(),Role.R5.name());
						} else if (0.30 < p && p <= 0.75) {
							increaseValue(Role.R6);
							roles.put(node.getId(),Role.R6.name());
						} else if (p > 0.75) {
							increaseValue(Role.R7);
							roles.put(node.getId(),Role.R7.name());
						}
					}
				} else {
					increaseValue(Role.NON_HUBS);
					if (node.hasProperty("p")) {
						Double p = (Double) node.getProperty("p");
						if (p <= 0.05d) {
							increaseValue(Role.R1);
							roles.put(node.getId(),Role.R1.name());
						} else if (0.05 < p && p <= 0.62) {
							increaseValue(Role.R2);
							roles.put(node.getId(),Role.R2.name());
						} else if (0.62 < p && p <= 0.80) {
							increaseValue(Role.R3);
							roles.put(node.getId(),Role.R3.name());
						} else if (p > 0.80) {
							increaseValue(Role.R4);
							roles.put(node.getId(),Role.R4.name());
						}
					}
				}
			}
		}
		while (iterator.hasNext()) {
			Relationship relation = iterator.next();
			rolesDistributionRecursive(relation.getStartNode());
		}

	}

	private void increaseValue(Role role) {
		Long count;
		if ((count = (Long) analyze.get(role)) == null) {
			analyze.put(role, new Long(1L));
		} else {
			analyze.put(role, count + 1);
		}
	}
}
