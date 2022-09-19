package org.opentripplanner.routing.api.request.preference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Map;
import java.util.function.DoubleFunction;
import org.junit.jupiter.api.Test;
import org.opentripplanner.routing.api.request.RequestFunctions;
import org.opentripplanner.transit.model.basic.TransitMode;
import org.opentripplanner.transit.raptor.api.transit.SearchDirection;

class TransitPreferencesTest {

  private static final int BOARD_SLACK = 45;
  private static final int ALIGHT_SLACK = 35;
  private static final int OTHER_THAN_PREFERRED_ROUTES_PENALTY = 350;
  private static final Map<TransitMode, Double> RELUCTANCE_FOR_MODE = Map.of(
    TransitMode.AIRPLANE,
    2.1
  );
  private static final DoubleFunction<Double> UNPREFERRED_COST = RequestFunctions.parse(
    "300 + 1.15 x"
  );
  private static final Duration D15s = Duration.ofSeconds(15);
  private static final Duration D45s = Duration.ofSeconds(45);
  private static final Duration D25m = Duration.ofMinutes(25);
  private static final Duration D35m = Duration.ofMinutes(35);
  private static final SearchDirection RAPTOR_SEARCH_DIRECTION = SearchDirection.REVERSE;
  private static final boolean IGNORE_REALTIME_UPDATES = true;
  private static final boolean INCLUDE_PLANNED_CANCELLATIONS = true;

  private final TransitPreferences subject = TransitPreferences
    .of()
    .withBoardSlack(bs -> bs.withDefaultSec(BOARD_SLACK))
    .withAlightSlack(as -> as.withDefaultSec(ALIGHT_SLACK))
    .setReluctanceForMode(RELUCTANCE_FOR_MODE)
    .setOtherThanPreferredRoutesPenalty(OTHER_THAN_PREFERRED_ROUTES_PENALTY)
    .setUnpreferredCost(UNPREFERRED_COST)
    .withRaptorOptions(b -> b.withSearchDirection(RAPTOR_SEARCH_DIRECTION))
    .withBoardSlack(b -> b.withDefault(D45s).with(TransitMode.AIRPLANE, D35m))
    .withAlightSlack(b -> b.withDefault(D15s).with(TransitMode.AIRPLANE, D25m))
    .setIgnoreRealtimeUpdates(IGNORE_REALTIME_UPDATES)
    .setIncludePlannedCancellations(INCLUDE_PLANNED_CANCELLATIONS)
    .build();

  @Test
  void boardSlack() {
    assertEquals(D45s, subject.boardSlack().defaultValue());
    assertEquals(D35m, subject.boardSlack().valueOf(TransitMode.AIRPLANE));
  }

  @Test
  void alightSlack() {
    assertEquals(D15s, subject.alightSlack().defaultValue());
    assertEquals(D25m, subject.alightSlack().valueOf(TransitMode.AIRPLANE));
  }

  @Test
  void reluctanceForMode() {
    assertEquals(RELUCTANCE_FOR_MODE, subject.reluctanceForMode());
  }

  @Test
  void otherThanPreferredRoutesPenalty() {
    assertEquals(OTHER_THAN_PREFERRED_ROUTES_PENALTY, subject.otherThanPreferredRoutesPenalty());
  }

  @Test
  void unpreferredCost() {
    assertEquals(UNPREFERRED_COST, subject.unpreferredCost());
  }

  @Test
  void ignoreRealtimeUpdates() {
    assertFalse(TransitPreferences.DEFAULT.ignoreRealtimeUpdates());
    assertTrue(subject.ignoreRealtimeUpdates());
  }

  @Test
  void includePlannedCancellations() {
    assertFalse(TransitPreferences.DEFAULT.includePlannedCancellations());
    assertTrue(subject.includePlannedCancellations());
  }

  @Test
  void raptorOptions() {
    assertEquals(RAPTOR_SEARCH_DIRECTION, subject.raptorOptions().getSearchDirection());
  }

  @Test
  void testEquals() {
    // Return same object if no value is set
    assertSame(subject, subject.copyOf().build());
    assertSame(TransitPreferences.DEFAULT, TransitPreferences.of().build());

    // Create a copy, make a change and set it back again to force creating a new object
    var other = subject.copyOf().setIgnoreRealtimeUpdates(!IGNORE_REALTIME_UPDATES).build();
    var copy = other.copyOf().setIgnoreRealtimeUpdates(IGNORE_REALTIME_UPDATES).build();

    assertEquals(subject.toString(), copy.toString());
    assertEquals(subject.hashCode(), copy.hashCode());
    assertNotEquals(subject, other);
    assertNotEquals(subject.hashCode(), other.hashCode());
  }

  @Test
  void testToString() {
    assertEquals(
      "TransitPreferences{" +
      "boardSlack: DurationForTransitMode{default:45s, AIRPLANE:35m}, " +
      "alightSlack: DurationForTransitMode{default:15s, AIRPLANE:25m}, " +
      "reluctanceForMode: {AIRPLANE=2.1}, " +
      "otherThanPreferredRoutesPenalty: 350, " +
      "unpreferredCost: f(x) = 300.0 + 1.15 x, " +
      "ignoreRealtimeUpdates, " +
      "includePlannedCancellations" +
      "}",
      subject.toString()
    );
  }
}
