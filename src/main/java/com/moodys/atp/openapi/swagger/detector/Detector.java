package com.moodys.atp.openapi.swagger.detector;

import com.deepoove.swagger.diff.SwaggerDiff;
import com.moodys.atp.openapi.swagger.detector.util.FileDetector;
import com.moodys.atp.openapi.swagger.detector.util.Report;
import com.qdesrame.openapi.diff.OpenApiCompare;
import com.qdesrame.openapi.diff.model.ChangedOpenApi;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Detector {

    static final String BENCHMARK_FOLDER = "benchmark";
    static final String CURRENT_FOLDER = "current";
    static final String REPORT_FOLDER = "report";

    public Detector() {
        // Delete out of date report
        Report.deleteFolder(new File(REPORT_FOLDER));

        // Get all files
        List<String> benchmarkFiles = FileDetector.searchFiles(BENCHMARK_FOLDER, "json");
        List<String> currentFiles = FileDetector.searchFiles(CURRENT_FOLDER, "json");

        System.out.println("benchmark: ");
        benchmarkFiles.forEach(item -> System.out.print(item + " "));
        System.out.println();
        System.out.println("current: ");
        currentFiles.forEach(item -> System.out.print(item + " "));
        System.out.println();

        // Difference set
        List<String> benchmarkFileNames = new ArrayList<>(benchmarkFiles);
        benchmarkFileNames = benchmarkFileNames.stream().map(item -> item.substring(BENCHMARK_FOLDER.length() + 1))
                .collect(Collectors.toList());
        List<String> currentFileNames = new ArrayList<>(currentFiles);
        currentFileNames = currentFileNames.stream().map(item -> item.substring(CURRENT_FOLDER.length() + 1))
                .collect(Collectors.toList());
        List<String> benchmarkDifferenceSet = new ArrayList<>(benchmarkFileNames);
        benchmarkDifferenceSet.removeAll(currentFileNames);
        List<String> currentDifferenceSet = new ArrayList<>(currentFileNames);
        currentDifferenceSet.removeAll(benchmarkFileNames);
        if (!benchmarkDifferenceSet.isEmpty()) {
            System.out.println("benchmark difference set: ");
            benchmarkDifferenceSet.forEach(item -> System.out.print(item + " "));
            System.out.println();
        }
        if (!currentDifferenceSet.isEmpty()) {
            System.out.println("benchmark difference set: ");
            currentDifferenceSet.forEach(item -> System.out.print(item + " "));
            System.out.println();
        }

        // Detect
        for (String benchmark : benchmarkFiles) {
            String current = CURRENT_FOLDER + "\\" + benchmark.substring(BENCHMARK_FOLDER.length() + 1);

            System.out.println(benchmark.replace(BENCHMARK_FOLDER, ""));
            try {
                FileReader reader = new FileReader(benchmark);
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(reader);
                // Detect swagger version
                if (obj.containsKey("openapi")) {
                    FileDetector.updateToAnyOf(benchmark);
                    FileDetector.updateToAnyOf(current);
                    ChangedOpenApi diff = OpenApiCompare.fromLocations(benchmark, current);
                    if (diff.isDiff()) {
                        Report.render(diff, benchmark.substring(BENCHMARK_FOLDER.length() + 1));
                    }
                } else {
                    SwaggerDiff diff = SwaggerDiff.compareV2(benchmark, current);
                    if (diff.getNewEndpoints().size() != 0 || diff.getMissingEndpoints().size() != 0
                            || diff.getChangedEndpoints().size() != 0) {
                        Report.render(diff, benchmark.substring(BENCHMARK_FOLDER.length() + 1));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new Detector();
    }
}
