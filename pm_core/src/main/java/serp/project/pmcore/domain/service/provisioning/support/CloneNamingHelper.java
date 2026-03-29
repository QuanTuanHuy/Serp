package serp.project.pmcore.domain.service.provisioning.support;

import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.shared.enums.CloneMode;
import serp.project.pmcore.domain.shared.enums.SchemeType;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

@Component
public class CloneNamingHelper {

    private static final int DEFAULT_NAME_MAX_LENGTH = 255;
    private static final int DEFAULT_WORKFLOW_KEY_MAX_LENGTH = 100;

    /**
     * JIRA-like naming:
     *   <PROJECT_KEY> - <Original Name>
     *   <PROJECT_KEY> - <Original Name> (Shared)
     * <p>
     * Examples:
     *   SERP - Default Issue Type Scheme
     *   SERP - Software Workflow Scheme
     *   SERP - Software Workflow Scheme (Shared)
     */
    public String buildSchemeCloneName(String projectKey,
                                       String sourceName,
                                       SchemeType schemeType,
                                       CloneMode cloneMode) {
        String normalizedProjectKey = cloneMode == CloneMode.SHARED ? "" : normalizeProjectKey(projectKey);
        String baseName = safeSourceName(sourceName, schemeType);

        String suffix = cloneMode == CloneMode.SHARED ? " (Shared)" : "";
        String candidate = baseName + suffix;
        if (!normalizedProjectKey.isBlank()) {
            candidate = candidate + " - " + normalizedProjectKey;
        }

        return truncate(candidate, DEFAULT_NAME_MAX_LENGTH);
    }

    public String buildWorkflowCloneKey(String projectKey,
                                        String sourceWorkflowKey,
                                        String sourceWorkflowName,
                                        Long sourceWorkflowId,
                                        CloneMode cloneMode,
                                        boolean appendUniquenessSuffix) {
        String normalizedProjectKey = normalizeProjectKey(projectKey);

        String baseKey = normalizeWorkflowKeyPart(sourceWorkflowKey);
        if (baseKey == null) {
            baseKey = normalizeWorkflowKeyPart(sourceWorkflowName);
        }
        if (baseKey == null) {
            baseKey = "workflow";
        }

        String modeSuffix = cloneMode == CloneMode.SHARED ? "shared" : "copy";

        String candidate = normalizedProjectKey.toLowerCase(Locale.ROOT)
                + "_"
                + baseKey;

        if (appendUniquenessSuffix) {
            candidate = candidate + "_" + modeSuffix + "_" + Objects.toString(sourceWorkflowId, "x");
        }

        return truncate(candidate, DEFAULT_WORKFLOW_KEY_MAX_LENGTH);
    }

    public String buildEntityCloneName(String projectKey,
                                       String sourceName,
                                       String entityLabel,
                                       CloneMode cloneMode) {
        String normalizedProjectKey = normalizeProjectKey(projectKey);
        String safeEntityLabel = (entityLabel == null || entityLabel.isBlank()) ? "Item" : entityLabel.trim();
        String safeSourceName = (sourceName == null || sourceName.isBlank()) ? safeEntityLabel : sourceName.trim();

        String suffix = cloneMode == CloneMode.SHARED ? " (Shared)" : "";
        return truncate(normalizedProjectKey + " - " + safeSourceName + suffix, DEFAULT_NAME_MAX_LENGTH);
    }

    public String normalizeProjectKey(String rawProjectKey) {
        if (rawProjectKey == null || rawProjectKey.isBlank()) {
            return "PROJECT";
        }

        String normalized = stripAccents(rawProjectKey)
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "");

        if (normalized.isBlank()) {
            return "PROJECT";
        }

        if (!Character.isLetter(normalized.charAt(0))) {
            normalized = "P" + normalized;
        }

        return normalized;
    }

    private String safeSourceName(String sourceName, SchemeType schemeType) {
        if (sourceName != null && !sourceName.isBlank()) {
            return sourceName.trim();
        }
        return defaultSchemeName(schemeType);
    }

    private String defaultSchemeName(SchemeType schemeType) {
        return switch (schemeType) {
            case ISSUE_TYPE -> "Issue Type Scheme";
            case WORKFLOW -> "Workflow Scheme";
            case FIELD_CONFIG -> "Field Configuration Scheme";
            case SCREEN -> "Screen Scheme";
            case PERMISSION -> "Permission Scheme";
            case NOTIFICATION -> "Notification Scheme";
            case PRIORITY -> "Priority Scheme";
            case ISSUE_SECURITY -> "Issue Security Scheme";
            default -> "Scheme";
        };
    }

    private String schemeLabel(SchemeType schemeType) {
        return switch (schemeType) {
            case ISSUE_TYPE -> "issue type scheme";
            case WORKFLOW -> "workflow scheme";
            case FIELD_CONFIG -> "field configuration scheme";
            case SCREEN -> "screen scheme";
            case PERMISSION -> "permission scheme";
            case NOTIFICATION -> "notification scheme";
            case PRIORITY -> "priority scheme";
            case ISSUE_SECURITY -> "issue security scheme";
            default -> "scheme";
        };
    }

    private String normalizeWorkflowKeyPart(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String normalized = stripAccents(raw)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "");

        return normalized.isBlank() ? null : normalized;
    }

    private String stripAccents(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}