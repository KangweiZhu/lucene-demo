package com.anicaazhu.lucenedemo.controller;

import com.anicaazhu.lucenedemo.indexer.LuceneIndexer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/index")
public class IndexController {

    private final LuceneIndexer indexer;

    public IndexController(LuceneIndexer indexer) {
        this.indexer = indexer;
    }

    @PostMapping("/build")
    public String buildIndex() {
        try {
            indexer.buildIndex();
            return "索引构建完成";
        } catch (Exception e) {
            return "构建失败: " + e.getMessage();
        }
    }
}
