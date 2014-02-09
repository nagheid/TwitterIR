public class TwitterIR {

	public static void main (String [] args) {
		Indexer indexer = new Indexer();
		XMLParser parser = new XMLParser();
		
		try {
		    parser.createXMLFromQueriesFile();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
		try {
			// Preprocessing and indexing (steps 1 + 2)
			indexer.init();

			// Retrieval and ranking (step 3)
			indexer.searchQueries();
						
			// Results file (step 4)
			// TODO
			
			// Verify indexing and report
			indexer.indexStats();
			indexer.getTokenFromIndex("http");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
}
