package serp.project.school_bus_service.shared.export;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExportServiceImpl implements IExportService {

    private final ExportHandlerResolver handlerResolver;

    public ExportServiceImpl(ExportHandlerResolver handlerResolver) {
        this.handlerResolver = handlerResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public ExportResult export(ExportRequest request) {
        ExportHandler handler = handlerResolver.resolve(request.getExportCode());
        return handler.export(request);
    }
}
