package com.windcoder.coupon.customer.loadbalance;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.SelectedInstanceCallback;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.windcoder.coupon.customer.constant.Constant.TRAFFIC_VERSION;

@Slf4j
public class CanaryRule implements ReactorServiceInstanceLoadBalancer {

    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplier;
    private String serviceId;

    final AtomicInteger position;

    public CanaryRule(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplier, String serviceId) {
        this.serviceInstanceListSupplier = serviceInstanceListSupplier;
        this.serviceId = serviceId;
        this.position = new AtomicInteger(new Random().nextInt(1000));
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplier.getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next()
                .map(serviceInstances -> processInstanceResponse(supplier, serviceInstances,request));
    }

    private Response<ServiceInstance> processInstanceResponse(ServiceInstanceListSupplier supplier, List<ServiceInstance> serviceInstances, Request request) {
        Response<ServiceInstance> serviceInstanceResponse = getInstanceResponse(serviceInstances,request);

        if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
            ((SelectedInstanceCallback)supplier).selectedServiceInstance(serviceInstanceResponse.getServer());
        }
        return serviceInstanceResponse;

    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances, Request request) {
        // 注册中心无可用实例 抛出异常
        if (CollectionUtils.isEmpty(instances)) {
            log.warn("No instance available {}", serviceId);
            return new EmptyResponse();
        }

        // 从请求Header中获取特定的流量打标值
        // 注意：以下代码仅适用于WebClient调用，如果使用RestTemplate或者Feign则需要额外适配
        DefaultRequestContext context = (DefaultRequestContext) request.getContext();
        RequestData requestData = (RequestData) context.getClientRequest();
        HttpHeaders headers = requestData.getHeaders();

        String trafficVersion = headers.getFirst(TRAFFIC_VERSION);

        // 如果没有找到打标标记，或者标记为空，则使用RoundRobin规则进行轮训
        if (StringUtils.isBlank(trafficVersion)) {
            // 过滤掉所有金丝雀测试的节点（Metadaba有值的节点）
            List<ServiceInstance> noneCanaryInstances = instances.stream()
                    .filter(e -> !e.getMetadata().containsKey(TRAFFIC_VERSION))
                    .collect(Collectors.toList());
            return getRoundRobinInstance(noneCanaryInstances);
        }

        // 找出所有金丝雀服务器，用RoundRobin算法挑出一台
        List<ServiceInstance> canaryInstances = instances.stream().filter(e -> {
            String trafficVersionInMetadata = e.getMetadata().get(TRAFFIC_VERSION);
            return StringUtils.equalsIgnoreCase(trafficVersionInMetadata, trafficVersion);
        }).collect(Collectors.toList());
        // 使用轮训机制获取节点
        return getRoundRobinInstance(canaryInstances);
    }

    private Response<ServiceInstance> getRoundRobinInstance(List<ServiceInstance> instances) {
        // 如果没有可用节点，则返回空
        if (instances.isEmpty()) {
            log.warn("No servers available for service: " + serviceId);
            return new EmptyResponse();
        }

        int pos = Math.abs(this.position.incrementAndGet());
        ServiceInstance instance = instances.get(pos % instances.size());

        return new DefaultResponse(instance);
    }


}
