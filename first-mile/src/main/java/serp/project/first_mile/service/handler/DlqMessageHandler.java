/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.handler;

public interface DlqMessageHandler {
    /**
     * Định nghĩa topic mà handler này chịu trách nhiệm xử lý.
     */
    String getSupportedTopic();

    /**
     * Logic xử lý payload khi retry thành công.
     */
    void process(String payload) throws Exception;
}