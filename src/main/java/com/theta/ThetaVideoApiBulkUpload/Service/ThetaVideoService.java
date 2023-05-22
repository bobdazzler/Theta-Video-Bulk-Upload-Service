package com.theta.ThetaVideoApiBulkUpload.Service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.theta.ThetaVideoApiBulkUpload.apiModels.*;
import com.theta.ThetaVideoApiBulkUpload.thetaRestClient.ThetaRestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class ThetaVideoService {
    // Number of threads to execute concurrently
    //Also, theta video transcode service has a limit of 3 video transcoding at a time
    // per theta apiKey and theta api-secret
    private final static int Number_OF_Threads = 3;

    @Async
    public void processBulkUploadAsync(List<byte[]> files, String thetaApiKey,
                                        String thetaApiSecret, String webhookUrl) throws ExecutionException, InterruptedException {
        var checkVideoUploadResponses = processBulkUpload(files,thetaApiKey,thetaApiSecret);
        var processedVideoResponse = compileProcessedVideoResponse(checkVideoUploadResponses);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ThetaRestClient.sendPostRequest(webhookUrl, headers,processedVideoResponse,Void.class);
    }

    public ProcessedVideoResponse compileProcessedVideoResponse(List<CheckVideoUploadResponse> checkVideoUploadResponses){
        var processedVideoResponse = new ProcessedVideoResponse();
        processedVideoResponse.setStatus("success");
        processedVideoResponse.setMessage("Operation Successful");
        List<Video> videos = checkVideoUploadResponses.stream()
                .map(item -> item.getBody().getVideos().get(0))
                .collect(Collectors.toList());
        processedVideoResponse.setVideos(videos);
        return processedVideoResponse;
    }

    public List<CheckVideoUploadResponse> processBulkUpload(List<byte[]> files, String thetaApiKey,
                                                            String thetaApiSecret) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(Number_OF_Threads);
        List<CompletableFuture<CheckVideoUploadResponse>> completableFutures = new ArrayList<>();

        // Submit tasks to the executor service and create CompletableFuture for each task
        for (byte[] file : files) {
            CompletableFuture<CheckVideoUploadResponse> completableFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return combinedVideoUploadProcess(file,thetaApiKey,thetaApiSecret);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }, executorService);
            completableFutures.add(completableFuture);
        }

        // Create a CompletableFuture for all tasks and combine the results
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                completableFutures.toArray(new CompletableFuture[0])
        );

        // Handle completion of all tasks
        CompletableFuture<List<CheckVideoUploadResponse>> resultListFuture = allFutures.thenApply(v -> {
            return completableFutures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
        });

        // Process the resultList as needed
        resultListFuture.thenAccept(resultList -> {
            for (CheckVideoUploadResponse result : resultList) {
                System.out.println("Result: " + result.toString());
            }
        });

        // Shutdown the executor service
        executorService.shutdown();

        return resultListFuture.get();
    }


    public CheckVideoUploadResponse combinedVideoUploadProcess(byte[] file, String thetaApiKey, String thetaApiSecret) throws UnirestException, IOException, InterruptedException {
        var createPreSignedUrlResponse = createPreSignedUrlResponse(thetaApiKey, thetaApiSecret);
        String presignedUrl = createPreSignedUrlResponse.getBody().getUploads().get(0).getPresignedUrl();
        String sourceVideoId = createPreSignedUrlResponse.getBody().getUploads().get(0).getId();
        uploadFileToPreSignedUrl(file,presignedUrl);
        var  transcodeVideoResponse = transcodeVideoUsingUpload(thetaApiKey, thetaApiSecret, sourceVideoId);
        var transcodeVideoId = transcodeVideoResponse.getBody().getVideos().get(0).getId();
        return checkVideoUpload(thetaApiKey, thetaApiSecret, transcodeVideoId);
    }

    public CreatePreSignedUrlResponse createPreSignedUrlResponse(String apiKey, String apiSecret){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-tva-sa-id", apiKey);
        headers.set("x-tva-sa-secret", apiSecret);
        return ThetaRestClient.sendPostRequest("https://api.thetavideoapi.com/upload",
                headers,null,CreatePreSignedUrlResponse.class);
    }

    public void uploadFileToPreSignedUrl(byte[] file, String preSignedUrl) throws IOException, UnirestException {
        LocalDateTime startTime = LocalDateTime.now();
        System.out.println("Upload File To PreSignedUrl Start Time: " + formatDateTime(startTime));
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.put(preSignedUrl)
                .header("Content-Type", "application/octet-stream")
                .body(file)
                .asString();
        response.getBody();
        LocalDateTime endTime = LocalDateTime.now();
        System.out.println("Upload File To PreSignedUrl End Time: " + formatDateTime(endTime));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    public TranscodeVideoResponse transcodeVideoUsingUpload(String apiKey, String apiSecret, String sourceUploadId){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-tva-sa-id", apiKey);
        headers.set("x-tva-sa-secret", apiSecret);
        TranscodeVideoRequest request = new TranscodeVideoRequest();
        request.setSourceUploadId(sourceUploadId);
        request.setPlaybackPolicy("public");
        return ThetaRestClient.sendPostRequest("https://api.thetavideoapi.com/video",
                headers,request,TranscodeVideoResponse.class);
    }

    public CheckVideoUploadResponse checkVideoUpload(String apiKey, String apiSecret, String transcodeVideoId) throws InterruptedException {
        CheckVideoUploadResponse checkVideoUploadResponse = new CheckVideoUploadResponse();
        while (true) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-tva-sa-id", apiKey);
            headers.set("x-tva-sa-secret", apiSecret);
            String url = "https://api.thetavideoapi.com/video/".concat(transcodeVideoId);

            checkVideoUploadResponse = ThetaRestClient.sendGetRequest(url, headers, CheckVideoUploadResponse.class);
            int progress =checkVideoUploadResponse.getBody().getVideos().get(0).getProgress();
            String state = checkVideoUploadResponse.getBody().getVideos().get(0).getState();

            if(progress == 100 && "success".equalsIgnoreCase(state))
            {
                break;
            }
            //sleep for 10seconds and check progress again
            Thread.sleep(10000);
        }
        return checkVideoUploadResponse;
    }
}
