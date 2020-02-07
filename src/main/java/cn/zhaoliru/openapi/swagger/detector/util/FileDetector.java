package cn.zhaoliru.openapi.swagger.detector.util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileDetector {

    /**
     * Return specified type files
     * @param pathname Folder or file path
     * @param type Specified file type
     * @return File list
     */
    public static List<String> searchFiles(String pathname, final String type) {
        List<String> result = new ArrayList<>();
        File file = new File(pathname);

        if (file.isFile()) {
            result.add(pathname);
        }

        File[] subFolders = file.listFiles(pathname1 -> {
            if (pathname1.isDirectory()) {
                return true;
            } else {
                return pathname1.getName().substring(pathname1.getName().lastIndexOf(".") + 1).equals(type);
            }
        });

        if (subFolders != null) {
            for (File f : subFolders) {
                if (f.isFile()) {
                    result.add(f.getPath());
                } else {
                    result.addAll(searchFiles(f.getPath(), type));
                }
            }
        }

        return result;
    }

    /**
     * Return files
     * @param pathname Folder or file path
     * @return File list
     */
    public static List<String> searchFiles(String pathname) {
        List<String> result = new ArrayList<>();
        File file = new File(pathname);

        if (file.isFile()) {
            result.add(pathname);
        }

        File[] subFolders = file.listFiles();

        if (subFolders != null) {
            for (File f : subFolders) {
                if (f.isFile()) {
                    result.add(f.getPath());
                } else {
                    result.addAll(searchFiles(f.getPath()));
                }
            }
        }

        return result;
    }

    /**
     * Calculate file difference set between two folders
     * @param left List of file path
     * @param right List of file path
     * @return List of difference set files
     */
    public static Map<String, List<String>> folderDetector(List<String> left, List<String> right) {
        Map<String, List<String>> result = new HashMap<>();
        // Remove first lever folder
        List<String> leftFileNames = new ArrayList<>(left);
        leftFileNames = leftFileNames.stream().map(item -> item.substring(item.indexOf('\\') + 1))
                .collect(Collectors.toList());
        List<String> rightFileNames = new ArrayList<>(right);
        rightFileNames = rightFileNames.stream().map(item -> item.substring(item.indexOf('\\') + 1))
                .collect(Collectors.toList());
        List<String> leftDifferenceSet = new ArrayList<>(leftFileNames);
        leftDifferenceSet.removeAll(rightFileNames);
        List<String> rightDifferenceSet = new ArrayList<>(rightFileNames);
        rightDifferenceSet.removeAll(leftFileNames);
        if (!leftDifferenceSet.isEmpty()) {
            leftDifferenceSet = leftDifferenceSet.stream()
                    .map(item -> left.get(0).substring(0, left.get(0).indexOf("\\") + 1) + item)
                    .collect(Collectors.toList());
            result.put("left", leftDifferenceSet);
        }
        if (!rightDifferenceSet.isEmpty()) {
            rightDifferenceSet = rightDifferenceSet.stream()
                    .map(item -> right.get(0).substring(0, right.get(0).indexOf("\\") + 1) + item)
                    .collect(Collectors.toList());
            result.put("right", rightDifferenceSet);
        }
        return result.isEmpty() ? null : result;
    }

    /**
     * Update <code>allOf</code> or <code>oneOf</code> to <code>anyOf</code> in OpenAPI json file
     * @param pathname json file path
     */
    public static void updateToAnyOf(String pathname) throws IOException {
        File file = new File(pathname);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        CharArrayWriter tempStream = new CharArrayWriter();
        String line;
        while ((line = br.readLine()) != null) {
            line = line.replaceAll("\"allOf\"", "\"anyOf\"")
                    .replaceAll("\"oneOf\"", "\"anyOf\"");
            tempStream.write(line);
            tempStream.append(System.getProperty("line.separator"));
        }
        br.close();
        FileWriter fw = new FileWriter(file);
        tempStream.writeTo(fw);
        fw.close();
    }
}
