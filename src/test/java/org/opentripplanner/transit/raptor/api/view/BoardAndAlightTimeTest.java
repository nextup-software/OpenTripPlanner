package org.opentripplanner.transit.raptor.api.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opentripplanner.raptor.api.transit.BoardAndAlightTime;
import org.opentripplanner.raptor.api.transit.RaptorTripSchedule;
import org.opentripplanner.transit.raptor._data.transit.TestTripSchedule;

public class BoardAndAlightTimeTest {

  @Test
  public void testToString() {
    RaptorTripSchedule trip = TestTripSchedule
      .schedule("11:30 11:40 11:50")
      .pattern("L1", 2, 5, 3)
      .build();

    assertEquals("[5 ~ 11:40 11:50(10m) ~ 3]", new BoardAndAlightTime(trip, 1, 2).toString());
  }
}
