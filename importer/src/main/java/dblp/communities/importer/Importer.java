package dblp.communities.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import dblp.communities.db_interface.AuthorGraphRelationshipType;
import dblp.communities.db_interface.DBBatchInserter;
import dblp.communities.db_interface.DBConnector;
import dblp.communities.db_interface.NodePair;
import dblp.communities.db_interface.Pair;

public class Importer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: Inputdir Uniquedir Neo4jPath ");
			return;

		}

		File dir = new File(args[0]);
		File unique = new File(args[1]);

		HashSet<Pair> delayedInsertions = new HashSet<Pair>();
		String dbPath = args[2];
		DBBatchInserter dbbatch = new DBBatchInserter(dbPath);
		// DBConnector olddb=DBConnector.getInstance(args[3]);
		/*
		 * for(Node node:olddb.getGraphDb().getAllNodes()) {
		 * dbbatch.createNode(node);
		 * 
		 * } for(Node node:olddb.getGraphDb().getAllNodes()) {
		 * Iterable<Relationship> iterable
		 * =node.getRelationships(Direction.OUTGOING); for (Relationship
		 * relationship : iterable) { dbbatch.createRelationship(relationship);
		 * } } System.out.println("Old DB Imported!");
		 */
		HashMap<Long, Long> realnodes = new HashMap<Long, Long>();
		for (String file : unique.list()) {
			System.out.println("Scanning file: " + file);
			try {
				HashMap<Long, Long> communityLevel = new HashMap<Long, Long>();
				BufferedReader buf = new BufferedReader(new FileReader(
						new File(unique, file)));

				String line;
				long num = 0l;
				while ((line = buf.readLine()) != null) {
					realnodes.put(num, Long.valueOf(line));
					num++;
				}
				File name = new File(file);
				buf = new BufferedReader(new FileReader(new File(dir, name
						.getName().substring(0,
								name.getName().length() - ".unique".length())
						+ ".tree")));
				HashSet<Pair> insertions = new HashSet<Pair>();

				boolean authors = true;
				num = 0l;
				while ((line = buf.readLine()) != null) {
					String[] columns = line.split(" ");
					long from = Long.valueOf(columns[0]);
					long to = Long.valueOf(columns[1]);
					if (num != from) {
						// Write everything on level so last level wont be
						// written!
						num = 0l;

						Long real_to;
						for (Pair pair : insertions) {
							
							if ((real_to = communityLevel.get(pair.getTo())) == null) {
								real_to = dbbatch.createCommunity();
								communityLevel.put(pair.getTo(), real_to);
							}
							if (authors) {

								delayedInsertions.add(new Pair(realnodes
										.get(pair.getFrom()), real_to));
							} else {
								// communities can be inserted now

								dbbatch.belongsTo(realnodes.get(pair.getFrom()),
										real_to);
							}

						}
						authors = false;
						insertions.clear();
						realnodes = communityLevel;
						communityLevel = new HashMap<Long, Long>();
					}

					insertions.add(new Pair(from, to));
					num++;
				}
				// Hang in top level communities
				String[] year_label = name.getName().substring(0,
						name.getName().length() - ".unique".length())
						.split("-");
				Long year = dbbatch.createYear(Integer.valueOf(year_label[0]),
						Integer.valueOf(year_label[1]));
				delayedInsertions.add(new Pair(year, 0l));
				for (Long realnode : realnodes.values()) {
					dbbatch.belongsTo(realnode, year);
				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}

		}
		dbbatch.shutdown();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DBConnector connector = DBConnector.getInstance(dbPath);
		GraphDatabaseService graphdb = connector.getGraphDb();

		System.out.println("Batch Import finished.");
		System.out.println("Cleaning up and Linking Database.");
		int num = 0;
		int totalnum = delayedInsertions.size();
		HashSet<NodePair> froms = new HashSet<NodePair>();
		for (Pair pair : delayedInsertions) {
			if (num % 1000 == 0 && num != 0) {
				// Input 1000 in one Transaction
				if (num % 10000 == 0)
					System.out.println(num + "/" + totalnum);
				inputFroms(graphdb, froms);
			}
			num++;
			NodePair nodepair = new NodePair(graphdb
					.getNodeById(pair.getFrom()), graphdb.getNodeById(pair
					.getTo()));

			// Adding to List
			froms.add(nodepair);
		}
		inputFroms(graphdb, froms);
		connector.getGraphDb().shutdown();
	}

	private static void inputFroms(GraphDatabaseService graphdb,
			HashSet<NodePair> froms) {
		Transaction tx = graphdb.beginTx();
		try {

			// MultiInsert
			for (NodePair pair : froms) {
				pair.getN_from().createRelationshipTo(pair.getN_to(),
						AuthorGraphRelationshipType.BELONGS_TO);
			}
			tx.success();
		} finally {
			tx.finish();
		}
		froms.clear();
	}

}
