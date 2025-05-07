package com.anicaazhu.lucenedemo.controller;

import com.anicaazhu.lucenedemo.model.SearchResult;
import com.anicaazhu.lucenedemo.service.LuceneSearchService;
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
            @RequestParam(required = false) String pickup,
            @RequestParam(required = false) String dropoff
    ) throws IOException {
        return luceneSearchService.search(pickup, dropoff);
    }
}
