package org.opentripplanner.gtfs.mapping;

import static org.opentripplanner.gtfs.mapping.AgencyAndIdMapper.mapAgencyAndId;

import org.onebusaway.gtfs.model.Stop;
import org.opentripplanner.transit.model.base.WgsCoordinate;
import org.opentripplanner.transit.model.base.WheelchairAccessibility;
import org.opentripplanner.transit.model.framework.FeedScopedId;
import org.opentripplanner.transit.model.site.StationElement;
import org.opentripplanner.transit.model.site.StopLevel;

/**
 * Wrap GTFS Stop to provide a common base mapping for all {@link StationElement}s.
 */
class StopMappingWrapper {

  final Stop stop;

  public StopMappingWrapper(Stop stop) {
    this.stop = stop;
  }

  public FeedScopedId getId() {
    return mapAgencyAndId(stop.getId());
  }

  public String getName() {
    return stop.getName();
  }

  public String getCode() {
    return stop.getCode();
  }

  public String getDescription() {
    return stop.getDesc();
  }

  public WgsCoordinate getCoordinate() {
    return WgsCoordinateMapper.mapToDomain(stop);
  }

  public WheelchairAccessibility getWheelchairAccessibility() {
    return WheelchairAccessibility.valueOfGtfsCode(stop.getWheelchairBoarding());
  }

  public StopLevel getLevel() {
    if (stop.getLevel() == null) {
      return null;
    }
    return new StopLevel(stop.getLevel().getName(), stop.getLevel().getIndex());
  }
}
