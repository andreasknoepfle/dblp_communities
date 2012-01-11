package dblp.communities.shortestPath;


import java.util.ArrayList;
import java.util.Random;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.kernel.Traversal;


import dblp.communities.db_interface.AuthorGraphRelationshipType;
import dblp.communities.db_interface.DBConnector;


public class LSP_Generator 
{
	
	final static String longestshortestpath = "lsp";
	private static DBConnector dbconnector;
	
    public static void main( String[] args )
    {
    	try{
    		if(args.length == 2){

    	        dbconnector = DBConnector.getInstance(args[0]);       

    	        //long id = createCommunityExample(dbconnector, 3, 20);
    	
    	        getLongestShortestPath(dbconnector, Long.valueOf(args[1]));
    	          
    	        System.out.println("done");
        	
        	}
        	else{
        		System.out.println("Usage: LSP_Generator <db_path> <root_id>");
        	}

    	}catch (Exception e) {
    		System.out.println("Usage: LSP_Generator <db_path> <root_id>");
		}
    }
    
    
    private static int getLongestShortestPath(DBConnector connector, long community){
    	
    	Iterable<Relationship> rel = connector.getGraphDb().getNodeById(community).getRelationships(AuthorGraphRelationshipType.BELONGS_TO,Direction.INCOMING);
    	
    	int lsp = 0; 
    	
    	while (rel.iterator().hasNext() ) {
    		
    		int longestShortestPath=0;
    		
    		Relationship element = rel.iterator().next ();
    		
    		// prove if we got the last community level	
    		Iterable<Relationship> t = element.getStartNode().getRelationships(AuthorGraphRelationshipType.BELONGS_TO,Direction.INCOMING);
    		if(!t.iterator().hasNext()){
    			
    			Iterable<Relationship> rel_authors = connector.getGraphDb().getNodeById(community).getRelationships(Direction.INCOMING,AuthorGraphRelationshipType.BELONGS_TO);
    			Iterable<Relationship> rel_authors2 = connector.getGraphDb().getNodeById(community).getRelationships(Direction.INCOMING,AuthorGraphRelationshipType.BELONGS_TO);
    			
    			
    			
    	    	while (rel_authors.iterator().hasNext() ) {
    	    		
    	    		int tmp_longestShortestPath=0;
    	    		Relationship r = rel_authors.iterator().next ();
    	    		connector.setAuthorProperty(r.getStartNode().getId(),longestshortestpath,"0");		

    	    		PathFinder<Path> finder = GraphAlgoFactory.shortestPath(
    	    			        Traversal.expanderForTypes( AuthorGraphRelationshipType.PUBLICATED_TOGETHER, Direction.BOTH ),Integer.MAX_VALUE );
    	    			 
    	    		while (rel_authors2.iterator().hasNext() ) {
        	    		
    	    			Relationship r2 = rel_authors2.iterator().next ();
        	    		Path shortestPath = finder.findSinglePath(r.getStartNode(), r2.getStartNode() );

        	    		if(shortestPath != null && shortestPath.length() > tmp_longestShortestPath){
        	    			tmp_longestShortestPath = shortestPath.length();
    	    			}
        	    		
    	    		}
    	    		if(tmp_longestShortestPath > longestShortestPath){
    	    			longestShortestPath = tmp_longestShortestPath;
	    			}
    	    			
    	    	}
    	    	
    	    	connector.setAuthorProperty(element.getEndNode().getId(),longestshortestpath,Integer.toString(longestShortestPath));		
    	    	
    	    	return longestShortestPath;
    	    	
    		}
    		else{
    			
    			lsp = getLongestShortestPath(connector,element.getStartNode().getId());
    			
    			if(!element.getEndNode().hasProperty(longestshortestpath) || 
    					Integer.valueOf(element.getEndNode().getProperty(longestshortestpath).toString()) < lsp
    					)
    				
    				connector.setAuthorProperty(element.getEndNode().getId(),longestshortestpath,Integer.toString(lsp));		
    	    	
    		}

         }
    	return 0;
    	
    }
    
    /*private static long createCommunityExample(DBConnector connector, int communities, int authors){
    	
    	long id_root = connector.createCommunity();
    	
    	for(int i = 0; i < communities;i ++){
    		
    		ArrayList<Long> al_auths = new ArrayList<Long>();
    		
    		long id_c = connector.createCommunity();
    		connector.belongsTo(connector.getGraphDb().getNodeById(id_c), connector.getGraphDb().getNodeById(id_root));
    		
    		for(int j = 0; j< authors*(i+1); j++){
    			long id_a = connector.createAuthor("author"+i+j);
    			al_auths.add(id_a);
    			connector.belongsTo(connector.getGraphDb().getNodeById(id_a), connector.getGraphDb().getNodeById(id_c));
    		}
    		
    		for(int k = 0; k < authors*(i+1)-1;k++){
    			
    			try{
    				
    				Random randomGenerator = new Random();
    				int auth1 = k;//randomGenerator.nextInt(al_auths.size());
    				int auth2 = k+1;randomGenerator.nextInt(al_auths.size());
    			   
    				if(auth1 != auth2)
    				   connector.createPublications(al_auths.get(auth1), al_auths.get(auth2),1);
    			
    			}catch (Exception e) {
					e.printStackTrace();
				}
    			   
    		}
  
    	}
    	return id_root;
    }*/
    
}
