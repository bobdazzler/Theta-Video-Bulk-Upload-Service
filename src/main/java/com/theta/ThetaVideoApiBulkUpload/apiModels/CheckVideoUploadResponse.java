package com.theta.ThetaVideoApiBulkUpload.apiModels;

import lombok.Data;

import java.util.List;

@Data
public class CheckVideoUploadResponse {
    private String status;
    private Body body;

    @Data
    public static class Body {
        private List<Video> videos;
    }
}
