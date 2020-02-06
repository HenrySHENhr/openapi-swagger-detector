package com.moodys.atp.openapi.swagger.detector.util;

import com.deepoove.swagger.diff.SwaggerDiff;
import com.qdesrame.openapi.diff.model.ChangedOpenApi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Report {

    private static final String REPORT_FOLDER = "report";

    public static void render(ChangedOpenApi diff, String pathname) {
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

    public static void render(SwaggerDiff diff, String pathname) {
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

    public static void deleteFolder(File file) {
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
}
