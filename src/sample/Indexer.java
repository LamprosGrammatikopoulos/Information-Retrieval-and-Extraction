package sample;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.AttributeFactory;

public class Indexer {
    private IndexWriter writer;
    public Indexer(String indexDirectoryPath) throws IOException {
        //this directory will contain the indexes
        Path indexPath = Paths.get(indexDirectoryPath);
        if(!Files.exists(indexPath)) {
            Files.createDirectory(indexPath);
        }
        //Path indexPath = Files.createTempDirectory(indexDirectoryPath);
        Directory indexDirectory = FSDirectory.open(indexPath);
        //create the indexer
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        writer = new IndexWriter(indexDirectory, config);
    }
    public void close() throws CorruptIndexException, IOException {
        writer.close();
    }
    private Document getDocument(File file) throws IOException {

        Document document = new Document();
        //index file contents
        BufferedReader br = new BufferedReader(new FileReader(file));

        String currentLine ="";
        int lineCounter = 0;
        while ((currentLine = br.readLine()) != null) {

            currentLine = currentLine.toString();

            //Tags Removal
            //Supposing that <PLACES></PLACES>, <PEOPLE></PEOPLE>, <TITLE></TITLE> are one line each
            //First line in txt (<PLACES></PLACES>)
            if(lineCounter == 0) {
                currentLine = currentLine.substring(7,currentLine.length()-9);
            }
            //Second line in txt (<PEOPLE></PEOPLE>)
            else if(lineCounter == 1) {
                currentLine = currentLine.substring(7,currentLine.length()-9);
            }
            //Third line in txt (<TITLE></TITLE>)
            else if(lineCounter == 2) {
                currentLine = currentLine.substring(6,currentLine.length()-8);
            }
            //Fourth line in txt (<BODY></BODY>)
            else if(lineCounter == 3 && currentLine.contains("</BODY>")) {
                currentLine = currentLine.substring(5,currentLine.length()-7);
            }
            //Last line in txt (<BODY>)
            else if (lineCounter == 3 && !currentLine.contains("</BODY>")) {
                currentLine = currentLine.substring(5);
            }
            //Last line in txt (</BODY>)
            else if (currentLine.contains("\u0003</BODY>")) {
                break;
            }

            //Punctuation Removal
            currentLine = currentLine.replaceAll("[^a-zA-Z0-9 ]*", "");

            //Case Folding
            currentLine = currentLine.toLowerCase();

            //Stopwords Removal and tokenization from body
            if (lineCounter >= 2) {
                final String CONTENTS = "contents";
                //Remove below comments for custom stopwords
                //final List<String> stopWords = Arrays.asList("short","test");
                //final CharArraySet stopSet = new CharArraySet(stopWords, true);
                CharArraySet enStopSet = EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;
                //stopSet.addAll(enStopSet);
                try {
                    Analyzer analyzer = new StandardAnalyzer(enStopSet); //stopSet for custom stopwords
                    TokenStream tokenStream = analyzer.tokenStream(CONTENTS, new StringReader(currentLine));
                    CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
                    tokenStream.reset();
                    while(tokenStream.incrementToken()) {
                        System.out.print("[" + term.toString() + "] ");
                    }
                    tokenStream.close();
                    analyzer.close();
                } catch (IOException e) {
                    System.out.println("Exception:n");
                    e.printStackTrace();
                }
            }

            System.out.println("Current===>"+currentLine);

            if(currentLine!=""){
                Field contentField = new Field(LuceneConstants.CONTENTS, currentLine, TextField.TYPE_STORED);
                //index file name
                Field fileNameField = new Field(LuceneConstants.FILE_NAME, file.getName(), StringField.TYPE_STORED);
                //index file path
                Field filePathField = new Field(LuceneConstants.FILE_PATH, file.getCanonicalPath(), StringField.TYPE_STORED);

                document.add(contentField);
                document.add(fileNameField);
                document.add(filePathField);
            }
            lineCounter++;
        }

        //String currentLine =br.readLine().toString();

        br.close();
        return document;
    }
    private void indexFile(File file) throws IOException {
        System.out.println("Indexing "+file.getCanonicalPath());
        Document document = getDocument(file);
        writer.addDocument(document);
    }
    public int createIndex(String dataDirPath, FileFilter filter) throws
            IOException {
        //get all files in the data directory
        File[] files = new File(dataDirPath).listFiles();
        for (File file : files) {
            if(!file.isDirectory()
                    && !file.isHidden()
                    && file.exists()
                    && file.canRead()
                    && filter.accept(file)
            ){
                indexFile(file);
            }
        }
        return writer.numRamDocs();
    }
}
