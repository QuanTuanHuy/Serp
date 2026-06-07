package serp.project.school_bus_service.shared.export;

public interface ExportHandler {
    String getExportCode();

    ExportResult export(ExportRequest request);
}
