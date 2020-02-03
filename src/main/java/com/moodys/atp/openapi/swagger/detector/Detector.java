package com.moodys.atp.openapi.swagger.detector;

import com.deepoove.swagger.diff.SwaggerDiff;
import com.deepoove.swagger.diff.model.ChangedEndpoint;
import com.deepoove.swagger.diff.model.Endpoint;
import com.deepoove.swagger.diff.output.HtmlRender;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Detector {

    static final String SWAGGER_V2_DOC1 = "src/main/resources/petstore_v2_1.json";
    static final String SWAGGER_V2_DOC2 = "src/main/resources/petstore_v2_2.json";

    public Detector() {
        SwaggerDiff diff = SwaggerDiff.compareV2(SWAGGER_V2_DOC1, SWAGGER_V2_DOC2);
        List<Endpoint> newEndpoints = diff.getNewEndpoints();
        List<Endpoint> missingEndpoints = diff.getMissingEndpoints();
        List<ChangedEndpoint> changedEndPoints = diff.getChangedEndpoints();
        this.render(diff);
    }

    public void render(SwaggerDiff diff) {
        String html = new HtmlRender("Changelog",
                "http://deepoove.com/swagger-diff/stylesheets/demo.css")
                .render(diff);

        try {
            FileWriter fw = new FileWriter(
                    "testNewApi.html");
            fw.write(html);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Detector();
    }
}
