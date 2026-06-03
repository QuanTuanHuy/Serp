/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.enums;

/**
 * Identifies which service published an order sync event to avoid consume/publish loops.
 */
public enum OrderSyncEventSource {
    TMS_ORDER,
    FIRST_MILE,
    SECOND_MILE
}
