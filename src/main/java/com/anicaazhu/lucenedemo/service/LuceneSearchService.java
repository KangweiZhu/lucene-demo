package com.anicaazhu.lucenedemo.service;

import com.anicaazhu.lucenedemo.model.SearchResult;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class LuceneSearchService {

    private final String INDEX_DIR = "lucene_index";

    public List<SearchResult> search(String pickupKeyword, String dropoffKeyword) throws IOException {
        DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIR)));
        IndexSearcher searcher = new IndexSearcher(reader);
        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

        if (pickupKeyword != null && !pickupKeyword.isBlank()) {
            String keyword = pickupKeyword.toLowerCase();
            BooleanQuery.Builder pickupQuery = new BooleanQuery.Builder();
            pickupQuery.add(new TermQuery(new Term("pickup_exact", keyword)), BooleanClause.Occur.SHOULD);
            pickupQuery.add(new FuzzyQuery(new Term("pickup_fuzzy", keyword), 1), BooleanClause.Occur.SHOULD);
            queryBuilder.add(pickupQuery.build(), BooleanClause.Occur.MUST);
        }

        if (dropoffKeyword != null && !dropoffKeyword.isBlank()) {
            String keyword = dropoffKeyword.toLowerCase();
            BooleanQuery.Builder dropoffQuery = new BooleanQuery.Builder();
            dropoffQuery.add(new TermQuery(new Term("dropoff_exact", keyword)), BooleanClause.Occur.SHOULD);
            dropoffQuery.add(new FuzzyQuery(new Term("dropoff_fuzzy", keyword), 1), BooleanClause.Occur.SHOULD);
            queryBuilder.add(dropoffQuery.build(), BooleanClause.Occur.MUST);
        }

        TopDocs topDocs = searcher.search(queryBuilder.build(), 10);
        List<SearchResult> results = new ArrayList<>();

        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            results.add(new SearchResult(
                    doc.get("pickup_display"),
                    doc.get("dropoff_display"),
                    Double.parseDouble(doc.get("total_amount")),
                    doc.get("pickup_datetime")
            ));
        }

        reader.close();
        return results;
    }
}
