/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.service;

import serp.project.mailservice.core.domain.entity.EmailEntity;

public interface IEmailService {
    void validateEmail(EmailEntity email);
    
    void enrichEmail(EmailEntity email);
    
    boolean isHighPriority(EmailEntity email);
}
