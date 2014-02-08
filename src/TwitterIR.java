public class TwitterIR {

	public static void main (String [] args) {
		Indexer indexer = new Indexer();
		try {
			indexer.analyzerInit();
			indexer.indexInit();
			
			indexer.readerInit();
			indexer.searchInit();
			
			indexer.indexStats();
			
			indexer.searchIndex("vegas");
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
}
