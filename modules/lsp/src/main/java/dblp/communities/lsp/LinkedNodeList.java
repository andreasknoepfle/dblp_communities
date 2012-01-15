package dblp.communities.lsp;

import org.neo4j.graphdb.Node;

public class LinkedNodeList{
	
	private LinkedNodeListContainer startnode;
	private LinkedNodeListContainer endnode;
	
	public LinkedNodeListContainer getStartnode() {
		return startnode;
	}

	public void setStartnode(LinkedNodeListContainer startnode) {
		this.startnode = startnode;
	}

	public LinkedNodeListContainer getEndnode() {
		return endnode;
	}

	public void setEndnode(LinkedNodeListContainer endnode) {
		this.endnode = endnode;
	}

	public LinkedNodeList(Node node){
		startnode=new LinkedNodeListContainer(node);
		endnode=startnode;
	}

	public void add(LinkedNodeList list) {
		this.endnode.setNextContainer(list.startnode);
		this.endnode=list.endnode;
		
	}

	public boolean isAuthor() {
		if(startnode==endnode) {
			return true;
		} else {
			return false;
		}
	}
	
}
