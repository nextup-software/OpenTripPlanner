package org.opentripplanner.raptor.configure;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.IntPredicate;
import javax.annotation.Nullable;
import org.opentripplanner.framework.concurrent.OtpRequestThreadFactory;
import org.opentripplanner.raptor.api.model.RaptorTripSchedule;
import org.opentripplanner.raptor.api.request.RaptorRequest;
import org.opentripplanner.raptor.api.request.RaptorTuningParameters;
import org.opentripplanner.raptor.rangeraptor.DefaultRangeRaptorWorker;
import org.opentripplanner.raptor.rangeraptor.RangeRaptor;
import org.opentripplanner.raptor.rangeraptor.context.SearchContext;
import org.opentripplanner.raptor.rangeraptor.internalapi.Heuristics;
import org.opentripplanner.raptor.rangeraptor.internalapi.PassThroughPointsService;
import org.opentripplanner.raptor.rangeraptor.internalapi.RangeRaptorWorker;
import org.opentripplanner.raptor.rangeraptor.internalapi.RaptorWorkerResult;
import org.opentripplanner.raptor.rangeraptor.internalapi.RaptorWorkerState;
import org.opentripplanner.raptor.rangeraptor.internalapi.RoutingStrategy;
import org.opentripplanner.raptor.rangeraptor.multicriteria.configure.McRangeRaptorConfig;
import org.opentripplanner.raptor.rangeraptor.standard.configure.StdRangeRaptorConfig;
import org.opentripplanner.raptor.rangeraptor.transit.RaptorSearchWindowCalculator;
import org.opentripplanner.raptor.spi.RaptorTransitDataProvider;

/**
 * This class is responsible for creating a new search and holding application scoped Raptor state.
 * <p/>
 * This class should have APPLICATION scope. It manage a threadPool, and hold a reference to the
 * application tuning parameters.
 *
 * @param <T> The TripSchedule type defined by the user of the raptor API.
 */
public class RaptorConfig<T extends RaptorTripSchedule> {

  private final ExecutorService threadPool;
  private final RaptorTuningParameters tuningParameters;

  /** The service is not final, because it depends on the request. */
  private PassThroughPointsService passThroughPointsService = null;

  public RaptorConfig(RaptorTuningParameters tuningParameters) {
    this.tuningParameters = tuningParameters;
    this.threadPool = createNewThreadPool(tuningParameters.searchThreadPoolSize());
  }

  public static <T extends RaptorTripSchedule> RaptorConfig<T> defaultConfigForTest() {
    return new RaptorConfig<>(new RaptorTuningParameters() {});
  }

  public SearchContext<T> context(RaptorTransitDataProvider<T> transit, RaptorRequest<T> request) {
    // The passThroughPointsService is needed to create the context, so we initialize it here.
    this.passThroughPointsService = createPassThroughPointsService(request);
    return new SearchContext<>(request, tuningParameters, transit, acceptC2AtDestination());
  }

  public RangeRaptor<T> createStdWorker(
    RaptorTransitDataProvider<T> transitData,
    RaptorRequest<T> request
  ) {
    var context = context(transitData, request);
    var stdConfig = new StdRangeRaptorConfig<>(context);
    return createRangeRaptor(context, stdConfig.state(), stdConfig.strategy());
  }

  public RangeRaptor<T> createMcWorker(
    RaptorTransitDataProvider<T> transitData,
    RaptorRequest<T> request,
    Heuristics heuristics
  ) {
    var context = context(transitData, request);
    var mcConfig = new McRangeRaptorConfig<>(context, passThroughPointsService)
      .withHeuristics(heuristics);

    return createRangeRaptor(context, mcConfig.state(), mcConfig.strategy());
  }

  public RangeRaptor<T> createHeuristicSearch(
    RaptorTransitDataProvider<T> transitData,
    RaptorRequest<T> request
  ) {
    return createStdWorker(transitData, request);
  }

  public Heuristics createHeuristic(
    RaptorTransitDataProvider<T> transitData,
    RaptorRequest<T> request,
    RaptorWorkerResult<T> results
  ) {
    var context = context(transitData, request);
    return new StdRangeRaptorConfig<>(context).createHeuristics(results);
  }

  public boolean isMultiThreaded() {
    return threadPool != null;
  }

  public ExecutorService threadPool() {
    return threadPool;
  }

  public void shutdown() {
    if (threadPool != null) {
      threadPool.shutdown();
    }
  }

  public RaptorSearchWindowCalculator searchWindowCalculator() {
    return new RaptorSearchWindowCalculator(tuningParameters.dynamicSearchWindowCoefficients());
  }

  /* private factory methods */

  private static PassThroughPointsService createPassThroughPointsService(RaptorRequest<?> request) {
    return McRangeRaptorConfig.passThroughPointsService(request.multiCriteria());
  }

  private RangeRaptorWorker<T> createWorker(
    SearchContext<T> ctx,
    RaptorWorkerState<T> workerState,
    RoutingStrategy<T> routingStrategy
  ) {
    return new DefaultRangeRaptorWorker<>(
      workerState,
      routingStrategy,
      ctx.transit(),
      ctx.slackProvider(),
      ctx.accessPaths(),
      ctx.calculator(),
      ctx.lifeCycle(),
      ctx.performanceTimers(),
      ctx.useConstrainedTransfers()
    );
  }

  private RangeRaptor<T> createRangeRaptor(SearchContext<T> ctx, RangeRaptorWorker<T> worker) {
    return new RangeRaptor<>(
      worker,
      ctx.transit(),
      ctx.accessPaths(),
      ctx.roundTracker(),
      ctx.calculator(),
      ctx.createLifeCyclePublisher(),
      ctx.performanceTimers()
    );
  }

  private RangeRaptor<T> createRangeRaptor(
    SearchContext<T> ctx,
    RaptorWorkerState<T> workerState,
    RoutingStrategy<T> routingStrategy
  ) {
    return createRangeRaptor(ctx, createWorker(ctx, workerState, routingStrategy));
  }

  private IntPredicate acceptC2AtDestination() {
    return passThroughPointsService.isNoop()
      ? null
      : passThroughPointsService.acceptC2AtDestination();
  }

  @Nullable
  private ExecutorService createNewThreadPool(int size) {
    return size > 0
      ? Executors.newFixedThreadPool(size, OtpRequestThreadFactory.of("raptor-%d"))
      : null;
  }
}
