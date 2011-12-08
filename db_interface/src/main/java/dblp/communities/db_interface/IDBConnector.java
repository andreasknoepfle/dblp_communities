package dblp.communities.db_interface;


public interface IDBConnector {

	public abstract Long createPublications(Long author_from,
			Long author_to, int weight);

	public abstract void setAuthorProperty(Long node, String id, String value);

	public abstract  Long createAuthor(String name);
}