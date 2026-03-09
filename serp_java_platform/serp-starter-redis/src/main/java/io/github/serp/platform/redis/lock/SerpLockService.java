/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.redis.lock;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

public interface SerpLockService {
    boolean tryLock(String lockName, String ownerId, Duration ttl);

    boolean unlock(String lockName, String ownerId);

    <T> Optional<T> executeWithLock(String lockName, String ownerId, Duration ttl, Supplier<T> task);
}
