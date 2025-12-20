package ru.practicum.ewm.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.NameResolverRegistry;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.devh.boot.grpc.client.nameresolver.DiscoveryClientResolverFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.grpc.collector.RecommendationsControllerGrpc;
import ru.practicum.grpc.collector.UserActionControllerGrpc;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GrpcConfiguration {

    String collectorDiscoveryName;
    String analyzerDiscoveryName;
    DiscoveryClient discoveryClient;

    public GrpcConfiguration(
            @Value("${explore-with-me.collector.discovery.name:collector}") String collectorDiscoveryName,
            @Value("${explore-with-me.analyzer.discovery.name:analyzer}") String analyzerDiscoveryName,
            DiscoveryClient discoveryClient
    ) {
        this.collectorDiscoveryName = collectorDiscoveryName;
        this.analyzerDiscoveryName = analyzerDiscoveryName;
        this.discoveryClient = discoveryClient;
    }

    @PostConstruct
    public void init() {
        DiscoveryClientResolverFactory resolverFactory = new DiscoveryClientResolverFactory(discoveryClient);
        NameResolverRegistry.getDefaultRegistry().register(resolverFactory);
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel collectorChannel() {
        return ManagedChannelBuilder.forTarget("discovery:///" + collectorDiscoveryName)
                .defaultLoadBalancingPolicy("round_robin")
                .usePlaintext()
                .enableRetry()
                .keepAliveWithoutCalls(true)
                .build();
    }

    @Bean
    public UserActionControllerGrpc.UserActionControllerBlockingStub userActionControllerBlockingStub(
            @Qualifier("collectorChannel") ManagedChannel channel
    ) {
        return UserActionControllerGrpc.newBlockingStub(channel);
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel analyzerChannel() {
        return ManagedChannelBuilder.forTarget("discovery:///" + analyzerDiscoveryName)
                .defaultLoadBalancingPolicy("round_robin")
                .usePlaintext()
                .enableRetry()
                .keepAliveWithoutCalls(true)
                .build();
    }

    @Bean
    public RecommendationsControllerGrpc.RecommendationsControllerBlockingStub recommendationsControllerBlockingStub(
            @Qualifier("analyzerChannel") ManagedChannel channel
    ) {
        return RecommendationsControllerGrpc.newBlockingStub(channel);
    }
}