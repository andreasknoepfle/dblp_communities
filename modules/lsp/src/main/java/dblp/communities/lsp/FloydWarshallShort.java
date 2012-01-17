package dblp.communities.lsp;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dblp.communities.db_interface.Pair;

public class FloydWarshallShort {

		  
	      protected short init; 
	      protected Set<Long> nodes;
	      protected Set<Pair> relationships;
	      short[][] costs;
	      Map<Long,Integer> index; 
	     
	
	      public FloydWarshallShort( Set<Long> nodes, Set<Pair> relationships )
	      {
	          super();
	          this.init = Short.MAX_VALUE;
	          this.nodes = nodes;
	          this.relationships = relationships;
	      }
	
	    
	     public void run() throws Exception
	     {
	    	 int n = nodes.size();
	        costs = new short[n][n];
	        index = new HashMap<Long,Integer>();
	        for ( int i = 0; i < n; ++i )
	        {
	            for ( int j = 0; j < n; ++j )
	             {
	                 costs[i][j] = init;
	             }
	             costs[i][i] = 0;
	         }
	        int num = 0;
	         for ( Long node : nodes )
	         {
	            index.put( node, num );
	            num++;
	         }
	        
	         for ( Pair relationship : relationships )
	         {
	        	 int i1 = index.get( relationship.getFrom() );
	        	 int i2 = index.get( relationship.getTo() );
	             costs[i1][i2] = 1;
	             costs[i2][i1] = 1;
	         }
	         
	         for ( int v = 0; v < n; ++v )
	         {
	        	 if(v%10==0) {
	        		 System.out.println(v+"/"+n);
	        	 }
	             for ( int i = 0; i < n; ++i )
	             {
	                 for ( int j = 0; j < n; ++j )
	                 {
	                	 short alternative = (short) (costs[i][v]+costs[v][j]);
	                	
	                	 if (  costs[i][j] > alternative  )
	                     {
	                         costs[i][j] = alternative;
	                        
	                     }
	                 }
	             }
	         }
	         
	     }
	     
	     
	     public short maxCost() {
	    	 short max=0;
	    	 for (int i=0;i<costs.length;i++) {
				for(int j=0;j<costs[i].length;j++) {
					if(costs[i][j]>max) {
						max=costs[i][j];
					}
				}
			}
	    	 return max;
	     }
	
	 }
	
	


