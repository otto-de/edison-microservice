package de.otto.edison.aws.s3;

import de.otto.edison.aws.configuration.AwsProperties;
import de.otto.edison.aws.configuration.AwsConfiguration;
import de.otto.edison.aws.s3.configuration.S3Properties;
import de.otto.edison.aws.s3.configuration.S3Configuration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;

import static java.util.Objects.requireNonNull;

@Component
@ConditionalOnProperty(name = "edison.aws.config.s3.enabled", havingValue = "true")
public class S3BucketPropertySourcePostProcessor implements BeanFactoryPostProcessor, EnvironmentAware {

    private static final String BUCKET_PROPERTY_SOURCE = "bucketPropertySource";
    private static final String EDISON_S3_PROPERTIES_BUCKETNAME = "edison.aws.config.s3.bucketname";
    private static final String EDISON_S3_PROPERTIES_FILENAME = "edison.aws.config.s3.filename";
    private final AwsProperties awsProperties;
    private S3Properties secretsProperties;

    public S3BucketPropertySourcePostProcessor(final AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
    }

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {

        final AwsConfiguration awsConfig = new AwsConfiguration();
        final S3Configuration s3Config = new S3Configuration();

        final S3Client s3Client = s3Config.s3Client(awsProperties, awsConfig.awsCredentialsProvider(awsProperties));

        final S3BucketPropertyReader s3BucketPropertyReader = new S3BucketPropertyReader(s3Client, secretsProperties);

        final ConfigurableEnvironment env = beanFactory.getBean(ConfigurableEnvironment.class);
        final MutablePropertySources propertySources = env.getPropertySources();
        propertySources.addLast(new PropertiesPropertySource(BUCKET_PROPERTY_SOURCE, s3BucketPropertyReader.getPropertiesFromS3()));
    }


    @Override
    public void setEnvironment(final Environment environment) {

        final String bucketName = requireNonNull(
                environment.getProperty(EDISON_S3_PROPERTIES_BUCKETNAME),
                "property '" + EDISON_S3_PROPERTIES_BUCKETNAME + "' must not be null");
        final String filename = requireNonNull(
                environment.getProperty(EDISON_S3_PROPERTIES_FILENAME),
                "property '" + EDISON_S3_PROPERTIES_FILENAME + "' must not be null");

        secretsProperties = new S3Properties();
        secretsProperties.setBucketname(bucketName);
        secretsProperties.setFilename(filename);
    }
}
