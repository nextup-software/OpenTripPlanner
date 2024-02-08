package org.opentripplanner.street.model;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.Serializable;

/**
 * Holds limits of the street graph.
 * <p>
 * TODO this can be expanded to include some fields from the {@link org.opentripplanner.routing.graph.Graph}.
 */
@Singleton
public class StreetLimitationParameters implements Serializable {

  private float maxCarSpeed = 40f;

  @Inject
  public StreetLimitationParameters() {}

  /**
   * Initiliaze the maximum speed limit in m/s.
   */
  public void initMaxCarSpeed(float maxCarSpeed) {
    this.maxCarSpeed = maxCarSpeed;
  }

  /**
   * If this graph contains car routable streets, this value is the maximum speed limit in m/s.
   * Defaults to 40 m/s == 144 km/h.
   */
  public float maxCarSpeed() {
    return maxCarSpeed;
  }
}
