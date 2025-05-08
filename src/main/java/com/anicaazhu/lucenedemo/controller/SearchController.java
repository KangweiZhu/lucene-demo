package com.anicaazhu.lucenedemo.controller;

import com.anicaazhu.lucenedemo.model.SearchResult;
import com.anicaazhu.lucenedemo.service.LuceneSearchService;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final LuceneSearchService luceneSearchService;

    public SearchController(LuceneSearchService luceneSearchService) {
        this.luceneSearchService = luceneSearchService;
    }

    @GetMapping
    public List<SearchResult> search(
            @RequestParam(required = false, name="pickup") String pickup,
            @RequestParam(required = false, name="dropoff") String dropoff
    ) throws IOException, ParseException {
        return luceneSearchService.search(pickup, dropoff);
    }
}
