package com.theta.ThetaVideoApiBulkUpload.apiModels;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TranscodeVideoRequest {
    @JsonProperty("source_upload_id")
    private String sourceUploadId;
    @JsonProperty("playback_policy")
    private String playbackPolicy;
    @JsonProperty("source_uri")
    private String sourceUri;
    @JsonProperty("nft_collection")
    private String nftCollection;
}
