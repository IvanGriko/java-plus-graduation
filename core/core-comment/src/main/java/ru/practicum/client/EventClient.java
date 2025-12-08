package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.event.EventAllApi;

@FeignClient(name = "core-event")
public interface EventClient extends EventAllApi {
}
