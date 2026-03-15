/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.port.client;

import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.domain.entity.EmailEntity;
import serp.project.mailservice.core.domain.dto.provider.ProviderSendResult;

public interface IEmailProviderPort {

    ProviderSendResult sendEmail(EmailEntity email);

    String getProviderName();

    EmailProvider getProviderType();

    boolean isHealthy();
}
