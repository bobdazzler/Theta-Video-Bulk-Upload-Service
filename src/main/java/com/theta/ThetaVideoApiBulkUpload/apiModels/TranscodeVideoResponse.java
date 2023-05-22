package com.theta.ThetaVideoApiBulkUpload.apiModels;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TranscodeVideoResponse {
    private String status;
    private Body body;

    @Data
    public static class Body {
        private List<Video> videos;

        @Data
        public static class Video {
            private String id;
            @JsonProperty("playback_uri")
            private String playbackUri;
            @JsonProperty("create_time")
            private String createTime;
            @JsonProperty("update_time")
            private String updateTime;
            @JsonProperty("service_account_id")
            private String serviceAccountId;
            @JsonProperty("file_name")
            private String fileName;
            private String state;
            @JsonProperty("sub_state")
            private String subState;
            @JsonProperty("source_upload_id")
            private String sourceUploadId;
            @JsonProperty("source_uri")
            private String sourceUri;
            @JsonProperty("playback_policy")
            private String playbackPolicy;
            private int progress;
            private String error;
            private String duration;
            private String resolution;
            private Map<String, Object> metadata;
        }
    }
}
