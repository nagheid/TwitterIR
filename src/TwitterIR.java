import java.util.ArrayList;
import java.util.Map.Entry;

public class TwitterIR {

	public static void main (String [] args) {
		Indexer indexer = new Indexer();
		XMLParser parser = new XMLParser();
		
		ArrayList<Entry<String, String>> queries = parser.getQueries();
		
		try {
			// Preprocessing and indexing (steps 1 + 2)
			indexer.init();

			// Retrieval and ranking + results file (step 3 + 4)
			indexer.searchQueries(queries);

			// Verify indexing and report
			indexer.indexStats();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
}
