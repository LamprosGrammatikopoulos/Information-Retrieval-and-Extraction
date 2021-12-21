package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class LuceneTester {

    static String indexDir = "res/Index";
    String dataDir = "res/Data";
    static Indexer indexer;
    Searcher searcher;

    public void createIndex() throws IOException {
        indexer = new Indexer(indexDir);
        int numIndexed;
        long startTime = System.currentTimeMillis();
        numIndexed = indexer.createIndex(dataDir, new TextFileFilter());
        long endTime = System.currentTimeMillis();
        indexer.close();
        System.out.println(numIndexed+" File(s) indexed, time taken: " + (endTime-startTime)+" ms");
    }

    public ObservableList search(String searchQuery, int TopK) throws IOException, ParseException {
        searcher = new Searcher(indexDir);
        long startTime = System.currentTimeMillis();
        TopDocs hits = searcher.search(searchQuery, TopK);
        long endTime = System.currentTimeMillis();

        ScoreDoc[] filterScoreDocsArray = hits.scoreDocs;
        for (int i = 0; i < filterScoreDocsArray.length; ++i) {
            Document d = searcher.getDocument(filterScoreDocsArray[i]);
            System.out.println((i + 1) + ". Score: " + filterScoreDocsArray[i].score);
        }

        ObservableList observableList = FXCollections.observableArrayList();

        System.out.println(hits.totalHits +" documents found. Time :" + (endTime - startTime));

        Analyzer analyzer = new WhitespaceAnalyzer();
        TokenStream tokenStream = analyzer.tokenStream("contents", new StringReader(searchQuery));
        tokenStream = new LowerCaseFilter(tokenStream);
        CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        List<String> termsArrayList= new ArrayList<>();
        while (tokenStream.incrementToken()) {
            termsArrayList.add(term.toString());
        }
        tokenStream.close();
        analyzer.close();


        Controller controller = new Controller();

        int k=0;
        for(ScoreDoc scoreDoc : hits.scoreDocs) {

            Document doc = searcher.getDocument(scoreDoc);
            System.out.println("File: " + doc.get(LuceneConstants.FILE_PATH));

            BufferedReader br = new BufferedReader(new FileReader(doc.get(LuceneConstants.FILE_PATH)));

            String currentLine = "";
            int lineCounter = 0;
            tokenStream.reset();
            String contents = "";

            observableList.add(doc.get(LuceneConstants.FILE_PATH));

            while ((currentLine = br.readLine()) != null) {
                //Print the title line in which term exists
                if (lineCounter == 2) {
                    currentLine = currentLine.replaceAll("<TITLE>", "");
                    currentLine = currentLine.replaceAll("</TITLE>", "");
                    //contents = currentLine + "\n";
                    observableList.add("Score: [" + filterScoreDocsArray[k].score + "] " + currentLine);
                }
                //Else print the body line in which term exists
                else if (lineCounter > 2) {
                    for (int i=0; i<termsArrayList.size(); i++) {
                        if (!termsArrayList.get(i).equals("AND") && !termsArrayList.get(i).equals("OR") && !termsArrayList.get(i).equals("NOT")) {
                            //If current line contains any of the query terms
                            if (currentLine.contains(termsArrayList.get(i))) {
                                //Remove tags
                                currentLine = currentLine.replaceAll("<BODY>", "");
                                currentLine = currentLine.replaceAll("\u0003</BODY>", "");
                                contents = contents + currentLine + "\n";

                            }
                            //Else print the first line of the article
                            else {
                                if (lineCounter == 3) {
                                    //Remove tags
                                    currentLine = currentLine.replaceAll("<BODY>", "");
                                    currentLine = currentLine.replaceAll("\u0003</BODY>", "");
                                    contents = contents + currentLine + "\n";
                                }
                            }
                        }
                    }


                }
                lineCounter ++;
            }//>>>end while read file
            br.close();
            observableList.add(contents);
            observableList.add("");
            k++;
        }//>>>end FOR


        searcher.close();
        return  observableList;
    }

    public static void deleteFile(String str) throws IOException {
        indexer = new Indexer(indexDir);
        File file = new File(str);
        if(file.delete()) {
            indexer.deleteDocument(file);
            System.out.println("File: " + str + " deleted successfully");
        }
        else {
            System.out.println("Failed to delete the file" + str);
        }
        indexer.close();
    }

    public static void addFile(String src) {
        try {
            Path path = null;
            path = Paths.get(src);
            String fileName = path.getFileName().toString();
            Path result = null;
            result = Files.copy(Paths.get(src), Paths.get("res/Data/" + fileName));
            if(result != null) {
                System.out.println("File added successfully.");
                indexer = new Indexer(indexDir);
                File file = new File("res/Data/" + fileName);
                indexer.addDocument(file);
            }else {
                System.out.println("File addition failed.");
            }
            indexer.close();
        } catch (Exception e) {
            System.out.println("Exception while adding file: " + e.getMessage());
        }
    }
}











