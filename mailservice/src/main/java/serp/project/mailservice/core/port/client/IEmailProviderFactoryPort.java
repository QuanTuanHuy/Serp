/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.port.client;

import serp.project.mailservice.core.domain.enums.EmailProvider;

public interface IEmailProviderFactoryPort {
    IEmailProviderPort getProvider(EmailProvider provider);

    IEmailProviderPort getHealthyProvider(EmailProvider preferredProvider);
}
