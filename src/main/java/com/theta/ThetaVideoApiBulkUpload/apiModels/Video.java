package com.theta.ThetaVideoApiBulkUpload.apiModels;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Video {
    private String id;

    @JsonProperty("playback_uri")
    private String playbackUri;

    @JsonProperty("player_uri")
    private String playerUri;

    @JsonProperty("use_drm")
    private String useDrm;

    private String duration;

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
    private String resolution;
    private Object metadata;

    @JsonProperty("start_time_override")
    private String startTimeOverride;
}
