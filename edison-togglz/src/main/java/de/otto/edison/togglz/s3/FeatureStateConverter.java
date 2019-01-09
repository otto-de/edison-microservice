package de.otto.edison.togglz.s3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.togglz.configuration.TogglzProperties;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.FeatureStateStorageWrapper;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

public class FeatureStateConverter {

    private static final String ERR_NO_SUCH_KEY = "NoSuchKey";

    private final S3Client s3Client;
    private final TogglzProperties togglzProperties;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public FeatureStateConverter(final S3Client s3Client,
                                 final TogglzProperties togglzProperties) {
        this.s3Client = s3Client;
        this.togglzProperties = togglzProperties;
    }

    public FeatureState retrieveFeatureStateFromS3(final Feature feature) {
        final GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(togglzProperties.getS3().getBucketName())
                .key(keyForFeature(feature))
                .build();
        try (final ResponseInputStream<GetObjectResponse> responseStream = s3Client.getObject(getRequest)) {
            if (responseStream != null) {
                final FeatureStateStorageWrapper wrapper = objectMapper.reader()
                        .forType(FeatureStateStorageWrapper.class)
                        .readValue(responseStream);
                return FeatureStateStorageWrapper.featureStateForWrapper(feature, wrapper);
            }
        } catch (final S3Exception ae) {
            if (ERR_NO_SUCH_KEY.equals(ae.awsErrorDetails().errorCode()) ||  ae.awsErrorDetails().sdkHttpResponse().statusCode() == 404) {
                return null;
            }
            throw ae;
        } catch (final IOException e) {
            throw new RuntimeException("Failed to get the feature state", e);
        }
        return null;
    }

    public void persistFeatureStateToS3(final FeatureState featureState) {
        try {
            final FeatureStateStorageWrapper storageWrapper = FeatureStateStorageWrapper.wrapperForFeatureState(featureState);
            final String json = objectMapper.writeValueAsString(storageWrapper);
            final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(togglzProperties.getS3().getBucketName())
                    .key(keyForFeature(featureState.getFeature()))
                    .serverSideEncryption(ServerSideEncryption.AES256)
                    .build();
            final RequestBody requestBody = RequestBody.fromString(json);
            s3Client.putObject(putObjectRequest, requestBody);
        } catch (final S3Exception | JsonProcessingException e) {
            throw new RuntimeException("Failed to set the feature state", e);
        }
    }

    private String keyForFeature(final Feature feature) {
        return togglzProperties.getS3().getKeyPrefix() + feature.name();
    }

}
