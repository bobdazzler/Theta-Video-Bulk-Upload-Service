package com.theta.ThetaVideoApiBulkUpload.apiModels;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ProcessedVideoResponse {
    private String status;
    private String message;
    @JsonProperty("data")
    private List<Video> videos;
}
