
/**
 * @author Abdel Gomez-Perez 	6201022
 * @author Naglaa Eid 			6177022
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

public class Indexer {
	
	// Static variables
	private static final Version	LUCENE_VERSION		= Version.LUCENE_46;
	private static final String		STOP_WORDS_PATH 	= "../collection/StopWords.txt";
	private static final String 	INPUT_INDEX_PATH 	= "../collection/Trec_microblog11.txt";
	private static final String 	OUTPUT_INDEX_PATH 	= "../index";
	private static final String 	OUTPUT_RESULTS_PATH	= "../queries/results.txt";
	private static final String 	OUTPUT_ANSWERS_PATH	= "../queries/answers.txt";
	private static final String 	OUTPUT_TOKENS_PATH	= "../index/100tokens.txt";
	
	// Index manipulation objects
	private IndexWriter 	indexWriter;
	private IndexReader 	indexReader;
	private IndexSearcher 	indexSearcher;
	
	// Preprocessing analyzer
	private Analyzer 		analyzer;
	
	// File resources
	private PrintWriter 	resultsWriter;
	private PrintWriter 	answersWriter;
	
	public Indexer() {}

	/*
	 * INITIALIZE HELPERS
	 */
	public void init() throws IOException {
		System.out.println("Initializing Lucene Helpers");
		System.out.println("---------------------------");	
		
		analyzerInit();
		indexInit();
		searchInit();
	}
	
	/*
	 * Step 1:
	 * 		PREPROCESSIING
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
	 * Step 2:
	 * 		INDEXING
	 */
	public void indexInit() throws IOException {
		// Status report
		System.out.println("Creating index...");
		
		// Create resources
		Directory outputDir = FSDirectory.open(new File(OUTPUT_INDEX_PATH));
		BufferedReader	br	= new BufferedReader(new FileReader(INPUT_INDEX_PATH)); //, Charset.forName("UTF-8")));

		// Initialize indexer
		IndexWriterConfig indexConfig;
		indexConfig 	= new IndexWriterConfig(LUCENE_VERSION, analyzer); 
		indexWriter 	= new IndexWriter(outputDir, indexConfig);
		
		// Start timer before indexing starts
		long startTime = System.nanoTime();

		// Read index input file
		String line;
		while ( (line = br.readLine()) != null ) {
			// Create document
			Document doc 	= new Document();

			// Parse input file
			String[]	parts 		= line.split("\t");
			String		tweetId 	= parts[0];
			String 		message 	= parts[1];

			// Add fields to the documents
			doc.add( new StringField("tweetId", tweetId, Field.Store.YES) ); 
			doc.add( new TextField	("message", message, Field.Store.YES) );

			// Add new document to the indexer 
			// Or update if already exists with that tweet ID
			indexWriter.updateDocument(new Term("tweetId", tweetId), doc);
		}

		// End timer when indexing is done
		long endTime = System.nanoTime();

		long duration = endTime - startTime;
		
		// Close indexWriter
		indexWriter.close();
		
		// Initialize indexReader when output directory is still open
		indexReader  = DirectoryReader.open(outputDir);

		// Close resources
		outputDir.close();		
		br.close();

		// Status report
		System.out.println("Index created with " + indexReader.numDocs() + " documents");
		System.out.println("Time taken to create index: " + duration + " nanoseconds");
	}
	
	/*
	 * Step 3:
	 * 		RETRIEVAL AND RANKING
	 */
	
	public void searchInit() {
		indexSearcher = new IndexSearcher(indexReader);
	}

	public void searchQueries(ArrayList<Entry<String, String>> queries) throws IOException, ParseException {
		System.out.println();
		System.out.println("Searching XML Queries");
		System.out.println("---------------------");
		
		// Open resources
		resultsInit();
		answersInit();
		
		long startTime = System.nanoTime();
		
		// Result retrieval and reporting
		for (Entry<String, String> q : queries) {
		    String 	queryNum 	= q.getKey();	
		    String	queryText	= q.getValue();
		    
		 	// Search queries
			String[][] results 	= searchIndex(queryText);
			
			// Report top 10 answers for queries 1 to 25
			if ( Integer.parseInt(queryNum) >= 1 && Integer.parseInt(queryNum) <= 25 ) {
				answersAppend(queryNum, queryText, results);
			}
			
			// Report results
			resultsAppend(queryNum, results, "XMLQueriesRun");
		}
		
		long endTime = System.nanoTime();
		
		// Close resources
		resultsWriter.close();
		answersWriter.close();
		
		System.out.println("Time taken to process and search 49 queries: " + (endTime-startTime) + " nanoseconds");
	}
	
	public String[][] searchIndex(String searchString) throws IOException, ParseException {
		// Status report
		System.out.println("Searching for '" + searchString + "'...");
		
		// Init
        String searchField 	= "message";
        
        // Create QueryParser object
        QueryParser queryParser = new QueryParser(LUCENE_VERSION, searchField, this.analyzer);
        
        // To allow searching for tokens that contains the search query
		queryParser.setAllowLeadingWildcard(true);
		
		// To avoid getting a constant score of 1.0 from using wildcards
		queryParser.setMultiTermRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE); 
		
		// To avoid "TooManyClauses exception"
		BooleanQuery.setMaxClauseCount((int) Integer.MAX_VALUE);
					
		// Wraps every term in the query with wildcards
		Query query = queryParser.parse("*"+searchString+"*");
		
		// Run query and get result stats
		// Start timer before searching
		long startTime = System.nanoTime();
		TopDocs results = indexSearcher.search(query, indexReader.numDocs());
		long endTime   = System.nanoTime();
		
		ScoreDoc[] 	hits 	= results.scoreDocs;	    
	    int			numHits = results.totalHits;
	    String[][]	hitsInfo = new String[ Math.min(numHits, 1000) ][4];
	    
	    // Gather doc and score information for a max of 1000 results
	    for (int i = 0; i < hitsInfo.length; i++) {
	    	ScoreDoc hit = hits[i];
    		Document doc = indexSearcher.doc(hit.doc);

      		hitsInfo[i][0] = doc.get("tweetId");			// docno
      		hitsInfo[i][1] = Integer.toString(i+1);			// rank
      		hitsInfo[i][2] = Float.toString(hit.score); 	// score
      		hitsInfo[i][3] = doc.get("message");			// message
	    }
	    
	 	// Status report
	    String found = (numHits==0)?"not ":"" + "found";
	    System.out.println("Query " + found + " with " + numHits + " total matching documents in " + (endTime-startTime) + " nanoseconds");
		          
	    // Needed for creating results file
	    return hitsInfo;
    }
	
	/* 
	 * Step 4:
	 * 		RESULTS 
	 */
	public void resultsInit() throws FileNotFoundException, UnsupportedEncodingException {
		resultsWriter = new PrintWriter(OUTPUT_RESULTS_PATH, "UTF-8");
		resultsWriter.println("topic_id \t Q0 \t docno \t rank \t score \t tag");
	}

	public void resultsAppend(String queryNum, String [][] queryResults, String tag) {
		
		for ( String[] doc: queryResults ) {
			resultsWriter.println(queryNum + " \t Q0 \t " + doc[0] + " \t " + doc[1] + " \t " + doc[2] + " \t " + tag);
		}
		
	}
	
	/*
	 * Answers file
	 */
	public void answersInit() throws FileNotFoundException, UnsupportedEncodingException {
		answersWriter = new PrintWriter(OUTPUT_ANSWERS_PATH, "UTF-8");
	}

	public void answersAppend(String queryNum, String queryText, String[][] resultsList) {

		answersWriter.println("Query " + queryNum + "(" + queryText + ")");
		answersWriter.println("---------");
				
		for (int i = 0; i < Math.min(10,resultsList.length) ; i++) {
    	
			String rank  = resultsList[i][1];
    		String score = resultsList[i][2];
    		String msg	 = resultsList[i][3];
          	
    		answersWriter.println(rank + ". " + msg + " (score="+score +")");
	    }
		
		answersWriter.println();
	}	
	
	/*
	 * REPORTING
	 */
	public void indexStats() throws IOException {
		System.out.println();
		System.out.println("Index Stats");
		System.out.println("-----------");
		System.out.println("Total number of documents:\t" + indexReader.numDocs());	// should be 45,750
		int numTokens = getSampleTokens();
		System.out.println("Total number of tokens:\t\t" + numTokens);
		
		System.out.println("For sample of 100 tokens, see:\t\t" + OUTPUT_TOKENS_PATH);
		System.out.println("For top 10 answers to queries 1 to 25, see:\t" + OUTPUT_ANSWERS_PATH);
	}
	
	public int getSampleTokens() throws IOException {
		PrintWriter tokenWriter = new PrintWriter(OUTPUT_TOKENS_PATH, "UTF-8");
		tokenWriter.println("Sample of 100 tokens from vocab:");
			
		int numTokens = 0;

		Fields 		fields 	 = MultiFields.getFields(indexReader);
        Terms 		terms 	 = fields.terms("message");
        TermsEnum 	iterator = terms.iterator(null);
        BytesRef 	byteRef;
        
        while( (byteRef = iterator.next()) != null ) { 
            String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
            
            // This gets exactly 100 tokens for our particular input
            if ( numTokens % 980 == 0 )
            	tokenWriter.println(term); 
            
            numTokens++;
        }
        
        tokenWriter.close();
        
        return numTokens;
	}

	public boolean getTokenFromIndex(String token) throws IOException {

		Fields 		fields 	 = MultiFields.getFields(indexReader);
        Terms 		terms 	 = fields.terms("message");
        TermsEnum 	iterator = terms.iterator(null);
        BytesRef 	byteRef;
        
        while( (byteRef = iterator.next()) != null ) { 
            String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
            
            if ( term.contains(token) ) {
            	System.out.println("Token, " + token + ", found at: " + term);
            	return true;
            }
        }
        
        return false;
	}
}