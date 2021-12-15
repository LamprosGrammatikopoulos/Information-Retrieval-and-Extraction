package sample;

import javafx.collections.FXCollections;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


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

    public  ObservableList search(String searchQuery) throws IOException, ParseException {
        searcher = new Searcher(indexDir);
        long startTime = System.currentTimeMillis();
        TopDocs hits = searcher.search(searchQuery);
        long endTime = System.currentTimeMillis();

        ObservableList observableList = FXCollections.observableArrayList();

        System.out.println(hits.totalHits +" documents found. Time :" + (endTime - startTime));
        for(ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = searcher.getDocument(scoreDoc);
            System.out.println("File: " + doc.get(LuceneConstants.FILE_PATH));

            observableList.add(doc.get(LuceneConstants.FILE_PATH));
        }
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











