package de.otto.edison.aws.s3;

import de.otto.edison.aws.s3.configuration.S3Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Component
@EnableConfigurationProperties(S3Properties.class)
@ConditionalOnProperty(name = "edison.aws.config.s3.enabled", havingValue = "true")
public class S3BucketPropertyReader {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Autowired
    public S3BucketPropertyReader(final S3Client s3Client,
                                  final S3Properties s3Properties) {
        this.s3Client = s3Client;
        this.s3Properties = s3Properties;
    }

    Properties getPropertiesFromS3() {
        return s3Client.getObject(GetObjectRequest.builder()
                        .bucket(s3Properties.getBucketname())
                        .key(s3Properties.getFilename())
                        .build(),
                (response, in) -> makeProperties(in));
    }

    private Properties makeProperties(InputStream inputStream) {
        try {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
