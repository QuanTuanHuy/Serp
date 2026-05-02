package serp.project.crm.core.service.impl;

import org.junit.jupiter.api.Test;
import serp.project.crm.core.domain.entity.AccountEntity;
import serp.project.crm.core.domain.entity.MeetingRequestEntity;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.enums.AccountTier;
import serp.project.crm.core.domain.enums.ExperienceLevel;
import serp.project.crm.core.domain.enums.MeetingRequestType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RepCompatibilityMatcherTest {

    private final RepCompatibilityMatcher matcher = new RepCompatibilityMatcher();

    @Test
    void preferredRepWithLanguageAndSkillMatch_scoresHigher() {
        MeetingRequestEntity request = MeetingRequestEntity.builder()
                .preferredUserId(100L)
                .meetingType(MeetingRequestType.NEGOTIATION)
                .build();

        AccountEntity account = AccountEntity.builder()
                .tier(AccountTier.PLATINUM)
                .language("en")
                .build();

        TeamMemberEntity strongMatch = TeamMemberEntity.builder()
                .userId(100L)
                .languages(List.of("en", "vi"))
                .skills(List.of("negotiation", "closing"))
                .experienceLevel(ExperienceLevel.SENIOR)
                .capacity(90)
                .maxMeetings(6)
                .build();

        TeamMemberEntity weakMatch = TeamMemberEntity.builder()
                .userId(101L)
                .languages(List.of("vi"))
                .skills(List.of("discovery"))
                .experienceLevel(ExperienceLevel.JUNIOR)
                .capacity(60)
                .maxMeetings(6)
                .build();

        int strongScore = matcher.calculate(request, account, strongMatch, 1);
        int weakScore = matcher.calculate(request, account, weakMatch, 1);

        assertThat(strongScore).isGreaterThan(weakScore);
    }

    @Test
    void canTakeMoreMeetings_returnsFalseWhenDailyLimitReached() {
        TeamMemberEntity member = TeamMemberEntity.builder()
                .maxMeetings(3)
                .build();

        assertThat(matcher.canTakeMoreMeetings(member, 3)).isFalse();
        assertThat(matcher.canTakeMoreMeetings(member, 2)).isTrue();
    }
}
