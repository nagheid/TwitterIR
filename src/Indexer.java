
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.parser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {
	
	// Static variables
	public static final Version LUCENE_VERSION 			= Version.LUCENE_46;
	public static final String 	INPUT_INDEX_DIRECTORY 	= "C:\\Users\\Nagla\\Dropbox\\Winter 2014\\CSI4107\\Assignments\\A1\\IndexInput";
	public static final String 	OUTPUT_INDEX_DIRECTORY 	= "C:\\Users\\Nagla\\Dropbox\\Winter 2014\\CSI4107\\Assignments\\A1\\IndexOutput";
	
	// Index manipulation objects
	IndexWriter indexWriter;
	IndexReader indexReader;
	IndexSearcher indexSearcher;
	
	// Preprocessor analyzer
	Analyzer analyzer;
	
	public Indexer() {
		this.analyzer = new StandardAnalyzer(LUCENE_VERSION);
	}
	
	public void createIndexer() throws CorruptIndexException, LockObtainFailedException, IOException {
		
		SimpleFSDirectory indexDir = new SimpleFSDirectory(new File(OUTPUT_INDEX_DIRECTORY));
		  
		
		// Q: 	see here, im passing the analyzer to the indexWriter constructor (thats what evan was talking about)
		//		doesnt that mean that this analyzer will be used downstairs when adding fields to the doc?
		//		and this will be the preprocessing steps of removing the stop words and everything?
		IndexWriterConfig indexConfig;
		indexConfig 	= new IndexWriterConfig(LUCENE_VERSION, analyzer); 
		indexWriter 	= new IndexWriter(indexDir, indexConfig);
		
		File 	inputDir= new File(INPUT_INDEX_DIRECTORY);
		File[] 	files 	= inputDir.listFiles();
		File 	file 	= files[0];				// Only one file there
		
		InputStream    	fis = new FileInputStream(file);
		BufferedReader 	br	= new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
		String         	line;
	
		while ( (line = br.readLine()) != null ) {
			
			Document doc = new Document();
			
			// TODO test this + regex
			String documentId = line.substring(0, 18).trim();
			String twitterMsg = line.substring(18, line.length()).trim();
			
			doc.add( new StringField("documentId", documentId, Field.Store.YES) ); 
			doc.add( new TextField	("twitterMsg", twitterMsg, Field.Store.YES) );
			
			//doc.add(new Field("twitterMsg", twitterMsg, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
			
			indexWriter.addDocument(doc);
		}
		
		indexWriter.close();
	}
	
	
	public void createSearcher() {
	    
	    try{
	         File indexDirFile = new File(OUTPUT_INDEX_DIRECTORY);
	         Directory dir = FSDirectory.open(indexDirFile);
	         indexReader  = DirectoryReader.open(dir);
	         indexSearcher = new IndexSearcher(indexReader);
	    }catch(IOException ioe){
	        ioe.printStackTrace();
	    }
	 
	 }
	
	public void searchIndex(String searchString) throws IOException, ParseException, Exception {
		System.out.println("Searching for '" + searchString + "'");
		
		File indexDirFile = new File(OUTPUT_INDEX_DIRECTORY);
        Directory dir = FSDirectory.open(indexDirFile);
        indexReader  = DirectoryReader.open(dir);
        indexSearcher = new IndexSearcher(indexReader);
        
        String searchField = "twitterMsg";
		QueryParser queryParser = new QueryParser(LUCENE_VERSION, searchField, this.analyzer);
		Query query = queryParser.parse(searchString);
		TopDocs results = indexSearcher.search(query, 5);
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

	    while (true) {
	        if (end > hits.length) {
	          System.out.println("Only results 1 - " + hits.length +" of " + numTotalHits + " total matching documents collected.");
	          hits = indexSearcher.search(query, numTotalHits).scoreDocs;
	        }
	        
	        end = Math.min(hits.length, start + 1);
	        
	        for (int i = start; i < end; i++) {
	            System.out.println("doc="+hits[i].doc+" score="+hits[i].score);

		          Document doc = indexSearcher.doc(hits[i].doc);
		          String twitterMsg = doc.get("twitterMsg");
		          
		          System.out.println((i+1) + ". " + twitterMsg);
	        }
		          
	        }
	    }
	
	public boolean getTermIndex(String token) {
		// getCommitData()
		// extract map value for key=token
		return false;
	}

}