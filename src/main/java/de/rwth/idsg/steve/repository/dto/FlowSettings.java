package de.rwth.idsg.steve.repository.dto;

import de.rwth.idsg.steve.NotificationFeature;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * @author Sushant
 * @since 05.06.2021
 */
@Getter
@Builder
public class FlowSettings {
    private final boolean flowEnabled;
    private final String flow;
    private final List<NotificationFeature> enabledFeatures;
}
