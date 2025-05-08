package com.anicaazhu.lucenedemo.indexer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.MMapDirectory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.sql.*;

@Component
public class LuceneIndexer {

    private static final Path INDEX_DIR = Path.of("lucene_index");
    private static final String JDBC_URL  = "jdbc:mysql://localhost:3306/taxi";
    private static final String JDBC_USER = "dev";
    private static final String JDBC_PASS = "dev123";

    public void buildIndex() throws Exception {
        Analyzer keywordAnalyzer = new KeywordAnalyzer();
        IndexWriterConfig cfg = new IndexWriterConfig(keywordAnalyzer);
        try (IndexWriter writer = new IndexWriter(MMapDirectory.open(INDEX_DIR), cfg);
             Connection conn   = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
             Statement  stmt   = conn.createStatement();
             ResultSet  rs     = stmt.executeQuery(
                     "SELECT pickup_location, dropoff_location, total_amount, pickup_datetime FROM taxi_trip"
             )) {

            int count = 0;

            while (rs.next()) {
                String pickup  = rs.getString("pickup_location").toLowerCase();
                String dropoff = rs.getString("dropoff_location").toLowerCase();

                Document doc = new Document();
                doc.add(new StringField("pickup_exact",  pickup,  Field.Store.YES));
                doc.add(new StringField("dropoff_exact", dropoff, Field.Store.YES));
                doc.add(new TextField("pickup_fuzzy",  pickup,  Field.Store.NO));
                doc.add(new TextField("dropoff_fuzzy", dropoff, Field.Store.NO));
                doc.add(new StoredField("pickup_display",  pickup));
                doc.add(new StoredField("dropoff_display", dropoff));
                doc.add(new StoredField("total_amount",    rs.getDouble("total_amount")));
                doc.add(new StoredField("pickup_datetime", rs.getString("pickup_datetime")));

                writer.addDocument(doc);
                if (++count % 100_000 == 0) {
                    System.out.printf("Finished writing %d %n", count);
                }
            }
            writer.commit();
            System.out.printf("Index Completed, in total %d %n", count);
        }
    }
}
