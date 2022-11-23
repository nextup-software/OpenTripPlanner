package org.opentripplanner.transit.raptor._data.stoparrival;

import org.opentripplanner.raptor.api.transit.RaptorAccessEgress;
import org.opentripplanner.raptor.api.view.AccessPathView;
import org.opentripplanner.transit.raptor._data.transit.TestAccessEgress;

public class Access extends AbstractStopArrival {

  private final RaptorAccessEgress access;

  public Access(int stop, int departureTime, int arrivalTime, int cost) {
    this(
      stop,
      arrivalTime,
      TestAccessEgress.walk(stop, Math.abs(arrivalTime - departureTime), cost)
    );
  }

  public Access(int stop, int arrivalTime, RaptorAccessEgress path) {
    super(0, stop, arrivalTime, path.generalizedCost(), null);
    this.access = path;
  }

  @Override
  public boolean arrivedByAccess() {
    return true;
  }

  @Override
  public AccessPathView accessPath() {
    return () -> access;
  }
}
