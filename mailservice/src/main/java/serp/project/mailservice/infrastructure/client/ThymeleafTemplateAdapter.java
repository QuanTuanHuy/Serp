/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.exceptions.TemplateProcessingException;
import serp.project.mailservice.core.port.client.ITemplateEnginePort;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ThymeleafTemplateAdapter implements ITemplateEnginePort {

    private final TemplateEngine templateEngine;

    @Override
    public String processTemplate(String templateContent, Map<String, Object> variables) {
        try {
            Context context = new Context();
            if (variables != null && !variables.isEmpty()) {
                context.setVariables(variables);
            }

            String renderedHtml = templateEngine.process(templateContent, context);

            log.debug("Template processed successfully");

            return renderedHtml;

        } catch (TemplateProcessingException e) {
            log.error("Error processing template: {}", e.getMessage(), e);
            throw new RuntimeException("Template processing error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error processing template: {}", e.getMessage(), e);
            throw new RuntimeException("Template processing failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validateTemplate(String templateContent) {
        try {
            Context context = new Context();
            templateEngine.process(templateContent, context);

            log.debug("Template validation successful");
            return true;

        } catch (TemplateProcessingException e) {
            log.warn("Template validation failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during template validation: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Render template from file (helper method)
     * Template path: classpath:/templates/email/{templateName}.html
     */
    public String renderTemplateFromFile(String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            if (variables != null && !variables.isEmpty()) {
                context.setVariables(variables);
            }

            String templatePath = "email/" + templateName;
            String renderedHtml = templateEngine.process(templatePath, context);

            log.debug("Template file rendered successfully: {}", templateName);

            return renderedHtml;

        } catch (TemplateInputException e) {
            log.error("Template file not found: {}", templateName, e);
            throw new RuntimeException("Template file not found: " + templateName, e);
        } catch (TemplateProcessingException e) {
            log.error("Error processing template file {}: {}", templateName, e.getMessage(), e);
            throw new RuntimeException("Template processing error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error rendering template file {}: {}", templateName, e.getMessage(), e);
            throw new RuntimeException("Template rendering failed: " + e.getMessage(), e);
        }
    }

    public boolean templateFileExists(String templateName) {
        try {
            String templatePath = "email/" + templateName;
            Context context = new Context();
            templateEngine.process(templatePath, context);
            return true;
        } catch (TemplateInputException e) {
            log.debug("Template file does not exist: {}", templateName);
            return false;
        } catch (Exception e) {
            log.error("Error checking template file existence: {}", e.getMessage());
            return false;
        }
    }
}
