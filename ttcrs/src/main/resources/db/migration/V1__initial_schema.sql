CREATE TABLE IF NOT EXISTS "locations" (
    "id"                    BIGSERIAL       NOT NULL,
    "tenant_id"             BIGINT          NOT NULL,
    "location_code"         VARCHAR(50)     NOT NULL,
    "lat"                   DOUBLE PRECISION,
    "lng"                   DOUBLE PRECISION,
    "type"                  VARCHAR(50)     NOT NULL CHECK ("type" IN ('PORT','WAREHOUSE','DEPOT_CONTAINER','DEPOT_TRUCK','DEPOT_TRAILER')),
    "created_stamp"         TIMESTAMP,
    "last_updated_stamp"    TIMESTAMP,
    CONSTRAINT "locations_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "locations_location_code_unique" UNIQUE ("location_code")
);

CREATE TABLE IF NOT EXISTS "trucks" (
    "id"                    BIGSERIAL   NOT NULL,
    "tenant_id"             BIGINT      NOT NULL,
    "code"                  VARCHAR(50) NOT NULL,
    "status"                VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE'
        CHECK ("status" IN ('AVAILABLE','IN_USE','MAINTENANCE')),
    "current_location_code" VARCHAR(50),
    "created_stamp"         TIMESTAMP,
    "last_updated_stamp"    TIMESTAMP,
    CONSTRAINT "trucks_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "trucks_code_unique" UNIQUE ("code"),
    CONSTRAINT "trucks_current_location_code_foreign"
        FOREIGN KEY ("current_location_code") REFERENCES "locations" ("location_code")
);

CREATE TABLE IF NOT EXISTS "trailers" (
    "id"                    BIGSERIAL   NOT NULL,
    "tenant_id"             BIGINT      NOT NULL,
    "code"                  VARCHAR(50) NOT NULL,
    "status"                VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE'
        CHECK ("status" IN ('AVAILABLE','IN_USE','MAINTENANCE')),
    "current_location_code" VARCHAR(50),
    "created_stamp"         TIMESTAMP,
    "last_updated_stamp"    TIMESTAMP,
    CONSTRAINT "trailers_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "trailers_code_unique" UNIQUE ("code"),
    CONSTRAINT "trailers_current_location_code_foreign"
        FOREIGN KEY ("current_location_code") REFERENCES "locations" ("location_code")
);

CREATE TABLE IF NOT EXISTS "containers" (
    "id"                    BIGSERIAL   NOT NULL,
    "tenant_id"             BIGINT      NOT NULL,
    "code"                  VARCHAR(50) NOT NULL,
    "size"                  VARCHAR(20) CHECK ("size" IN ('TWENTY','FORTY')),
    "status"                VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE'
        CHECK ("status" IN ('AVAILABLE','IN_USE','MAINTENANCE')),
    "current_location_code" VARCHAR(50),
    "created_stamp"         TIMESTAMP,
    "last_updated_stamp"    TIMESTAMP,
    CONSTRAINT "containers_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "containers_code_unique" UNIQUE ("code"),
    CONSTRAINT "containers_current_location_code_foreign"
        FOREIGN KEY ("current_location_code") REFERENCES "locations" ("location_code")
);

CREATE TABLE IF NOT EXISTS "drivers" (
    "id"                BIGSERIAL       NOT NULL,
    "tenant_id"         BIGINT          NOT NULL,
    "name"              VARCHAR(100)    NOT NULL,
    "status"            VARCHAR(50)     NOT NULL DEFAULT 'AVAILABLE'
        CHECK ("status" IN ('AVAILABLE','IN_USE','OFF')),
    "created_stamp"     TIMESTAMP,
    "last_updated_stamp" TIMESTAMP,
    CONSTRAINT "drivers_pkey" PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "transport_plans" (
    "id"                 BIGSERIAL   NOT NULL,
    "tenant_id"          BIGINT      NOT NULL,
    "truck_id"           BIGINT      NOT NULL,
    "driver_id"          BIGINT      NOT NULL,
    "start_time"         TIMESTAMP,
    "end_time"           TIMESTAMP,
    "status"             VARCHAR(50) DEFAULT 'CREATED'
        CHECK ("status" IN ('CREATED','EXECUTING','COMPLETED','CANCELLED')),
    "created_stamp"      TIMESTAMP,
    "last_updated_stamp" TIMESTAMP,
    CONSTRAINT "transport_plans_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "transport_plans_truck_id_foreign"
        FOREIGN KEY ("truck_id") REFERENCES "trucks" ("id"),
    CONSTRAINT "transport_plans_driver_id_foreign"
        FOREIGN KEY ("driver_id") REFERENCES "drivers" ("id")
);

CREATE TABLE IF NOT EXISTS "requests" (
    "id"                    BIGSERIAL   NOT NULL,
    "tenant_id"             BIGINT      NOT NULL,
    "src_location_code"     VARCHAR(50) NOT NULL,
    "dest_location_code"    VARCHAR(50) NOT NULL,
    "early_at_src"          TIMESTAMP,
    "late_at_src"           TIMESTAMP,
    "early_at_dest"         TIMESTAMP,
    "late_at_dest"          TIMESTAMP,
    "weight"                DOUBLE PRECISION,
    "container_size"        VARCHAR(20) CHECK ("container_size" IN ('TWENTY','FORTY')),
    "drop_trailer_required" BOOLEAN,
    "reason"                TEXT,
    "evidence_at_src"       TEXT,
    "evidence_at_dest"      TEXT,
    "status"                VARCHAR(50) NOT NULL DEFAULT 'PENDING'
        CHECK ("status" IN ('PENDING','PLANNED','IN_PROGRESS','COMPLETED','CANCELLED')),
    "type"                  VARCHAR(10) NOT NULL CHECK ("type" IN ('OF','IF','OE','IE')),
    "transport_plan_id"     BIGINT,
    "created_by"            BIGINT,
    "created_stamp"         TIMESTAMP,
    "last_updated_stamp"    TIMESTAMP,
    CONSTRAINT "requests_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "requests_src_location_code_foreign"
        FOREIGN KEY ("src_location_code") REFERENCES "locations" ("location_code"),
    CONSTRAINT "requests_dest_location_code_foreign"
        FOREIGN KEY ("dest_location_code") REFERENCES "locations" ("location_code"),
    CONSTRAINT "requests_transport_plan_id_foreign"
        FOREIGN KEY ("transport_plan_id") REFERENCES "transport_plans" ("id")
);

CREATE INDEX IF NOT EXISTS "requests_transport_plan_id_index"
    ON "requests" ("transport_plan_id");

CREATE TABLE IF NOT EXISTS "transport_plan_stops" (
    "id"                    BIGSERIAL   NOT NULL,
    "tenant_id"             BIGINT      NOT NULL,
    "transport_plan_id"     BIGINT      NOT NULL,
    "sequence"              INTEGER     NOT NULL,
    "location_code"         VARCHAR(50) NOT NULL,
    "request_id"            BIGINT,
    "trailer_id"            BIGINT,
    "action"                VARCHAR(50) NOT NULL
        CHECK ("action" IN ('DEPOT_START','DEPOT_END','PICKUP_CONTAINER','DELIVERY_CONTAINER','PICKUP_TRAILER','DROP_TRAILER')),
    "planned_arrival_time"  TIMESTAMP,
    "actual_arrival_time"   TIMESTAMP,
    "created_stamp"         TIMESTAMP,
    "last_updated_stamp"    TIMESTAMP,
    CONSTRAINT "transport_plan_stops_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "transport_plan_stops_transport_plan_id_sequence_unique"
        UNIQUE ("transport_plan_id", "sequence"),
    CONSTRAINT "transport_plan_stops_transport_plan_id_foreign"
        FOREIGN KEY ("transport_plan_id") REFERENCES "transport_plans" ("id"),
    CONSTRAINT "transport_plan_stops_location_code_foreign"
        FOREIGN KEY ("location_code") REFERENCES "locations" ("location_code"),
    CONSTRAINT "transport_plan_stops_request_id_foreign"
        FOREIGN KEY ("request_id") REFERENCES "requests" ("id")
);

CREATE INDEX IF NOT EXISTS "transport_plan_stops_transport_plan_id_index"
    ON "transport_plan_stops" ("transport_plan_id");

CREATE INDEX IF NOT EXISTS "transport_plan_stops_request_id_index"
    ON "transport_plan_stops" ("request_id");

CREATE TABLE IF NOT EXISTS "distances" (
    "id"                BIGSERIAL        NOT NULL,
    "tenant_id"         BIGINT,
    "src_code"          VARCHAR(50)      NOT NULL,
    "dest_code"         VARCHAR(50)      NOT NULL,
    "distance"          DOUBLE PRECISION NOT NULL,
    "travel_time"       DOUBLE PRECISION NOT NULL,
    "created_stamp"     TIMESTAMP,
    "last_updated_stamp" TIMESTAMP,
    CONSTRAINT "distances_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "distances_src_dest_unique" UNIQUE ("src_code", "dest_code"),
    CONSTRAINT "distances_src_code_foreign"
        FOREIGN KEY ("src_code") REFERENCES "locations" ("location_code"),
    CONSTRAINT "distances_dest_code_foreign"
        FOREIGN KEY ("dest_code") REFERENCES "locations" ("location_code")
);
