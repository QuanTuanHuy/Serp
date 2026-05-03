/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelinktype.command.create;

import serp.project.pmcore.application.issuelinktype.IssueLinkTypeView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record CreateIssueLinkTypeCommand(
        String name,
        String outwardDescription,
        String inwardDescription,
        Long tenantId,
        Long userId
) implements ICommand<IssueLinkTypeView> {
}
