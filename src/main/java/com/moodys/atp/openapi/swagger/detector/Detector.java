package com.moodys.atp.openapi.swagger.detector;

import com.deepoove.swagger.diff.SwaggerDiff;
import com.moodys.atp.openapi.swagger.detector.util.FileDetector;
import com.qdesrame.openapi.diff.OpenApiCompare;
import com.qdesrame.openapi.diff.model.ChangedOpenApi;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Detector {

    static final String BENCHMARK_FOLDER = "benchmark";
    static final String CURRENT_FOLDER = "current";
    static final String REPORT_FOLDER = "report";

    public Detector() {
        // Delete out of date report
        deleteFolder(new File(REPORT_FOLDER));

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

            try {
                FileReader reader = new FileReader(benchmark);
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(reader);
                // Detect swagger version
                try {
                    if (obj.containsKey("openapi")) {
                        System.out.println("Detect: " + benchmark + " " + current);
                        ChangedOpenApi diff = OpenApiCompare.fromLocations(benchmark, current);
                        if (diff.isDiff()) {
                            this.render(diff, benchmark.substring(BENCHMARK_FOLDER.length() + 1));
                        }
                    } else {
                        SwaggerDiff diff = SwaggerDiff.compareV2(benchmark, current);
                        if (diff.getNewEndpoints().size() != 0 || diff.getMissingEndpoints().size() != 0
                                || diff.getChangedEndpoints().size() != 0) {
                            this.render(diff, benchmark.substring(BENCHMARK_FOLDER.length() + 1));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void render(ChangedOpenApi diff, String pathname) {
        String html = new com.qdesrame.openapi.diff.output.HtmlRender("Changelog",
                "http://deepoove.com/swagger-diff/stylesheets/demo.css")
                .render(diff);

        try {
            pathname = REPORT_FOLDER + "\\" + pathname.substring(0, pathname.lastIndexOf(".")) + ".html";
            String folderPath = pathname.substring(0, pathname.lastIndexOf("\\"));
            File folder = new File(folderPath);
            if (!folder.exists()) {
                boolean success = folder.mkdirs();
            }
            FileWriter fw = new FileWriter(pathname);
            fw.write(html);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void render(SwaggerDiff diff, String pathname) {
        String html = new com.deepoove.swagger.diff.output.HtmlRender("Changelog",
                "http://deepoove.com/swagger-diff/stylesheets/demo.css")
                .render(diff);

        try {
            pathname = REPORT_FOLDER + "\\" + pathname.substring(0, pathname.lastIndexOf(".")) + ".html";
            String folderPath = pathname.substring(0, pathname.lastIndexOf("\\"));
            File folder = new File(folderPath);
            if (!folder.exists()) {
                boolean success = folder.mkdirs();
            }
            FileWriter fw = new FileWriter(pathname);
            fw.write(html);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteFolder(File file) {
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteFolder(f);
                    } else {
                        boolean success = f.delete();
                    }
                }
            }
            boolean success = file.delete();
        }
    }

    public static void main(String[] args) {
        new Detector();
    }
}
