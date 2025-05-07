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

    private final String INDEX_DIR = "lucene_index"; // 索引路径

    public List<SearchResult> search(String pickupKeyword, String dropoffKeyword) throws IOException {
        DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIR)));
        IndexSearcher searcher = new IndexSearcher(reader);

        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

        if (pickupKeyword != null && !pickupKeyword.isBlank()) {
            queryBuilder.add(new TermQuery(new Term("pickup", pickupKeyword.toLowerCase())), BooleanClause.Occur.MUST);
        }

        if (dropoffKeyword != null && !dropoffKeyword.isBlank()) {
            queryBuilder.add(new TermQuery(new Term("dropoff", dropoffKeyword.toLowerCase())), BooleanClause.Occur.MUST);
        }

        TopDocs topDocs = searcher.search(queryBuilder.build(), 20);
        List<SearchResult> results = new ArrayList<>();

        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            results.add(new SearchResult(
                    doc.get("pickup"),
                    doc.get("dropoff"),
                    Double.parseDouble(doc.get("total_amount")),
                    doc.get("pickup_datetime")
            ));
        }

        reader.close();
        return results;
    }
}
