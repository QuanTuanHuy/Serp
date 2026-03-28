/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.shared.cqrs.query;

@FunctionalInterface
public interface IQueryHandler<Q extends IQuery<R>, R> {
    R handle(Q query);
}
