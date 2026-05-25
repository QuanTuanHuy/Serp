/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.shared.dto.user.UserProfileDto;
import serp.project.pmcore.domain.shared.port.client.ICachePort;
import serp.project.pmcore.domain.shared.port.client.IUserProfileClient;
import serp.project.pmcore.domain.user.service.IUserService;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final IUserProfileClient userProfileClient;
    private final ICachePort cachePort;

    private static final String CACHE_NAMESPACE = "user_profile";
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);

    @Override
    public UserProfileDto getUserById(Long userId) {
        if (userId == null) {
            return null;
        }

        String cacheKey = String.valueOf(userId);
        try {
            Optional<UserProfileDto> cached = cachePort.get(CACHE_NAMESPACE, cacheKey, UserProfileDto.class);
            if (cached.isPresent()) {
                return cached.get();
            }
        } catch (Exception ex) {
            log.warn("[UserService] Failed to get user profile from cache: userId={}, error={}", userId, ex.getMessage());
        }

        UserProfileDto profile = userProfileClient.getUserProfileById(userId);
        if (profile != null) {
            try {
                cachePort.put(CACHE_NAMESPACE, cacheKey, profile, CACHE_TTL);
            } catch (Exception ex) {
                log.warn("[UserService] Failed to put user profile into cache: userId={}, error={}", userId, ex.getMessage());
            }
        }
        return profile;
    }

    @Override
    public List<UserProfileDto> getUserProfilesByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> distinctIds = userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (distinctIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, UserProfileDto> profilesMap = new HashMap<>();
        List<Long> missedIds = new ArrayList<>();

        for (Long id : distinctIds) {
            String cacheKey = String.valueOf(id);
            try {
                Optional<UserProfileDto> cached = cachePort.get(CACHE_NAMESPACE, cacheKey, UserProfileDto.class);
                if (cached.isPresent()) {
                    profilesMap.put(id, cached.get());
                } else {
                    missedIds.add(id);
                }
            } catch (Exception ex) {
                log.warn("[UserService] Failed to get user profile from cache: userId={}, error={}", id, ex.getMessage());
                missedIds.add(id);
            }
        }

        if (!missedIds.isEmpty()) {
            try {
                List<UserProfileDto> fetchedProfiles = userProfileClient.getUserProfilesByIds(missedIds);
                if (fetchedProfiles != null) {
                    for (UserProfileDto profile : fetchedProfiles) {
                        if (profile != null && profile.getId() != null) {
                            profilesMap.put(profile.getId(), profile);
                            try {
                                cachePort.put(CACHE_NAMESPACE, String.valueOf(profile.getId()), profile, CACHE_TTL);
                            } catch (Exception ex) {
                                log.warn("[UserService] Failed to put user profile to cache: userId={}, error={}", profile.getId(), ex.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                log.error("[UserService] Failed to fetch user profiles batch for ids: {}", missedIds, ex);
            }
        }

        return userIds.stream()
                .map(profilesMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
