package serp.project.second_mile.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.second_mile.kafka.event.OrderSyncEvent;
import serp.project.second_mile.service.OrderSyncService;

@Service
@Slf4j
public class OrderSyncServiceImpl implements OrderSyncService {
    @Override
    public void syncOrder(OrderSyncEvent orderSyncEvent) {

    }
}
