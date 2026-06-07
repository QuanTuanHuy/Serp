/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelinktype.command.update;

import serp.project.pmcore.application.issuelinktype.IssueLinkTypeView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record UpdateIssueLinkTypeCommand(
        Long id,
        String name,
        String outwardDescription,
        String inwardDescription,
        Long tenantId,
        Long userId
) implements ICommand<IssueLinkTypeView> {
}
