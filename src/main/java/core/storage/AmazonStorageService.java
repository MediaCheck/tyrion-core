package core.storage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.net.URI;
import java.util.concurrent.CompletionStage;

@Singleton
public class AmazonStorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(AmazonStorageService.class);

    private final String bucketName;

    private final S3AsyncClient client;
    private final URI endpoint;
    private final Region region;

    @Inject
    public AmazonStorageService(Config config) {

        this.region = Region.of("ams3");
        this.endpoint = URI.create(config.getString("digitalOcean.space.url"));
        this.bucketName = config.getString("digitalOcean.space.bucket_name");

        this.client = S3AsyncClient
                .builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(config.getString("digitalOcean.space.access_key"), config.getString("digitalOcean.space.secret_key"))))
                .endpointOverride(this.endpoint)
                .region(this.region)
                .build();

        HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(this.bucketName)
                .build();

        this.client.headBucket(headBucketRequest)
                .whenComplete((response, exception) -> {
                    if (exception instanceof NoSuchBucketException) {
                        CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                                .bucket(this.bucketName)
                                .build();

                        this.client.createBucket(createBucketRequest)
                                .whenComplete((createBucketResponse, throwable) -> {
                                    if (throwable != null) {
                                        logger.error("<init>", throwable);
                                    } else {
                                        logger.info("<init> - bucket: {} successfully created", this.bucketName);
                                    }
                                });
                    } else if (exception != null) {
                        logger.error("<init>", exception);
                    } else {
                        logger.info("<init> - bucket: {} already exist", this.bucketName);
                    }
                });
    }

    private CompletionStage<? extends StorageItem> store(AsyncRequestBody body, String contentType, Long size, String name, String path) {

        final String key = path + "/" + name;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(this.bucketName)
                .key(key)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .contentType(contentType)
                .contentLength(size)
                .build();

        return this.client.putObject(request, body)
                .thenApply(response -> {

                    GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                            .endpoint(this.endpoint)
                            .region(this.region)
                            .bucket(this.bucketName)
                            .key(key)
                            .build();

                    String link = this.client.utilities().getUrl(getUrlRequest).toString();

                    return new StoredItem(name, key, link);
                })
                .whenComplete((storedItem, throwable) -> {
                    if (throwable != null) {
                        logger.error("store", throwable);
                    } else {
                        logger.trace("store - successfully stored item: {}", storedItem.getPath());
                    }
                });
    }

    @Override
    public CompletionStage<? extends StorageItem> store(File file, String contentType, String name, String path) {
        return this.store(AsyncRequestBody.fromFile(file), contentType, file.length(), name, path);
    }

    @Override
    public CompletionStage<? extends StorageItem> store(String file, String contentType, String name, String path) {
        return this.store(AsyncRequestBody.fromString(file), contentType, (long)file.length(), name, path);
    }

    @Override
    public CompletionStage<? extends StorageItem> store(byte[] file, String contentType, String name, String path) {
        return this.store(AsyncRequestBody.fromBytes(file), contentType, (long)file.length, name, path);
    }

    @Override
    public CompletionStage<? extends StorageItem> duplicate(StorageItem item, String path) {

        final String key = path.concat("/").concat(item.getName());

        CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                .bucket(this.bucketName)
                .copySource(this.bucketName + "/" + item.getPath())
                .key(key)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        return this.client.copyObject(copyObjectRequest)
                .thenApply(response -> {
                    GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                            .endpoint(this.endpoint)
                            .region(this.region)
                            .bucket(this.bucketName)
                            .key(key)
                            .build();

                    String link = this.client.utilities().getUrl(getUrlRequest).toString();

                    return new StoredItem(item.getName(), key, link);
                });
    }

    public CompletionStage<RetrievedItem> retrieve(StorageItem storageItem) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(this.bucketName)
                .key(storageItem.getPath())
                .build();

        return this.client.getObject(getObjectRequest, AsyncResponseTransformer.toBytes()).thenApply(RetrievedItem::new);
    }

    @Override
    public CompletionStage<Void> remove(StorageItem item) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(this.bucketName)
                .key(item.getPath())
                .build();

        return this.client.deleteObject(deleteObjectRequest).thenAccept(notUsed -> {});
    }
}
