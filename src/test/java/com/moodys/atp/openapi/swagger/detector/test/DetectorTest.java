package com.moodys.atp.openapi.swagger.detector.test;

import com.deepoove.swagger.diff.SwaggerDiff;
import com.deepoove.swagger.diff.model.ChangedEndpoint;
import com.deepoove.swagger.diff.model.Endpoint;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class DetectorTest {

    final String SWAGGER_V2_DOC1 = "petstore_v2_1.json";
    final String SWAGGER_V2_DOC2 = "petstore_v2_2.json";

    @Test
    public void testEqual() {
        SwaggerDiff diff = SwaggerDiff.compareV2(SWAGGER_V2_DOC1, SWAGGER_V2_DOC2);
        List<Endpoint> newEndpoints = diff.getNewEndpoints();
        List<Endpoint> missingEndpoints = diff.getMissingEndpoints();
        List<ChangedEndpoint> changedEndPoints = diff.getChangedEndpoints();
        Assert.assertFalse(newEndpoints.isEmpty());
        Assert.assertFalse(missingEndpoints.isEmpty());
        Assert.assertFalse(changedEndPoints.isEmpty());
    }

}
