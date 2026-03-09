/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Event handler strategy contract
 */

package serp.project.discuss_service.ui.messaging.handler;

public interface IEventHandler<T, E extends Enum<E>> {

    E getType();

    void handle(T event);
}
