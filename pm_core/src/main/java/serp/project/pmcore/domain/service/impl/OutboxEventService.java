/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import serp.project.pmcore.domain.entity.OutboxEventEntity;
import serp.project.pmcore.domain.port.store.IOutboxEventPort;
import serp.project.pmcore.domain.service.IOutboxEventService;

@Service
@RequiredArgsConstructor
public class OutboxEventService implements IOutboxEventService {
    private final IOutboxEventPort outboxEventPort;

    @Override
    public OutboxEventEntity saveEvent(OutboxEventEntity event) {
        return outboxEventPort.save(event);
    }
    
}
