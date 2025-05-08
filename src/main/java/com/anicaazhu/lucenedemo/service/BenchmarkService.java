package com.anicaazhu.lucenedemo.service;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.MMapDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BenchmarkService {

    private static final Logger log = LoggerFactory.getLogger(BenchmarkService.class);

    private final DataSource dataSource;
    private static final Path INDEX_PATH = Path.of("lucene_index");

    public BenchmarkService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, Object> benchmarkExact(String pickup) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pickup", pickup);

        long luceneHits;
        long luceneTime;
        try (DirectoryReader reader = DirectoryReader.open(MMapDirectory.open(INDEX_PATH))) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Query query = new org.apache.lucene.search.TermQuery(
                    new Term("pickup_exact", pickup.toLowerCase())
            );
            long t0 = System.currentTimeMillis();
            luceneHits = searcher.count(query);
            luceneTime = System.currentTimeMillis() - t0;
        }

        int mysqlHits;
        long mysqlTime;
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT COUNT(*) FROM taxi_trip WHERE pickup_location = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, pickup);
            long t0 = System.currentTimeMillis();
            ResultSet rs = ps.executeQuery();
            rs.next();
            mysqlHits = rs.getInt(1);
            mysqlTime = System.currentTimeMillis() - t0;
        }

        result.put("lucene_exact_hits", luceneHits);
        result.put("lucene_exact_time_ms", luceneTime);
        result.put("mysql_exact_hits", mysqlHits);
        result.put("mysql_exact_time_ms", mysqlTime);

        log.info("[EXACT] pickup={} | lucene_hits={} ({} ms) | mysql_hits={} ({} ms)",
                pickup, luceneHits, luceneTime, mysqlHits, mysqlTime);
        return result;
    }

    public Map<String, Object> benchmarkFuzzy(String pickup) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pickup", pickup);

        // Lucene
        long luceneHits;
        long luceneTime;
        try (DirectoryReader reader = DirectoryReader.open(MMapDirectory.open(INDEX_PATH))) {
            IndexSearcher searcher = new IndexSearcher(reader);

            Query query = new PrefixQuery(
                    new Term("pickup_fuzzy", pickup.toLowerCase())
            );

            long t0 = System.currentTimeMillis();
            luceneHits = searcher.count(query);
            luceneTime = System.currentTimeMillis() - t0;
        }

        int mysqlHits;
        long mysqlTime;
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT COUNT(*) FROM taxi_trip WHERE pickup_location LIKE ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + pickup + "%");
            long t0 = System.currentTimeMillis();
            ResultSet rs = ps.executeQuery();
            rs.next();
            mysqlHits = rs.getInt(1);
            mysqlTime = System.currentTimeMillis() - t0;
        }

        result.put("lucene_fuzzy_hits", luceneHits);
        result.put("lucene_fuzzy_time_ms", luceneTime);
        result.put("mysql_fuzzy_hits", mysqlHits);
        result.put("mysql_fuzzy_time_ms", mysqlTime);

        log.info("[FUZZY] pickup={} | lucene_hits={} ({} ms) | mysql_hits={} ({} ms)",
                pickup, luceneHits, luceneTime, mysqlHits, mysqlTime);
        return result;
    }

    public Map<String, Object> benchmarkAll() throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();

        long luceneHits;
        long luceneTime;
        try (DirectoryReader reader = DirectoryReader.open(MMapDirectory.open(INDEX_PATH))) {
            long t0 = System.currentTimeMillis();
            luceneHits = reader.maxDoc();
            luceneTime = System.currentTimeMillis() - t0;
        }

        // MySQL
        int mysqlHits;
        long mysqlTime;
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT COUNT(*) FROM taxi_trip";
            PreparedStatement ps = conn.prepareStatement(sql);
            long t0 = System.currentTimeMillis();
            ResultSet rs = ps.executeQuery();
            rs.next();
            mysqlHits = rs.getInt(1);
            mysqlTime = System.currentTimeMillis() - t0;
        }

        result.put("lucene_all_hits", luceneHits);
        result.put("lucene_all_time_ms", luceneTime);
        result.put("mysql_all_hits", mysqlHits);
        result.put("mysql_all_time_ms", mysqlTime);

        log.info("[ALL] lucene_hits={} ({} ms) | mysql_hits={} ({} ms)",
                luceneHits, luceneTime, mysqlHits, mysqlTime);
        return result;
    }
}
