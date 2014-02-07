
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {
	
	// Static variables
	public static final Version LUCENE_VERSION 			= Version.LUCENE_46;
	public static final String 	INPUT_INDEX_DIRECTORY 	= "C:\\Users\\Nagla\\Dropbox\\Winter 2014\\CSI4107\\Assignments\\A1\\IndexInput";
	public static final String 	OUTPUT_INDEX_DIRECTORY 	= "C:\\Users\\Nagla\\Dropbox\\Winter 2014\\CSI4107\\Assignments\\A1\\IndexOutputt";
	
	// Lucene's index writer
	IndexWriter indexWriter;
	Analyzer analyzer;
	
	// Index object
	Iterable<String> indexMap;
	
	public Indexer() {
		this.analyzer = new StandardAnalyzer(LUCENE_VERSION);
	}
	
	/*
	public Indexer(IndexWriter indexWriter) {
		this.indexWriter = indexWriter;
	}
	
	public Indexer(Analyzer analyzer/ *, Tokens tokens* /) {
		this.analyzer = analyzer;
	}

	// Back when i thought we need to tokenize
	public Indexer(String... tokens) {		
		for (String t : tokens) {
		  // access the terms for this field
		  //index.add(t);
		}
	}

	public Indexer(Iterable tokens) {
		
	}
	*/
	
	public void createIndexer() throws CorruptIndexException, LockObtainFailedException, IOException {
		
		//Directory dir = FSDirectory.open(new File(OUTPUT_INDEX_DIRECTORY));
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
			String documentId = line.substring(0, 8);
			String twitterMsg = line.substring(8, line.length()).trim();
			
			doc.add( new StringField("documentId", documentId, Field.Store.YES) ); //, Field.Index.NOT_ANALYZED) );
			// TODO!! pass TokenStream output from StopWordAnalzer
			doc.add( new TextField	("twitterMsg", twitterMsg, Field.Store.YES) );
			//doc.add(new Field("twitterMsg", twitterMsg, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
			
			indexWriter.addDocument(doc);
		}
		
		//indexWriter.optimize();
		indexWriter.close();
	}
	
	
	public boolean getTermIndex(String token) {
		// getCommitData()
		// extract map value for key=token
		return false;
	}

}