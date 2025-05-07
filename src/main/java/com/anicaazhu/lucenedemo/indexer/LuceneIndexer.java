package com.anicaazhu.lucenedemo.indexer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;

@Component
public class LuceneIndexer {

    private static final String INDEX_DIR = "lucene_index";
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/taxi";
    private static final String JDBC_USER = "dev";
    private static final String JDBC_PASS = "dev123";

    public void buildIndex() throws IOException, SQLException {
        FSDirectory dir = FSDirectory.open(Paths.get(INDEX_DIR));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(dir, config);

        // 连接数据库
        Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
        Statement stmt = conn.createStatement();

        String sql = "SELECT pickup_location, dropoff_location, total_amount, pickup_datetime FROM taxi_trip";
        ResultSet rs = stmt.executeQuery(sql);

        int count = 0;
        while (rs.next()) {
            Document doc = new Document();

            // 分词字段（可搜索）
            doc.add(new TextField("pickup", rs.getString("pickup_location").toLowerCase(), Field.Store.YES));
            doc.add(new TextField("dropoff", rs.getString("dropoff_location").toLowerCase(), Field.Store.YES));

            // 非索引字段（只展示）
            doc.add(new StoredField("total_amount", rs.getDouble("total_amount")));
            doc.add(new StoredField("pickup_datetime", rs.getString("pickup_datetime")));

            writer.addDocument(doc);
            count++;
            if (count % 100_000 == 0) {
                System.out.println("已写入文档数：" + count);
            }
        }

        writer.close();
        rs.close();
        stmt.close();
        conn.close();
        System.out.println("索引构建完成，共写入文档数：" + count);
    }
}

