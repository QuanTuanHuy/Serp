/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.kafka.event.OrderSyncEvent;

public interface OrderInboundSyncService {

    /**
     * Applies main order status from second-mile when event originates there.
     */
    void applyInboundStatus(OrderSyncEvent event);
}
