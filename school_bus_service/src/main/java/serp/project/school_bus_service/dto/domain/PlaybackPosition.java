package serp.project.school_bus_service.dto.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * Internal value object representing a computed position on the demo route.
 * Used by DemoPlaybackService jump methods to return interpolated/snapped position.
 */
@Getter
@Setter
public class PlaybackPosition {

    private Double latitude;
    private Double longitude;
    private Double progressPercent;
    private Integer currentStopOrder;
    private Boolean fallbackUsed;

    public PlaybackPosition() {
        this.fallbackUsed = false;
    }

    public PlaybackPosition(Double latitude, Double longitude, Double progressPercent,
                            Integer currentStopOrder, Boolean fallbackUsed) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.progressPercent = progressPercent;
        this.currentStopOrder = currentStopOrder;
        this.fallbackUsed = fallbackUsed;
    }
}
