/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.domain;

import org.junit.jupiter.api.Test;
import serp.project.second_mile.enums.HubStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HubTest {

    @Test
    void canAcceptShouldReturnTrueWhenHubIsActiveAndHasRemainingCapacity() {
        Hub hub = new Hub();
        hub.setStatus(HubStatus.ACTIVE);
        hub.setDailyCapacity(10);
        hub.setCurrentLoad(8);

        assertTrue(hub.canAccept(2));
    }

    @Test
    void canAcceptShouldReturnFalseWhenIncomingOrdersExceedCapacity() {
        Hub hub = new Hub();
        hub.setStatus(HubStatus.ACTIVE);
        hub.setDailyCapacity(10);
        hub.setCurrentLoad(8);

        assertFalse(hub.canAccept(3));
    }

    @Test
    void releaseLoadShouldDecreaseCurrentLoadWithoutGoingBelowZero() {
        Hub hub = new Hub();
        hub.setCurrentLoad(2);

        hub.releaseLoad(5);

        assertEquals(0, hub.getCurrentLoad());
    }
}
