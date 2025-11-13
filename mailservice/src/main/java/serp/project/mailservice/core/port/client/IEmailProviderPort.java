/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.port.client;

import serp.project.mailservice.core.domain.entity.EmailEntity;

import java.util.Map;

public interface IEmailProviderPort {

    Map<String, Object> sendEmail(EmailEntity email);

    Map<String, Object> sendHtmlEmail(EmailEntity email);

    String getProviderName();

    boolean isHealthy();
}
