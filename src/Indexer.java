
/**
 * @author Abdel Gomez-Perez 	6201022
 * @author Naglaa Eid 			6177022
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
	
	// Index manipulation objects
	private IndexWriter 	indexWriter;
	private IndexReader 	indexReader;
	private IndexSearcher 	indexSearcher;
	
	// Preprocessing analyzer
	private Analyzer 		analyzer;

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

			// Add new document to the indexer 
			// Or update if already exists with that tweet ID
			indexWriter.updateDocument(new Term("documentId", documentId), doc);
		}

		// Initialize indexReader when output directory is still open
		indexReader  = DirectoryReader.open(outputDir);

		// Close resources
		indexWriter.close();
		outputDir.close();		
		br.close();

		// Status report
		System.out.println("Index created with " + indexReader.numDocs() + " documents");
	}

	public void searchInit() {
		indexSearcher = new IndexSearcher(indexReader);
	}
	
	
	/*
	 * Step 3:
	 * 		RETRIEVAL AND RANKING
	 */

	public void searchQueries() throws IOException, ParseException {
		System.out.println();
		System.out.println("Searching XML Queries");
		System.out.println("---------------------");
		
		// TODO parse XML then pass to searcher here
		//searchIndex("vegas"); 		// should return 58
		//searchIndex("http");  		// should return ~27,500
		//searchIndex("BBC world"); 	// should be ~1.6k
		searchIndex("0.0mm");			// 2 retrieved with 1.0 score ?? TODO check manually
		searchIndex("bit.ly");			// 1 retrieved with 1.0 score ?? TODO check manually
	}
	
	public TopDocs searchIndex(String searchString) throws IOException, ParseException {
		// Status report
		System.out.println("Searching for '" + searchString + "'...");
		
        String searchField = "twitterMsg";
		
        QueryParser queryParser = new QueryParser(LUCENE_VERSION, searchField, this.analyzer);
        // To allow searching for tokens that contains the search query
		queryParser.setAllowLeadingWildcard(true);
		// To avoid getting a constant score of 1.0 from using wildcards
		queryParser.setMultiTermRewriteMethod(MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT);
		
		// TODO CONSTANT_SCORE_*_REWRITE_DEFAULT gets scores higher than 1
		
		Query query = queryParser.parse("*"+searchString+"*");
		//Query newQuery = query.rewrite(indexReader);

		//Query query = new WildcardQuery(new Term(searchField, "*"+searchString+"*"));

		// TODO scores changed from 4.something to 0~0.5 when added wildcard
		//		run trekeval then fix here
		// TODO also check if wildcard used on all terms
		TopDocs results = indexSearcher.search(query, indexReader.numDocs());
		ScoreDoc[] hits = results.scoreDocs;	    
	    int numTotalHits = results.totalHits;
	    
//		Iterator<TopDocs> it = results.iterator();
//		while (it.hasNext()) {
//			Hit hit = it.next();
//			Document document = hit.getDocument();
//			String msg = document.get(searchField);
//			System.out.println("Hit: " + msg);
//		}
	    
	    for (int i = 0; i < hits.length; i++)
	    {
	    	if ( i == 0 || i == hits.length-1 ) {
	    		System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
	
	    		Document doc = indexSearcher.doc(hits[i].doc);
	          	String twitterMsg = doc.get("twitterMsg");
	          
	          	System.out.println((i+1) + ". " + twitterMsg);
	    	}
	    }
	    
	 	// Status report
	    System.out.println("Query found with " + numTotalHits + " total matching documents");
		          
	    // Needed for creating results file
	    return results;
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
	}
	
	public int getSampleTokens() throws IOException {
		System.out.println("Sample of 100 tokens from vocab: <token> (<doc-freq>)");
		System.out.print("\t");
		
		int numTokens = 0;

		Fields 		fields 	 = MultiFields.getFields(indexReader);
        Terms 		terms 	 = fields.terms("twitterMsg");
        TermsEnum 	iterator = terms.iterator(null);
        BytesRef 	byteRef;
        
        while( (byteRef = iterator.next()) != null ) { 
            String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
            
            // This gets exactly 100 tokens for our particular input
            if ( numTokens % 980 == 0 )
            	System.out.print(term + " (" + iterator.docFreq() + ")\t");
            
            if ( numTokens % 5000 == 0)
            	System.out.print("\n\t");
            
            numTokens++;
        }
        
        System.out.println();
        
        return numTokens;
	}

	public boolean getTokenFromIndex(String token) throws IOException {

		Fields 		fields 	 = MultiFields.getFields(indexReader);
        Terms 		terms 	 = fields.terms("twitterMsg");
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