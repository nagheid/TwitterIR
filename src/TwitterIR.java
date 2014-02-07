public class TwitterIR {

	public static void main (String [] args ) {
		Indexer indexer = new Indexer();
		try {
			indexer.createIndexer();
			indexer.searchIndex("vegas");
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
}
