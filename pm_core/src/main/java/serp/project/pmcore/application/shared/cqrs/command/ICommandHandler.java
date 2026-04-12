/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.shared.cqrs.command;

@FunctionalInterface
public interface ICommandHandler<C extends ICommand<R>, R> {
    R handle(C command);
}
