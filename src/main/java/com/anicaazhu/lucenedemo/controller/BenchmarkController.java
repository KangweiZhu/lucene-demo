package com.anicaazhu.lucenedemo.controller;

import com.anicaazhu.lucenedemo.service.BenchmarkService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/benchmark")
public class BenchmarkController {

    private final BenchmarkService benchmarkService;

    public BenchmarkController(BenchmarkService benchmarkService) {
        this.benchmarkService = benchmarkService;
    }

    @PostMapping("/exact")
    public List<Map<String, Object>> exactBatch(@RequestBody List<String> pickups) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        for (String pickup : pickups) {
            results.add(benchmarkService.benchmarkExact(pickup));
        }
        return results;
    }

    @PostMapping("/fuzzy")
    public Map<String, Object> fuzzy(@RequestParam("pickup") String pickup) throws Exception {
        return benchmarkService.benchmarkFuzzy(pickup);
    }

    @GetMapping("/all")
    public Map<String, Object> full() throws Exception {
        return benchmarkService.benchmarkAll();
    }
}
