
/**
 * @author Abdel Gomez-Perez 6201022
 * @author Naglaa Eid 6177022
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.parser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {
	
	// Static variables
	private static final Version	LUCENE_VERSION		= Version.LUCENE_46;
	private static final String		STOP_WORDS_PATH 	= "../collection/StopWords.txt";
	private static final String 	INPUT_INDEX_PATH 	= "../collection/Trec_microblog11.txt";
	private static final String 	OUTPUT_INDEX_PATH 	= "../index-results";
	
	// Index manipulation objects
	private IndexWriter 	indexWriter;
	private IndexReader 	indexReader;
	private IndexSearcher 	indexSearcher;
	
	// Preprocessing analyzer
	private Analyzer 		analyzer;

	public Indexer() {}

	/*
	 * PREPROCESSIING
	 */
	public void analyzerInit() throws IOException {
		// Create resource
		Reader fd = new FileReader(STOP_WORDS_PATH);

		// Initialize analyzer
		this.analyzer = new StandardAnalyzer(LUCENE_VERSION, fd);
		
		// Close resource
		fd.close();
	}

	/*
	 * INDEXING
	 */
	public void indexInit() throws IOException {
		// Create resources
		Directory outputDir = FSDirectory.open(new File(OUTPUT_INDEX_PATH));
		BufferedReader	br	= new BufferedReader(new FileReader(INPUT_INDEX_PATH)); //, Charset.forName("UTF-8")));

		// Initialize indexer
		IndexWriterConfig indexConfig;
		indexConfig 	= new IndexWriterConfig(LUCENE_VERSION, analyzer); 
		indexWriter 	= new IndexWriter(outputDir, indexConfig);

		// Read index input file
		String line;
		while ( (line = br.readLine()) != null ) {
			// Create document
			Document doc 	= new Document();

			// Parse input file
			String[]	parts 		= line.split("\t");
			String		documentId 	= parts[0];
			String 		twitterMsg 	= parts[1];

			// Add fields to the documents
			doc.add( new StringField("documentId", documentId, Field.Store.YES) ); 
			doc.add( new TextField	("twitterMsg", twitterMsg, Field.Store.YES) );

			// Add document to the indexer
			indexWriter.addDocument(doc);
		}

		// Close resources
		br.close();
		indexWriter.close();
	}

	public void indexStats() {
		System.out.println("Num docs: " + indexReader.numDocs());
	}
	
	public void get100Tokens() {
		//TokenSources.getTokenStream(indexReader, doc_id, )
	}
	
	public void readerInit() throws IOException {
		File indexDirFile = new File(OUTPUT_INDEX_PATH);
		Directory dir = FSDirectory.open(indexDirFile);
		indexReader  = DirectoryReader.open(dir);
		indexSearcher = new IndexSearcher(indexReader);
	}
	
	public void searchInit() {
		indexSearcher = new IndexSearcher(indexReader);
	}

	public void searchIndex(String searchString) throws IOException, ParseException, Exception {
		System.out.println("Searching for '" + searchString + "'");
		
        String searchField = "twitterMsg";
		QueryParser queryParser = new QueryParser(LUCENE_VERSION, searchField, this.analyzer);
		Query query = queryParser.parse(searchString);
		TopDocs results = indexSearcher.search(query, 1000);
		ScoreDoc[] hits = results.scoreDocs;	    
	    int numTotalHits = results.totalHits;
	    System.out.println(numTotalHits + " total matching documents");
	    
//		Iterator<TopDocs> it = results.iterator();
//		while (it.hasNext()) {
//			Hit hit = it.next();
//			Document document = hit.getDocument();
//			String msg = document.get(searchField);
//			System.out.println("Hit: " + msg);
//		}
	    
	    int start = 0;
	    int end = hits.length; //Math.min(numTotalHits, 1);

	    //while (true) {
	        /*if (end > hits.length) {
	          System.out.println("Only results 1 - " + hits.length +" of " + numTotalHits + " total matching documents collected.");
	          hits = indexSearcher.search(query, numTotalHits).scoreDocs;
	        }
	        
	        end = Math.min(hits.length, start + 1);
	        */
	        for (int i = start; i < end; i++) {
	            System.out.println("doc="+hits[i].doc+" score="+hits[i].score);

		          Document doc = indexSearcher.doc(hits[i].doc);
		          String twitterMsg = doc.get("twitterMsg");
		          
		          System.out.println((i+1) + ". " + twitterMsg);
	        }
		          
	       // }
	    }
	
	public boolean getTermIndex(String token) {
		// getCommitData()
		// extract map value for key=token
		return false;
	}

}