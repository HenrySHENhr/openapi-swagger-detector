package com.moodys.atp.openapi.swagger.detector.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
     * Update <code>allOf</code> to <code>oneOf</code> in OpenAPI json file
     * @param pathname json file path
     */
    public static void updateAllOfToOneOf(String pathname) throws IOException {
        File file = new File(pathname);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        CharArrayWriter tempStream = new CharArrayWriter();
        String line;
        while ((line = br.readLine()) != null) {
            line = line.replaceAll("\"allOf\"", "\"oneOf\"");
            tempStream.write(line);
            tempStream.append(System.getProperty("line.separator"));
        }
        br.close();
        FileWriter fw = new FileWriter(file);
        tempStream.writeTo(fw);
        fw.close();
    }
}
