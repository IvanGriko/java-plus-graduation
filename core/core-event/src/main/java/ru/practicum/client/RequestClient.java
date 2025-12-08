package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.request.RequestApi;

@FeignClient(name = "core-request")
public interface RequestClient extends RequestApi {
}
