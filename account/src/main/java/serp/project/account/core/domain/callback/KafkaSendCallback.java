package serp.project.account.core.domain.callback;

@FunctionalInterface
public interface KafkaSendCallback {
    void onComplete(boolean success, String topic, Object payload, Throwable ex);
}
