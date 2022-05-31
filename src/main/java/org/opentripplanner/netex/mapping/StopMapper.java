package org.opentripplanner.netex.mapping;

import java.util.Collection;
import javax.annotation.Nullable;
import org.opentripplanner.common.model.T2;
import org.opentripplanner.graph_builder.DataImportIssueStore;
import org.opentripplanner.netex.issues.QuayWithoutCoordinates;
import org.opentripplanner.netex.mapping.support.FeedScopedIdFactory;
import org.opentripplanner.transit.model.base.WgsCoordinate;
import org.opentripplanner.transit.model.base.WheelchairAccessibility;
import org.opentripplanner.transit.model.network.TransitMode;
import org.opentripplanner.transit.model.site.FareZone;
import org.opentripplanner.transit.model.site.Station;
import org.opentripplanner.transit.model.site.Stop;
import org.opentripplanner.util.NonLocalizedString;
import org.rutebanken.netex.model.Quay;

class StopMapper {

  private final DataImportIssueStore issueStore;

  private final FeedScopedIdFactory idFactory;

  StopMapper(FeedScopedIdFactory idFactory, DataImportIssueStore issueStore) {
    this.idFactory = idFactory;
    this.issueStore = issueStore;
  }

  /**
   * Map Netex Quay to OTP Stop
   */
  @Nullable
  Stop mapQuayToStop(
    Quay quay,
    Station parentStation,
    Collection<FareZone> fareZones,
    T2<TransitMode, String> transitMode,
    WheelchairAccessibility wheelchair
  ) {
    WgsCoordinate coordinate = WgsCoordinateMapper.mapToDomain(quay.getCentroid());

    if (coordinate == null) {
      issueStore.add(new QuayWithoutCoordinates(quay.getId()));
      return null;
    }

    Stop stop = new Stop(
      idFactory.createId(quay.getId()),
      parentStation.getName(),
      quay.getPublicCode(),
      quay.getDescription() != null
        ? new NonLocalizedString(quay.getDescription().getValue())
        : null,
      WgsCoordinateMapper.mapToDomain(quay.getCentroid()),
      wheelchair,
      null,
      null,
      fareZones,
      null,
      null,
      transitMode.first,
      transitMode.second
    );

    stop.setParentStation(parentStation);

    return stop;
  }
}
