/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import org.springframework.stereotype.Service;
import serp.project.crm.core.domain.entity.AccountEntity;
import serp.project.crm.core.domain.entity.MeetingRequestEntity;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.enums.AccountTier;
import serp.project.crm.core.domain.enums.ExperienceLevel;
import serp.project.crm.core.domain.enums.MeetingRequestType;

import java.util.List;
import java.util.Locale;

@Service
public class RepCompatibilityMatcher {

    // Scoring weights for rep-account compatibility
    private static final int SCORE_PREFERRED_REP = 40;        // Explicit customer request for specific rep
    private static final int SCORE_LANGUAGE_MATCH = 15;       // Critical for effective communication
    private static final int SCORE_SKILL_MATCH = 12;          // Rep has relevant skills for meeting type
    private static final int SCORE_EXPERIENCE_BASE = 6;       // Base score for meeting experience requirements
    private static final int SCORE_EXPERIENCE_BONUS = 3;      // Bonus per experience level above requirement
    private static final int SCORE_TIER_PLATINUM = 8;         // Platinum account with senior+ rep
    private static final int SCORE_TIER_GOLD = 5;             // Gold account with mid+ rep
    private static final int PENALTY_EXPERIENCE_MISMATCH = -10; // Rep below required experience level

    public int calculate(MeetingRequestEntity request, AccountEntity account, TeamMemberEntity member, int upcomingLoad) {
        int score = 0;

        if (request.getPreferredUserId() != null && request.getPreferredUserId().equals(member.getUserId())) {
            score += SCORE_PREFERRED_REP;
        }

        score += languageScore(account, member);
        score += experienceScore(request.getMeetingType(), member.getExperienceLevel());
        score += skillScore(request.getMeetingType(), member.getSkills());
        score += tierBonus(account, member.getExperienceLevel());
        score += capacityScore(member.getCapacity(), upcomingLoad);

        return score;
    }

    public boolean canTakeMoreMeetings(TeamMemberEntity member, int meetingsInDay) {
        return member.getMaxMeetings() == null || meetingsInDay < member.getMaxMeetings();
    }

    private int languageScore(AccountEntity account, TeamMemberEntity member) {
        String accountLanguage = normalize(account != null ? account.getLanguage() : null);
        if (accountLanguage == null || member.getLanguages() == null || member.getLanguages().isEmpty()) {
            return 0;
        }

        boolean matches = member.getLanguages().stream()
                .map(this::normalize)
                .anyMatch(accountLanguage::equals);
        return matches ? SCORE_LANGUAGE_MATCH : 0;
    }

    private int experienceScore(MeetingRequestType meetingType, ExperienceLevel experienceLevel) {
        if (meetingType == null || experienceLevel == null) {
            return 0;
        }

        int rank = experienceRank(experienceLevel);
        int requiredRank = switch (meetingType) {
            case DISCOVERY -> 1;
            case DEMO, PROPOSAL -> 2;
            case NEGOTIATION, QBR -> 3;
        };

        if (rank < requiredRank) {
            return PENALTY_EXPERIENCE_MISMATCH;
        }
        return SCORE_EXPERIENCE_BASE + ((rank - requiredRank) * SCORE_EXPERIENCE_BONUS);
    }

    private int skillScore(MeetingRequestType meetingType, List<String> skills) {
        if (meetingType == null || skills == null || skills.isEmpty()) {
            return 0;
        }

        List<String> normalizedSkills = skills.stream()
                .map(this::normalize)
                .filter(value -> value != null && !value.isBlank())
                .toList();

        boolean matches = expectedSkills(meetingType).stream().anyMatch(normalizedSkills::contains);
        return matches ? SCORE_SKILL_MATCH : 0;
    }

    private int tierBonus(AccountEntity account, ExperienceLevel experienceLevel) {
        if (account == null || account.getTier() == null || experienceLevel == null) {
            return 0;
        }

        if (AccountTier.PLATINUM.equals(account.getTier()) && experienceRank(experienceLevel) >= 3) {
            return SCORE_TIER_PLATINUM;
        }
        if (AccountTier.GOLD.equals(account.getTier()) && experienceRank(experienceLevel) >= 2) {
            return SCORE_TIER_GOLD;
        }
        return 0;
    }

    private int capacityScore(Integer capacity, int upcomingLoad) {
        int effectiveCapacity = capacity != null ? capacity : 100;
        int remainingCapacity = Math.max(0, effectiveCapacity - (upcomingLoad * 10));
        return remainingCapacity / 10;
    }

    private int experienceRank(ExperienceLevel experienceLevel) {
        return switch (experienceLevel) {
            case JUNIOR -> 1;
            case MID -> 2;
            case SENIOR -> 3;
            case EXPERT -> 4;
        };
    }

    private List<String> expectedSkills(MeetingRequestType meetingType) {
        return switch (meetingType) {
            case DISCOVERY -> List.of("discovery", "qualification");
            case DEMO -> List.of("demo", "product_demo", "presentation");
            case PROPOSAL -> List.of("proposal", "solutioning", "consulting");
            case NEGOTIATION -> List.of("negotiation", "closing", "enterprise_sales");
            case QBR -> List.of("account_management", "qbr", "customer_success");
        };
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ENGLISH);
    }
}
