package com.theta.ThetaVideoApiBulkUpload.apiModels;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
@Data
public class CreatePreSignedUrlResponse {
    private String status;
    private Body body;

    @Data
    public static class Body {
        private List<Upload> uploads;
    }

    @Data
    public static class Upload {
        private String id;

        @JsonProperty("service_account_id")
        private String serviceAccountId;

        @JsonProperty("presigned_url")
        private String presignedUrl;

        @JsonProperty("presigned_url_expiration")
        private String presignedUrlExpiration;

        @JsonProperty("presigned_url_expired")
        private boolean presignedUrlExpired;

        @JsonProperty("create_time")
        private String createTime;

        @JsonProperty("update_time")
        private String updateTime;
    }
}
