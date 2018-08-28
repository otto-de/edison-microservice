package de.otto.edison.aws.s3;

import org.slf4j.Logger;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static software.amazon.awssdk.services.s3.model.Delete.builder;

public class S3Service {

    private static final Logger LOG = getLogger(S3Service.class);

    private final S3Client s3Client;

    public S3Service(final S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void createBucket(final String bucketName) {
        if (!listBucketNames().contains(bucketName)) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        }
    }

    public List<String> listBucketNames() {
        return s3Client
                .listBuckets()
                .buckets()
                .stream()
                .map(Bucket::name)
                .collect(toList());
    }

    public void upload(final String bucketName,
                       final File file) {
        final PutObjectResponse putObjectResponse = s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(file.getName())
                        .build(),
                file.toPath());
        LOG.debug("upload {} to bucket {}: ", file.getName(), bucketName, putObjectResponse.toString());
    }

    public boolean download(final String bucketName,
                            final String fileName,
                            final Path destination) {
        try {
            if (Files.exists(destination)) {
                Files.delete(destination);
            }
        } catch (final IOException e) {
            LOG.error("could not delete temp snapshotfile {}", destination.toString(), e);
            return false;
        }
        final GetObjectRequest request = GetObjectRequest.builder().bucket(bucketName).key(fileName).build();
        final GetObjectResponse getObjectResponse = s3Client.getObject(request, destination);
        LOG.debug("download {} from bucket {}: ", fileName, bucketName, getObjectResponse.toString());
        return true;
    }

    public void deleteAllObjectsInBucket(final String bucketName) {
        LOG.debug("deleting all objects in bucket {}", bucketName);
        deleteAllObjectsWithPrefixInBucket(bucketName, "");
        LOG.debug("files in bucket: {}", listAllFiles(bucketName));
    }

    public void deleteAllObjectsWithPrefixInBucket(final String bucketName,
                                                   final String prefix) {
        final ListObjectsV2Response listObjectResponse = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build());
        if (listObjectResponse.keyCount() > 0) {
            DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(builder()
                            .objects(convertS3ObjectsToObjectIdentifiers(listObjectResponse, prefix))
                            .build())
                    .build();
            final DeleteObjectsResponse deleteObjectsResponse = s3Client.deleteObjects(deleteObjectsRequest);
            LOG.debug("deleteAllObjectsWithPrefixInBucket in bucket {} with prefix {}: {}", bucketName, prefix, deleteObjectsResponse);
        } else {
            LOG.debug("deleteAllObjectsWithPrefixInBucket listObjects found no keys in bucket {} with prefix {}: {}", bucketName, prefix, listObjectResponse);
        }
    }

    public List<String> listAllFiles(final String bucketName) {
        return listAll(bucketName)
                .stream()
                .map(S3Object::key)
                .collect(toList());
    }

    public List<S3Object> listAll(final String bucketName) {
        final ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build());

        if (listObjectsV2Response.keyCount() > 0) {
            return listObjectsV2Response.contents();
        } else {
            return Collections.emptyList();
        }

    }

    private List<ObjectIdentifier> convertS3ObjectsToObjectIdentifiers(final ListObjectsV2Response listObjectsV2Response,
                                                                       final String prefix) {
        return listObjectsV2Response.contents()
                .stream()
                .filter(o -> o.key() != null && o.key().startsWith(prefix))
                .map(o -> ObjectIdentifier.builder().key(o.key()).build()).collect(toList());
    }
}
