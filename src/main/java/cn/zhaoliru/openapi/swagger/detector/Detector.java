package cn.zhaoliru.openapi.swagger.detector;


import cn.zhaoliru.openapi.swagger.detector.util.FileDetector;
import cn.zhaoliru.openapi.swagger.detector.util.Report;
import com.deepoove.swagger.diff.SwaggerDiff;
import com.qdesrame.openapi.diff.OpenApiCompare;
import com.qdesrame.openapi.diff.model.ChangedOpenApi;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;


public class Detector {

    private static final String BENCHMARK_FOLDER = ResourceBundle.getBundle("config").getString("BENCHMARK_FOLDER");
    private static final String CURRENT_FOLDER = ResourceBundle.getBundle("config").getString("CURRENT_FOLDER");
    private static final String REPORT_FOLDER = ResourceBundle.getBundle("config").getString("REPORT_FOLDER");


    private Detector() {
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

        // Calculate file difference set between two folders
        Map<String, List<String>> differenceSet = FileDetector.folderDetector(benchmarkFiles, currentFiles);
        if (differenceSet != null && !differenceSet.isEmpty()) {
            if (differenceSet.containsKey("left")) {
                System.out.println("benchmark difference set: ");
                differenceSet.get("left").forEach(item -> System.out.print(item + " "));
                System.out.println();
                benchmarkFiles.removeAll(differenceSet.get("left"));
            }
            if (differenceSet.containsKey("right")) {
                System.out.println("current difference set: ");
                differenceSet.get("right").forEach(item -> System.out.print(item + " "));
                System.out.println();
                currentFiles.removeAll(differenceSet.get("right"));
            }

            System.out.println("benchmark: ");
            benchmarkFiles.forEach(item -> System.out.print(item + " "));
            System.out.println();
            System.out.println("current: ");
            currentFiles.forEach(item -> System.out.print(item + " "));
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
                    Map<String, String> benchmarkList = FileDetector.splitPathsToMap(benchmark);
                    Map<String, String> currentList = FileDetector.splitPathsToMap(current);

                    if (benchmarkList != null && !benchmarkList.isEmpty()
                            && currentList != null && !currentList.isEmpty()) {
                        if (!benchmarkList.keySet().equals(currentList.keySet())) {
                            // Get JSON object without paths node
                            JSONObject sourceWithoutPath = new JSONObject(obj);
                            sourceWithoutPath.remove("paths");
                            String sourceWithoutPathString = sourceWithoutPath.toString();
                            // Add JSON string without paths node to missing keys
                            Set<String> benchmarkDiffSet = benchmarkList.keySet();
                            Set<String> currentDiffSet = currentList.keySet();
                            benchmarkDiffSet.removeAll(currentList.keySet());
                            currentDiffSet.removeAll(benchmarkList.keySet());
                            if (!benchmarkDiffSet.isEmpty()) {
                                for (String diffNode : benchmarkDiffSet) {
                                    currentList.put(diffNode, sourceWithoutPathString);
                                }
                            }
                            if (!currentDiffSet.isEmpty()) {
                                for (String diffNode : currentDiffSet) {
                                    benchmarkList.put(diffNode, sourceWithoutPathString);
                                }
                            }
                        }
                        benchmarkList.forEach((key, value) -> {
                            ChangedOpenApi diff = OpenApiCompare.fromContents(value, currentList.get(key));
                            if (diff.isDiff()) {
                                Report.render(diff, benchmark.substring(BENCHMARK_FOLDER.length() + 1));
                            }
                        });
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
