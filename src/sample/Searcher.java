package sample;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Searcher {

    IndexSearcher indexSearcher;
    Directory indexDirectory;
    IndexReader indexReader;
    QueryParser queryParser;
    Controller controller;
    String[] finalString;
    Query query;

        public Searcher(String indexDirectoryPath) throws IOException {
        Path indexPath = Paths.get(indexDirectoryPath);
        indexDirectory = FSDirectory.open(indexPath);
        indexReader = DirectoryReader.open(indexDirectory);
        indexSearcher = new IndexSearcher(indexReader);

        ArrayList<String> fields = new ArrayList();

        controller = new Controller();

        if (controller.varPlaces) {
            fields.add(LuceneConstants.PLACES);
        }
        if (controller.varPeople) {
            fields.add(LuceneConstants.PEOPLE);
        }
        if (controller.varTitle) {
            fields.add(LuceneConstants.TITLE);
        }
        if (controller.varBody) {
            fields.add(LuceneConstants.BODY);
        }

        finalString = new String[fields.size()];

        for (int i=0; i<fields.size(); i++) {
            finalString[i] = fields.get(i);
            System.out.println(fields.get(i));
        }

        queryParser = new MultiFieldQueryParser(finalString, new WhitespaceAnalyzer());
    }

    public TopDocs search(String searchQuery) throws IOException, ParseException {
        query = queryParser.parse(searchQuery);
        //Vector space model
        query.createWeight(indexSearcher, ScoreMode.COMPLETE, 0.5f);
        System.out.println("query: "+ query.toString());
        return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
    }

    public Document getDocument(ScoreDoc scoreDoc) throws CorruptIndexException, IOException {
        return indexSearcher.doc(scoreDoc.doc);
    }

    public void close() throws IOException {
        indexReader.close();
        indexDirectory.close();
    }
}