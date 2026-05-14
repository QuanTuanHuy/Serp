/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelinktype.command.delete;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record DeleteIssueLinkTypeCommand(
        Long id,
        Long tenantId,
        Long userId
) implements ICommand<DeleteIssueLinkTypeResult> {
}
