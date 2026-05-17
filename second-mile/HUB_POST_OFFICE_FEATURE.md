# Hub Post Office Assignment Feature

## Overview
This feature enables assigning Post Offices to Hubs across the first-mile and second-mile services, with Kafka-based synchronization to maintain consistency.

## Architecture

### Database Schema

#### first-mile service
- **Table**: `post_offices`
  - Added column: `hub_id BIGINT` (optional, references hub in second-mile)
  - Index: `idx_post_offices_hub_id`

#### second-mile service
- **Table**: `hub_post_office_mappings`
  - `id BIGSERIAL PRIMARY KEY`
  - `hub_id BIGINT NOT NULL` (FK to `hubs.id` with CASCADE DELETE)
  - `post_office_code VARCHAR(255) NOT NULL`
  - `tenant_id BIGINT`
  - Unique constraint: `(tenant_id, post_office_code)` - one hub per post office code per tenant

### API Endpoints

#### first-mile service (`/first-mile/api/v1`)

**List Post Offices with Hub filter**
```
GET /post-offices?hub_id={hubId}
```
Returns paginated list of post offices, optionally filtered by hub ID.

**Assign Post Office to Hub**
```
PUT /post-offices/{id}/hub
Content-Type: application/json

{
  "hub_id": 123
}
```
- Set `hub_id` to a valid hub ID to assign
- Set `hub_id` to `null` to clear assignment
- Returns: Updated `PostOfficeResponse` with `hubId`
- Authorization: `TMS_ADMIN` role required

#### second-mile service (`/second-mile/api/v1`)

**List Post Offices for a Hub**
```
GET /hubs/{hubId}/post-offices?page=0&size=20
```
Returns paginated list of `HubPostOfficeMappingResponse`.

**Assign Post Office to Hub**
```
POST /hubs/{hubId}/post-offices
Content-Type: application/json

{
  "post_office_code": "PO-001"
}
```
- Creates or replaces mapping (one hub per post office code per tenant)
- Returns: `HubPostOfficeMappingResponse`
- Authorization: `TMS_ADMIN` role required

**Remove Post Office from Hub**
```
DELETE /hubs/{hubId}/post-offices/{postOfficeCode}
```
- Removes the mapping
- Authorization: `TMS_ADMIN` role required

### Kafka Synchronization

**Topic**: `HUB_POST_OFFICE_SYNC` (configurable via `app.kafka.topics.sync-hub-post-office`)

**Event Schema**: `HubPostOfficeSyncEvent`
```json
{
  "event_type": "ASSIGNED" | "REMOVED",
  "origin": "FIRST_MILE" | "SECOND_MILE",
  "tenant_id": 1,
  "hub_id": 123,
  "post_office_code": "PO-001"
}
```

**Flow**:
1. When a change is made in either service (via REST API), the service publishes a Kafka event with its `origin`.
2. Both services consume from the topic, but each service only processes events from the **other** service (checked via `origin` field) to prevent circular updates.
3. Events are published **after** the database transaction commits (using `TransactionAfterCommit` utility).
4. Failed messages are moved to the Dead Letter Queue (DLQ) for manual replay.

**first-mile consumer**:
- Consumes events with `origin = SECOND_MILE`
- Updates `post_offices.hub_id` accordingly

**second-mile consumer**:
- Consumes events with `origin = FIRST_MILE`
- Inserts/deletes rows in `hub_post_office_mappings` accordingly

### Frontend (TMS)

**Route**: `/first-mile/hubs`

**Features**:
- List all hubs (from second-mile service)
- Search and filter hubs by status
- For each hub, view and manage assigned post offices:
  - View paginated list of assigned post offices
  - Assign a new post office to the hub
  - Remove a post office from the hub

**Implementation**:
- Page component: `serp_web/src/modules/first-mile/pages/hubs/HubListPage.tsx`
- API endpoints: `serp_web/src/modules/first-mile/api/firstMileApi.ts`
  - `useGetHubsQuery`
  - `useGetHubByIdQuery`
  - `useGetHubPostOfficesQuery`
  - `useAssignPostOfficeToHubMutation`
  - `useRemovePostOfficeFromHubMutation`
- Types: `serp_web/src/modules/first-mile/types/index.ts`
  - `Hub`, `HubType`, `HubStatus`
  - `HubPostOfficeMapping`
  - `AssignHubPostOfficeRequest`

## Configuration

### Environment Variables

**first-mile**:
```yaml
app:
  kafka:
    topics:
      sync-hub-post-office: ${SYNC_HUB_POST_OFFICE_TOPIC:HUB_POST_OFFICE_SYNC}
    hub-post-office-sync:
      consumer-group-id: ${KAFKA_FIRST_MILE_HUB_PO_GROUP:first-mile-sync-hub-post-office}
```

**second-mile**:
```yaml
app:
  kafka:
    topics:
      sync-hub-post-office: ${SYNC_HUB_POST_OFFICE_TOPIC:HUB_POST_OFFICE_SYNC}
    hub-post-office-sync:
      consumer-group-id: ${KAFKA_SECOND_MILE_HUB_PO_GROUP:second-mile-sync-hub-post-office}
```

## Deployment Steps

1. **Database migrations**:
   - first-mile: `db/migration/post-office-add-hub-id.sql`
   - second-mile: `db.migration/hub_post_office_mappings.sql`
   
   Migrations will run automatically on service startup if using Flyway/Liquibase.

2. **Kafka topic**: Ensure `HUB_POST_OFFICE_SYNC` topic exists (or services will auto-create if configured).

3. **Build and deploy**:
   - first-mile: `./mvnw clean package -DskipTests`
   - second-mile: `./mvnw clean package -DskipTests`
   - serp_web: `npm run build`

4. **Authorization**: Users need `TMS_ADMIN` role to assign/remove post offices.

## Testing

### Manual Testing

1. Start infrastructure:
   ```bash
   docker-compose -f docker-compose.dev.yml up -d
   ```

2. Start services:
   ```bash
   # first-mile
   cd first-mile
   ./mvnw spring-boot:run

   # second-mile
   cd second-mile
   ./mvnw spring-boot:run

   # frontend
   cd serp_web
   npm run dev
   ```

3. Navigate to `http://localhost:3000/first-mile/hubs` (adjust port if different).

4. Test scenarios:
   - Assign a post office to a hub via frontend → verify in both services' databases
   - Remove a post office from a hub → verify removal in both services
   - Assign via second-mile API → verify first-mile `post_offices.hub_id` is updated
   - Clear assignment via first-mile API → verify second-mile mapping is deleted

### Unit Tests

**first-mile**:
```bash
cd first-mile
./mvnw test
./mvnw -Dtest=PostOfficeServiceImplTest test
```

**second-mile**: (no new tests added yet for this feature)

**frontend**: (no test framework configured yet)

## Troubleshooting

### Kafka events not consumed
- Check consumer group status: `kafka-console-consumer --bootstrap-server localhost:9092 --topic HUB_POST_OFFICE_SYNC --from-beginning`
- Check service logs for consumer errors
- Check DLQ table: `SELECT * FROM kafka_dlq_messages WHERE topic = 'HUB_POST_OFFICE_SYNC';`

### Circular updates
- Each service checks `event.origin` and only processes events from the **other** service.
- If a loop is detected, verify the `origin` field is correctly set when publishing.

### Database migration issues
- first-mile: Check `schema-locations` in `application.yaml` includes `post-office-add-hub-id.sql`.
- second-mile: Verify migration file exists and is in the correct location.

## Future Enhancements

- Bulk assignment of multiple post offices to a hub
- Validation: prevent assigning post office to a hub in a different region/province
- Audit log for assignment changes
- Frontend notification/toast when Kafka sync completes
- Real-time UI updates when another user makes changes
