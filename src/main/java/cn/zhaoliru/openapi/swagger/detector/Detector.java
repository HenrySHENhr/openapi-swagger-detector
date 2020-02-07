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

public class Detector {

    private static final String BENCHMARK_FOLDER = ResourceBundle.getBundle("config").getString("BENCHMARK_FOLDER");
    private static final String CURRENT_FOLDER = ResourceBundle.getBundle("config").getString("CURRENT_FOLDER");
    private static final String REPORT_FOLDER = ResourceBundle.getBundle("config").getString("REPORT_FOLDER");

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
            } catch (IllegalArgumentException e) {
                System.out.println(e.toString());
                try {
                    List<String> benchmarkList = FileDetector.splitPathsToFiles(benchmark);
                    List<String> currentList = FileDetector.splitPathsToFiles(current);
                    System.out.println("Split json file");
                    if (benchmarkList != null && !benchmarkList.isEmpty()
                            && currentList != null && !currentList.isEmpty()) {
                        // TODO Calculate path difference set between two folders

                        benchmarkList.forEach(path -> {
                            ChangedOpenApi diff = OpenApiCompare.fromLocations(path,
                                    CURRENT_FOLDER + "\\" + path.substring(BENCHMARK_FOLDER.length() + 1));
                            if (diff.isDiff()) {
                                Report.render(diff, path.substring(BENCHMARK_FOLDER.length() + 1));
                            }
                            //noinspection ResultOfMethodCallIgnored
                            new File(path).delete();
                        });
                        currentList.forEach(path -> {
                            //noinspection ResultOfMethodCallIgnored
                            new File(path).delete();
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
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
