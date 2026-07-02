/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.handler;

public interface DlqMessageHandler {
    String getSupportedTopic();

    void process(String payload) throws Exception;
}
