package sample;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        //config.setOpenMode( IndexWriterConfig.OpenMode.CREATE );                    //Overriding the original index in the folder
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

            System.out.println("Current===>"+currentLine);

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
            //Leave dates, time, words adn decimal number intact
            currentLine = currentLine.replaceAll("([^0-9a-zA-Z]*[\\.,:\\\\-][^0-9a-zA-Z>]+)|([^0-9a-zA-Z\\n][\\.,:\\\\-][^0-9a-zA-Z]*)", " ");
            currentLine = currentLine.replaceAll("([~!@#$%^&*()_+={}\\[\\]\\;\\'\\\"\\<\\>\\|\\?]*)", "");

            //Case Folding
            currentLine = currentLine.toLowerCase();

            //Stopwords Removal from body and tokenization at body
            System.out.print("After tokenization ==>> ");
            if (lineCounter > 2) {         //-----------> <BODY>
                final String CONTENTS = "contents";
                try {
                    //Remove below comments for custom stopwords
                    //final List<String> stopWords = Arrays.asList("short","test");
                    //final CharArraySet stopSet = new CharArraySet(stopWords, true);
                    CharArraySet enStopSet = EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;
                    //stopSet.addAll(enStopSet);
                    Analyzer analyzer = new StandardAnalyzer(enStopSet); //stopSet for custom stopwords
                    TokenStream tokenStream = analyzer.tokenStream(CONTENTS, new StringReader(currentLine));
                    CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
                    tokenStream.reset();
                    while(tokenStream.incrementToken()) {

                        //Stemming in body
                        EnglishStemmer stemmer = new EnglishStemmer();
                        stemmer.setCurrent(term.toString());
                        stemmer.stem();
                        String tmp = stemmer.getCurrent();

                        System.out.print("[" + tmp + "] ");

                        if(term.toString() != "") {

                            //index file contents
                            Field bodyField = new Field(LuceneConstants.BODY, tmp, TextField.TYPE_STORED);
                            document.add(bodyField);

                            //index file name
                            Field fileNameField = new Field(LuceneConstants.FILE_NAME, file.getName(), StringField.TYPE_STORED);
                            //index file path
                            Field filePathField = new Field(LuceneConstants.FILE_PATH, file.getCanonicalPath(), StringField.TYPE_STORED);

                            document.add(fileNameField);
                            document.add(filePathField);
                        }
                    }
                    tokenStream.close();
                    analyzer.close();
                } catch (IOException e) {
                    System.out.println("Exception:n");
                    e.printStackTrace();
                }
            }
            else {                      //-----------> <PLACES>, <PEOPLE>, <TITLE>
                if(currentLine != "") {

                    //index file contents
                    if (lineCounter==0) {
                        Field placesField = new Field(LuceneConstants.PLACES, currentLine, TextField.TYPE_STORED);
                        document.add(placesField);
                    }
                    else if(lineCounter==1) {
                        Field peopleField = new Field(LuceneConstants.PEOPLE, currentLine, TextField.TYPE_STORED);
                        document.add(peopleField);
                    }
                    else {
                        Field titleField = new Field(LuceneConstants.TITLE, currentLine, TextField.TYPE_STORED);
                        document.add(titleField);
                    }
                    //index file name
                    Field fileNameField = new Field(LuceneConstants.FILE_NAME, file.getName(), StringField.TYPE_STORED);
                    //index file path
                    Field filePathField = new Field(LuceneConstants.FILE_PATH, file.getCanonicalPath(), StringField.TYPE_STORED);


                    document.add(fileNameField);
                    document.add(filePathField);
                }
            }

            System.out.println();

            lineCounter++;
        }

        br.close();
        return document;
    }
    private void indexFile(File file) throws IOException {
        System.out.println("Indexing: " + file.getCanonicalPath());
        updateDocument(file);
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

    public void updateDocument(File file) throws IOException {
        Document document = getDocument(file);
        writer.updateDocument(new Term(LuceneConstants.FILE_NAME, file.getName()), document);
    }
    public void addDocument(File file) throws IOException {
        Document document = getDocument(file);
        writer.addDocument(document);
        writer.commit();
    }
    public void deleteDocument(File file) throws IOException {
        //delete indexes for a file
        writer.deleteDocuments(new Term(LuceneConstants.FILE_PATH,file.getPath()));
        writer.commit();
        System.out.println("index contains deleted files: " + writer.hasDeletions());
    }
}