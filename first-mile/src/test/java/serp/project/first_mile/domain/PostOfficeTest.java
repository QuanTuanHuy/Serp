/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.domain;

import org.junit.jupiter.api.Test;
import serp.project.first_mile.enums.PostOfficeStatus;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostOfficeTest {
    @Test
    void canAcceptShouldReturnTrueWhenPostOfficeIsActiveAndHasRemainingCapacity() {
        PostOffice postOffice = new PostOffice();
        postOffice.setStatus(PostOfficeStatus.ACTIVE);
        postOffice.setDailyCapacity(100);
        postOffice.setCurrentLoad(80);

        assertTrue(postOffice.canAccept(20));
    }

    @Test
    void canAcceptShouldReturnFalseWhenIncomingOrdersExceedCapacity() {
        PostOffice postOffice = new PostOffice();
        postOffice.setStatus(PostOfficeStatus.ACTIVE);
        postOffice.setDailyCapacity(100);
        postOffice.setCurrentLoad(90);

        assertFalse(postOffice.canAccept(11));
    }

    @Test
    void canAcceptShouldReturnFalseWhenPostOfficeIsNotActive() {
        PostOffice postOffice = new PostOffice();
        postOffice.setStatus(PostOfficeStatus.MAINTENANCE);
        postOffice.setDailyCapacity(100);
        postOffice.setCurrentLoad(40);

        assertFalse(postOffice.canAccept(10));
    }
}
