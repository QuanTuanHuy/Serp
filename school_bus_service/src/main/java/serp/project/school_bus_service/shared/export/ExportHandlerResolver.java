package serp.project.school_bus_service.shared.export;

import org.springframework.stereotype.Component;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ExportHandlerResolver {
    private final Map<String, ExportHandler> handlerMap;

    public ExportHandlerResolver(List<ExportHandler> handlers) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(ExportHandler::getExportCode, Function.identity()));
    }

    public ExportHandler resolve(String exportCode) {
        ExportHandler handler = handlerMap.get(exportCode);
        if (handler == null) {
            throw new AppException(AppErrorCode.Export.HANDLER_NOT_FOUND,
                    "No export handler found for export code: " + exportCode);
        }
        return handler;
    }
}
