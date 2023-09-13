import { TripQuery } from '../gql/graphql.ts';
import { Card } from 'react-bootstrap';

export function ItineraryListContainer({ tripQueryResult }: { tripQueryResult: TripQuery | null }) {
  return (
    <section
      style={{
        width: '36rem',
        height: 'auto',
      }}
    >
      <h2>Itineraries</h2>
      {tripQueryResult &&
        tripQueryResult.trip.tripPatterns.map((tripPattern) => (
          <Card key={tripPattern.legs.map((leg) => leg.id).join('_')}>
            <pre>{JSON.stringify(tripPattern, null, 2)}</pre>
          </Card>
        ))}
    </section>
  );
}
