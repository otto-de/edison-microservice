package de.otto.edison.aws.s3.togglz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.aws.s3.configuration.S3TogglzProperties;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.util.FeatureStateStorageWrapper;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;

import java.io.IOException;

/*
    Took this from here:
    https://github.com/togglz/togglz/blob/master/amazon-s3/src/main/java/org/togglz/s3/S3StateRepository.java

    But adapted it to the new AWS SDK (2.0.0)
    -- sw 27.09.2017
*/
public class S3StateRepository implements StateRepository {

    private static final String ERR_NO_SUCH_KEY = "NoSuchKey";
    private final S3TogglzProperties s3TogglzProperties;
    private final S3Client s3Client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public S3StateRepository(final S3TogglzProperties s3TogglzProperties, final S3Client s3Client) {
        this.s3TogglzProperties = s3TogglzProperties;
        this.s3Client = s3Client;
    }

    @Override
    public FeatureState getFeatureState(final Feature feature) {
        final GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(s3TogglzProperties.getBucketName())
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
            if (ERR_NO_SUCH_KEY.equals(ae.awsErrorDetails().errorCode())) {
                return null;
            }
            throw ae;
        } catch (final IOException e) {
            throw new RuntimeException("Failed to get the feature state", e);
        }
        return null;
    }

    @Override
    public void setFeatureState(final FeatureState featureState) {
        try {
            final FeatureStateStorageWrapper storageWrapper = FeatureStateStorageWrapper.wrapperForFeatureState(featureState);
            final String json = objectMapper.writeValueAsString(storageWrapper);
            final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3TogglzProperties.getBucketName())
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
        return s3TogglzProperties.getKeyPrefix() + feature.name();
    }
}
