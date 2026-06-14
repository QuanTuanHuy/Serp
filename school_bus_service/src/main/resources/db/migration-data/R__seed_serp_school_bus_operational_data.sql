-- Phase 2 operational data for the Serp school bus module.
-- Foundation data is owned by R__seed_serp_school_bus_foundation_data.sql.
--
-- Attendance is deliberately generated once per real operational event. With the
-- Phase 1 point distribution, 2-5 service stops per route, and the required trip
-- scenarios, 76 consistent events are possible. Inflating this to 160 would require
-- duplicate BOARDED/DROPPED_OFF events for the same student and stop.

DO $$
DECLARE
    v_tenant_id CONSTANT bigint := 1;
    v_seed_by CONSTANT varchar(100) := 'SEED_DATA';
BEGIN
    IF (SELECT count(*) FROM public.school_bus_school
        WHERE tenant_id = v_tenant_id AND created_by = v_seed_by AND is_deleted = false) <> 3
       OR (SELECT count(*) FROM public.school_bus_depot
           WHERE tenant_id = v_tenant_id AND created_by = v_seed_by AND is_deleted = false) < 2
       OR (SELECT count(*) FROM public.school_bus_bus
           WHERE tenant_id = v_tenant_id AND created_by = v_seed_by AND is_deleted = false) < 15
       OR (SELECT count(*) FROM public.school_bus_driver_profile
           WHERE tenant_id = v_tenant_id AND created_by = v_seed_by AND is_deleted = false) < 18
       OR (SELECT count(*) FROM public.school_bus_attendant_profile
           WHERE tenant_id = v_tenant_id AND created_by = v_seed_by AND is_deleted = false) < 12
       OR (SELECT count(*) FROM public.school_bus_student
           WHERE tenant_id = v_tenant_id AND created_by = v_seed_by AND is_deleted = false) < 120
       OR (SELECT count(*) FROM public.school_bus_student_subscription
           WHERE tenant_id = v_tenant_id
             AND created_by = v_seed_by
             AND status = 'ACTIVE'
             AND is_active = true
             AND is_deleted = false) < 120
       OR (SELECT count(*) FROM public.school_bus_pickup_point
           WHERE tenant_id = v_tenant_id AND created_by = v_seed_by AND is_deleted = false) < 24
       OR (SELECT count(*) FROM public.school_bus_school_pickup_point
           WHERE tenant_id = v_tenant_id AND created_by = v_seed_by AND is_deleted = false) < 58
    THEN
        RAISE EXCEPTION
            'Foundation seed data is missing. Run R__seed_serp_school_bus_foundation_data.sql first.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_route_plan
        WHERE tenant_id = v_tenant_id
          AND route_code BETWEEN 'RTE000001' AND 'RTE000012'
          AND created_by IS DISTINCT FROM v_seed_by
          AND is_deleted = false
    ) THEN
        RAISE EXCEPTION 'Operational seed route code namespace collision detected';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_trip_execution
        WHERE tenant_id = v_tenant_id
          AND trip_code BETWEEN 'TRP000001' AND 'TRP000008'
          AND created_by IS DISTINCT FROM v_seed_by
          AND is_deleted = false
    ) THEN
        RAISE EXCEPTION 'Operational seed trip code namespace collision detected';
    END IF;

    DELETE FROM public.school_bus_attendance
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_trip_student
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_trip_stop_log
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_trip_execution
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_route_assignment_history
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_route_assignment
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_route_plan_student
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_route_stop
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_route_plan
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_route_planning_session
    WHERE created_by = v_seed_by;
END $$;

DROP TABLE IF EXISTS seed_route_definition;

CREATE TEMP TABLE seed_route_definition (
    route_code varchar(100) PRIMARY KEY,
    route_name varchar(255) NOT NULL,
    school_code varchar(100) NOT NULL,
    route_direction varchar(30) NOT NULL,
    service_date date NOT NULL,
    session_status varchar(30) NOT NULL,
    route_status varchar(30) NOT NULL,
    depot_code varchar(100) NOT NULL,
    point_codes text[] NOT NULL,
    planned_start_time time NOT NULL,
    planned_end_time time NOT NULL,
    planned_distance_km double precision NOT NULL,
    planned_duration_min integer NOT NULL,
    trip_code varchar(100),
    trip_status varchar(30),
    trip_scenario varchar(30)
);

INSERT INTO seed_route_definition (
    route_code, route_name, school_code, route_direction, service_date,
    session_status, route_status, depot_code, point_codes,
    planned_start_time, planned_end_time,
    planned_distance_km, planned_duration_min,
    trip_code, trip_status, trip_scenario
)
VALUES
    ('RTE000001', 'Kindergarten West Corridor - To School',
     'SBU000001', 'OUTBOUND', date '2026-06-22', 'PUBLISHED', 'TRIP_CREATED',
     'DPT000001', ARRAY['PKP000002', 'PKP000003', 'PKP000004', 'PKP000005'],
     time '06:20', time '07:20', 22.4, 60, 'TRP000001', 'ASSIGNED', 'ASSIGNED'),
    ('RTE000002', 'Kindergarten West Corridor - Return',
     'SBU000001', 'RETURN', date '2026-06-22', 'PUBLISHED', 'TRIP_CREATED',
     'DPT000001', ARRAY['PKP000006', 'PKP000007', 'PKP000008', 'PKP000009', 'PKP000010'],
     time '15:20', time '16:30', 24.8, 70, 'TRP000002', 'COMPLETED', 'COMPLETED'),
    ('RTE000003', 'Primary West Corridor - To School',
     'SBU000002', 'OUTBOUND', date '2026-06-22', 'PUBLISHED', 'TRIP_CREATED',
     'DPT000001', ARRAY['PKP000002', 'PKP000003', 'PKP000004', 'PKP000005'],
     time '06:10', time '07:15', 23.6, 65, 'TRP000003', 'IN_PROGRESS', 'BOARDING'),
    ('RTE000004', 'Primary Central Corridor - To School',
     'SBU000002', 'OUTBOUND', date '2026-06-22', 'PUBLISHED', 'TRIP_CREATED',
     'DPT000002', ARRAY['PKP000001', 'PKP000015', 'PKP000016', 'PKP000017', 'PKP000020'],
     time '06:25', time '07:20', 17.9, 55, 'TRP000004', 'COMPLETED', 'COMPLETED'),
    ('RTE000005', 'Primary West Corridor - Return',
     'SBU000002', 'RETURN', date '2026-06-22', 'PUBLISHED', 'TRIP_CREATED',
     'DPT000001', ARRAY['PKP000001', 'PKP000002', 'PKP000003', 'PKP000004'],
     time '15:30', time '16:35', 22.8, 65, 'TRP000005', 'ASSIGNED', 'ASSIGNED'),
    ('RTE000006', 'Primary Central Corridor - Return',
     'SBU000002', 'RETURN', date '2026-06-22', 'PUBLISHED', 'TRIP_CREATED',
     'DPT000002', ARRAY['PKP000005', 'PKP000006', 'PKP000007', 'PKP000008', 'PKP000016'],
     time '15:40', time '16:55', 25.5, 75, 'TRP000006', 'COMPLETED', 'COMPLETED'),
    ('RTE000007', 'Secondary West Corridor - To School',
     'SBU000003', 'OUTBOUND', date '2026-06-22', 'PUBLISHED', 'TRIP_CREATED',
     'DPT000001', ARRAY['PKP000005', 'PKP000006', 'PKP000007', 'PKP000008'],
     time '06:05', time '07:10', 26.2, 65, 'TRP000007', 'IN_PROGRESS', 'MOVING'),
    ('RTE000008', 'Secondary East Corridor - To School',
     'SBU000003', 'OUTBOUND', date '2026-06-22', 'PUBLISHED', 'PUBLISHED',
     'DPT000002', ARRAY['PKP000010', 'PKP000011', 'PKP000012', 'PKP000013'],
     time '06:20', time '07:15', 19.7, 55, NULL, NULL, NULL),
    ('RTE000009', 'Secondary West Corridor - Return',
     'SBU000003', 'RETURN', date '2026-06-22', 'PUBLISHED', 'TRIP_CREATED',
     'DPT000001', ARRAY['PKP000005', 'PKP000006', 'PKP000007', 'PKP000008'],
     time '15:25', time '16:30', 25.9, 65, 'TRP000008', 'CANCELLED', 'CANCELLED'),
    ('RTE000010', 'Secondary East Corridor - Return',
     'SBU000003', 'RETURN', date '2026-06-22', 'PUBLISHED', 'PUBLISHED',
     'DPT000002', ARRAY['PKP000010', 'PKP000011', 'PKP000012', 'PKP000014'],
     time '15:35', time '16:35', 20.4, 60, NULL, NULL, NULL),
    ('RTE000011', 'Primary East Corridor - To School',
     'SBU000002', 'OUTBOUND', date '2026-06-23', 'REVIEWING', 'REVIEWING',
     'DPT000002', ARRAY['PKP000018', 'PKP000019', 'PKP000020', 'PKP000021'],
     time '06:15', time '07:20', 24.1, 65, NULL, NULL, NULL),
    ('RTE000012', 'Secondary North Corridor - Return',
     'SBU000003', 'RETURN', date '2026-06-23', 'DRAFT', 'DRAFT',
     'DPT000001', ARRAY['PKP000015', 'PKP000016', 'PKP000017', 'PKP000018'],
     time '15:30', time '16:25', 18.6, 55, NULL, NULL, NULL);

DROP TABLE IF EXISTS seed_route_geometry;

CREATE TEMP TABLE seed_route_geometry (
    route_code varchar(100) PRIMARY KEY,
    planned_distance_km double precision NOT NULL,
    planned_duration_min integer NOT NULL,
    geometry_path text NOT NULL
);

-- Snapshots generated by OSRM route/v1/driving with overview=simplified and geometries=geojson.
INSERT INTO seed_route_geometry (
    route_code, planned_distance_km, planned_duration_min, geometry_path
)
VALUES
    ('RTE000001', 28.34, 37, '[[105.77814,21.028896],[105.779064,21.028766],[105.778492,21.023831],[105.779533,21.020777],[105.792329,21.004616],[105.805034,21.01536],[105.814407,21.008639],[105.816954,21.005215],[105.818289,21.001886],[105.81589,21.000316],[105.813851,21.002506],[105.814922,21.002615],[105.813851,21.002506],[105.81589,21.000316],[105.810376,20.996621],[105.805881,21.002638],[105.806962,21.00392],[105.80299,21.007083],[105.80139,21.006387],[105.80299,21.007083],[105.799561,21.010443],[105.805148,21.015472],[105.800957,21.02176],[105.798212,21.024547],[105.799143,21.027024],[105.801988,21.030001],[105.800464,21.031012],[105.801923,21.032772],[105.800674,21.033146],[105.801923,21.032772],[105.800464,21.031012],[105.797463,21.033904],[105.797723,21.042873],[105.797535,21.041876],[105.79753,21.035341],[105.797248,21.034361],[105.796221,21.034265],[105.801515,21.030011],[105.798806,21.026836],[105.798138,21.024392],[105.800739,21.02182],[105.804408,21.015933],[105.814282,21.008755],[105.818829,21.003488],[105.833474,20.999323],[105.849756,20.995858],[105.858268,20.995088],[105.86734,20.997964],[105.867345,20.994696]]'),
    ('RTE000002', 34.86, 41, '[[105.867345,20.994696],[105.867562,20.996846],[105.865731,20.996833],[105.865451,20.99758],[105.858204,20.995336],[105.847314,20.996431],[105.819397,21.003419],[105.817406,21.004834],[105.814374,21.008864],[105.804938,21.015658],[105.798695,21.009989],[105.79232,21.005689],[105.783231,21.017053],[105.779987,21.020701],[105.778913,21.023368],[105.780522,21.036126],[105.77884,21.028957],[105.775261,21.029865],[105.776309,21.029169],[105.780986,21.028491],[105.779454,21.028835],[105.78092,21.036868],[105.78208,21.054407],[105.78517,21.06648],[105.786154,21.0759],[105.787874,21.076753],[105.788442,21.074795],[105.789379,21.075034],[105.788442,21.074795],[105.787874,21.076753],[105.788596,21.078311],[105.811216,21.069438],[105.810735,21.067415],[105.809109,21.062398],[105.809044,21.052458],[105.805962,21.047552],[105.810539,21.046368],[105.816378,21.041971],[105.812929,21.031952],[105.812366,21.031606],[105.807496,21.032943],[105.805065,21.034552],[105.80164,21.030163],[105.797263,21.034028],[105.791539,21.035934],[105.780298,21.036882],[105.779141,21.029202],[105.775261,21.029865],[105.77814,21.028896]]'),
    ('RTE000003', 28.25, 37, '[[105.77814,21.028896],[105.779064,21.028766],[105.778492,21.023831],[105.779533,21.020777],[105.792329,21.004616],[105.805034,21.01536],[105.814407,21.008639],[105.816954,21.005215],[105.818289,21.001886],[105.81589,21.000316],[105.813851,21.002506],[105.814922,21.002615],[105.813851,21.002506],[105.81589,21.000316],[105.810376,20.996621],[105.805881,21.002638],[105.806962,21.00392],[105.80299,21.007083],[105.80139,21.006387],[105.80299,21.007083],[105.799561,21.010443],[105.805148,21.015472],[105.800957,21.02176],[105.798212,21.024547],[105.799143,21.027024],[105.801988,21.030001],[105.800464,21.031012],[105.801923,21.032772],[105.800674,21.033146],[105.801923,21.032772],[105.800464,21.031012],[105.797463,21.033904],[105.797723,21.042873],[105.797535,21.041876],[105.79753,21.035341],[105.797248,21.034361],[105.796221,21.034265],[105.801515,21.030011],[105.798806,21.026836],[105.798138,21.024392],[105.800739,21.02182],[105.804408,21.015933],[105.814282,21.008755],[105.818829,21.003488],[105.833474,20.999323],[105.849756,20.995858],[105.858268,20.995088],[105.86734,20.997964],[105.867558,20.99505]]'),
    ('RTE000004', 28.91, 42, '[[105.899296,21.027973],[105.899717,21.028374],[105.898202,21.029084],[105.899456,21.030976],[105.890376,21.0182],[105.871725,21.000147],[105.865789,20.997704],[105.86734,20.997964],[105.867562,20.996846],[105.869054,20.996843],[105.869075,20.993899],[105.868583,20.993896],[105.86908,20.993098],[105.865978,20.993078],[105.865451,20.99758],[105.858204,20.995336],[105.849957,20.996128],[105.849377,20.99213],[105.847431,20.98993],[105.846032,20.983729],[105.840925,20.983817],[105.840848,20.978594],[105.84141,20.983469],[105.84172,20.982883],[105.841479,20.983518],[105.847706,20.983561],[105.846032,20.983729],[105.847399,20.98985],[105.849507,20.992632],[105.850572,21.000635],[105.849655,21.000471],[105.850572,21.000635],[105.84991,20.995848],[105.857286,20.995056],[105.860374,20.995551],[105.85876,20.99541],[105.858858,20.996977],[105.85876,20.99541],[105.858204,20.995336],[105.849957,20.996128],[105.851733,21.013097],[105.851413,21.017468],[105.853819,21.02898],[105.853065,21.03163],[105.851633,21.031879],[105.851002,21.031406],[105.851395,21.028785],[105.851667,21.02596],[105.857079,21.024209],[105.860156,21.024759],[105.863737,21.0118],[105.865108,21.009476],[105.86887,21.00562],[105.871005,20.999758],[105.865789,20.997704],[105.86741,20.99786],[105.867558,20.99505]]'),
    ('RTE000005', 18.93, 29, '[[105.867558,20.99505],[105.867469,20.992976],[105.86908,20.993098],[105.868583,20.993896],[105.86908,20.993098],[105.865978,20.993078],[105.865451,20.99758],[105.859599,20.995601],[105.856873,20.995338],[105.846677,20.996555],[105.820231,21.003185],[105.81589,21.000316],[105.813851,21.002506],[105.814922,21.002615],[105.813851,21.002506],[105.81589,21.000316],[105.810376,20.996621],[105.805881,21.002638],[105.806962,21.00392],[105.80299,21.007083],[105.80139,21.006387],[105.80299,21.007083],[105.799561,21.010443],[105.805148,21.015472],[105.800957,21.02176],[105.798212,21.024547],[105.799143,21.027024],[105.801988,21.030001],[105.800464,21.031012],[105.801923,21.032772],[105.800674,21.033146],[105.801923,21.032772],[105.800464,21.031012],[105.797152,21.034088],[105.790385,21.036294],[105.780298,21.036882],[105.779141,21.029202],[105.775261,21.029865],[105.77814,21.028896]]'),
    ('RTE000006', 48.60, 55, '[[105.867558,20.99505],[105.867562,20.996846],[105.865731,20.996833],[105.865451,20.99758],[105.858204,20.995336],[105.847314,20.996431],[105.818946,21.003639],[105.814374,21.008864],[105.804674,21.01588],[105.800957,21.02176],[105.798268,21.024405],[105.799073,21.026921],[105.801988,21.030001],[105.797463,21.033904],[105.797723,21.042873],[105.797535,21.041876],[105.797595,21.039055],[105.794061,21.037657],[105.793402,21.035348],[105.789651,21.036435],[105.780298,21.036882],[105.778556,21.023266],[105.779661,21.020577],[105.783251,21.016102],[105.783231,21.017053],[105.779987,21.020701],[105.778913,21.023368],[105.780522,21.036126],[105.77884,21.028957],[105.775261,21.029865],[105.776309,21.029169],[105.780986,21.028491],[105.779454,21.028835],[105.78092,21.036868],[105.78208,21.054407],[105.78517,21.06648],[105.786154,21.0759],[105.787874,21.076753],[105.788442,21.074795],[105.789379,21.075034],[105.788442,21.074795],[105.788524,21.074325],[105.790689,21.071241],[105.798063,21.07148],[105.801099,21.070645],[105.80571,21.064697],[105.804411,21.061919],[105.804333,21.05202],[105.805924,21.045258],[105.806775,21.037315],[105.80346,21.032058],[105.798876,21.026938],[105.798049,21.024886],[105.800886,21.021622],[105.803782,21.016641],[105.806798,21.01373],[105.814282,21.008755],[105.81729,21.0048],[105.819256,21.003277],[105.833474,20.999323],[105.84991,20.995848],[105.850572,21.000635],[105.849655,21.000471],[105.850572,21.000635],[105.84991,20.995848],[105.857213,20.995059],[105.860629,20.995631],[105.870947,20.999446],[105.891172,21.018762],[105.898202,21.029084],[105.899717,21.028374],[105.899296,21.027973]]'),
    ('RTE000007', 34.88, 39, '[[105.77814,21.028896],[105.787753,21.027222],[105.79051,21.03839],[105.790403,21.041637],[105.794629,21.041634],[105.796701,21.042849],[105.79753,21.042855],[105.797535,21.041876],[105.797595,21.039055],[105.794061,21.037657],[105.793402,21.035348],[105.789651,21.036435],[105.780298,21.036882],[105.778556,21.023266],[105.779661,21.020577],[105.783251,21.016102],[105.783231,21.017053],[105.779987,21.020701],[105.778913,21.023368],[105.780522,21.036126],[105.77884,21.028957],[105.775261,21.029865],[105.776309,21.029169],[105.780986,21.028491],[105.779454,21.028835],[105.78092,21.036868],[105.78208,21.054407],[105.78517,21.06648],[105.786154,21.0759],[105.787874,21.076753],[105.788442,21.074795],[105.789379,21.075034],[105.788442,21.074795],[105.788524,21.074325],[105.790689,21.071241],[105.798063,21.07148],[105.801099,21.070645],[105.80571,21.064697],[105.804411,21.061919],[105.804333,21.05202],[105.805924,21.045258],[105.806775,21.037315],[105.80346,21.032058],[105.798876,21.026938],[105.798049,21.024886],[105.800886,21.021622],[105.804408,21.015933],[105.814249,21.008782],[105.81729,21.0048],[105.819256,21.003277],[105.846053,20.996372],[105.85796,20.995059],[105.86734,20.997964],[105.867553,20.99545]]'),
    ('RTE000008', 32.95, 42, '[[105.899296,21.027973],[105.899717,21.028374],[105.898202,21.029084],[105.899456,21.030976],[105.895909,21.027355],[105.889642,21.028978],[105.881405,21.030063],[105.865694,21.039298],[105.865512,21.04022],[105.865566,21.039664],[105.854409,21.035501],[105.859209,21.02571],[105.859242,21.02494],[105.856965,21.02444],[105.844232,21.028017],[105.841049,21.030708],[105.830069,21.032789],[105.815964,21.030663],[105.812366,21.031606],[105.812929,21.031952],[105.813089,21.031284],[105.816225,21.030432],[105.821684,21.03116],[105.824737,21.027802],[105.823833,21.027057],[105.823059,21.026394],[105.821625,21.027994],[105.82072,21.029529],[105.821619,21.030085],[105.822483,21.030263],[105.824737,21.027802],[105.819787,21.023569],[105.809563,21.008869],[105.79668,20.999451],[105.80263,20.991974],[105.802354,20.991076],[105.78787,20.980377],[105.786301,20.981562],[105.787484,20.980815],[105.787608,20.97979],[105.820048,21.00272],[105.845689,20.996441],[105.856193,20.995151],[105.85938,20.995272],[105.86734,20.997964],[105.867553,20.99545]]'),
    ('RTE000009', 33.02, 37, '[[105.867553,20.99545],[105.867562,20.996846],[105.865731,20.996833],[105.865451,20.99758],[105.858204,20.995336],[105.847314,20.996431],[105.818946,21.003639],[105.814374,21.008864],[105.804674,21.01588],[105.800957,21.02176],[105.798268,21.024405],[105.799073,21.026921],[105.801988,21.030001],[105.797463,21.033904],[105.797723,21.042873],[105.797535,21.041876],[105.797595,21.039055],[105.794061,21.037657],[105.793402,21.035348],[105.789651,21.036435],[105.780298,21.036882],[105.778556,21.023266],[105.779661,21.020577],[105.783251,21.016102],[105.783231,21.017053],[105.779987,21.020701],[105.778913,21.023368],[105.780522,21.036126],[105.77884,21.028957],[105.775261,21.029865],[105.776309,21.029169],[105.780986,21.028491],[105.779454,21.028835],[105.78092,21.036868],[105.78208,21.054407],[105.78517,21.06648],[105.786154,21.0759],[105.787874,21.076753],[105.788442,21.074795],[105.789379,21.075034],[105.788442,21.074795],[105.787874,21.076753],[105.786215,21.07679],[105.784931,21.067122],[105.781839,21.054372],[105.779141,21.029202],[105.775261,21.029865],[105.77814,21.028896]]'),
    ('RTE000010', 35.22, 43, '[[105.867553,20.99545],[105.867562,20.996846],[105.865731,20.996833],[105.865451,20.99758],[105.858204,20.995336],[105.847314,20.996431],[105.818946,21.003639],[105.814374,21.008864],[105.805148,21.015472],[105.806665,21.016971],[105.811643,21.025504],[105.813207,21.030475],[105.813373,21.031364],[105.812366,21.031606],[105.812929,21.031952],[105.813089,21.031284],[105.816225,21.030432],[105.821684,21.03116],[105.824737,21.027802],[105.823833,21.027057],[105.823059,21.026394],[105.821625,21.027994],[105.82072,21.029529],[105.821619,21.030085],[105.822483,21.030263],[105.821684,21.03116],[105.828989,21.032407],[105.841569,21.028865],[105.840768,20.972434],[105.841853,20.965901],[105.833454,20.966884],[105.833035,20.965819],[105.826897,20.965003],[105.825683,20.964855],[105.826284,20.96704],[105.829619,20.969338],[105.83486,20.970742],[105.838603,20.97103],[105.83992,20.970231],[105.841576,20.970301],[105.841741,20.969702],[105.841034,20.971933],[105.841071,20.9765],[105.845499,20.981532],[105.847391,20.989823],[105.849306,20.991956],[105.84991,20.995848],[105.858693,20.995146],[105.8713,20.999658],[105.891172,21.018762],[105.898202,21.029084],[105.899717,21.028374],[105.899296,21.027973]]'),
    ('RTE000011', 42.82, 64, '[[105.899296,21.027973],[105.899717,21.028374],[105.900816,21.027858],[105.900134,21.028007],[105.901676,21.027455],[105.900519,21.025316],[105.889883,21.028931],[105.881405,21.030063],[105.865694,21.039298],[105.865512,21.04022],[105.865566,21.039664],[105.854908,21.035479],[105.851487,21.039736],[105.840287,21.05061],[105.841027,21.05122],[105.83948,21.054906],[105.842967,21.056146],[105.843749,21.057018],[105.846523,21.056936],[105.852523,21.046988],[105.854984,21.047876],[105.856103,21.045625],[105.856538,21.044694],[105.854711,21.043984],[105.846523,21.056936],[105.843809,21.057035],[105.842967,21.056146],[105.83948,21.054906],[105.841027,21.05122],[105.839988,21.050747],[105.840508,21.049893],[105.851243,21.03938],[105.854126,21.036106],[105.855924,21.032648],[105.85652,21.031324],[105.85133,21.03176],[105.851395,21.028785],[105.851667,21.02596],[105.843468,21.028342],[105.841049,21.030708],[105.829273,21.032624],[105.829996,21.03217],[105.829731,21.03136],[105.819787,21.023569],[105.809563,21.008869],[105.788912,20.993946],[105.767979,20.981156],[105.769816,20.982215],[105.775288,20.985015],[105.7901,20.994609],[105.808524,21.007905],[105.81107,21.01074],[105.814282,21.008755],[105.818854,21.003474],[105.830047,21.000212],[105.845913,20.996397],[105.857213,20.995059],[105.86734,20.997964],[105.867558,20.99505]]'),
    ('RTE000012', 35.35, 46, '[[105.867553,20.99545],[105.867562,20.996846],[105.865731,20.996833],[105.865451,20.99758],[105.858204,20.995336],[105.849957,20.996128],[105.849377,20.99213],[105.847431,20.98993],[105.846032,20.983729],[105.840925,20.983817],[105.840848,20.978594],[105.84141,20.983469],[105.84172,20.982883],[105.841479,20.983518],[105.847706,20.983561],[105.846032,20.983729],[105.847399,20.98985],[105.849507,20.992632],[105.850572,21.000635],[105.849655,21.000471],[105.850572,21.000635],[105.84991,20.995848],[105.857286,20.995056],[105.860374,20.995551],[105.85876,20.99541],[105.858858,20.996977],[105.85876,20.99541],[105.854164,20.995438],[105.85938,20.995272],[105.871647,20.999912],[105.891172,21.018762],[105.898202,21.029084],[105.900816,21.027858],[105.900134,21.028007],[105.900816,21.027858],[105.898202,21.029084],[105.899456,21.030976],[105.891026,21.018877],[105.871725,21.000147],[105.862726,20.996521],[105.85765,20.995225],[105.84603,20.996602],[105.819284,21.003464],[105.817406,21.004834],[105.814374,21.008864],[105.804938,21.015658],[105.802647,21.013516],[105.800567,21.013278],[105.795587,21.014324],[105.78772,21.027777],[105.775261,21.029865],[105.77814,21.028896]]');

UPDATE seed_route_definition definition
SET planned_distance_km = geometry.planned_distance_km,
    planned_duration_min = geometry.planned_duration_min
FROM seed_route_geometry geometry
WHERE geometry.route_code = definition.route_code;

INSERT INTO public.school_bus_route_planning_session (
    tenant_id, school_id, service_date, route_direction, status,
    total_eligible_students, total_planned_students, total_unassigned_students,
    total_routes, total_stops, total_distance_km, total_duration_min,
    published_at, published_by, planning_notes,
    is_active, is_deleted, created_at, created_by, updated_at, updated_by
)
SELECT
    1,
    school.id,
    definition.service_date,
    definition.route_direction,
    definition.session_status,
    0, 0, 0, 0, 0, 0, 0,
    CASE
        WHEN definition.session_status = 'PUBLISHED'
            THEN (definition.service_date + time '09:00') - interval '3 days'
        ELSE NULL
    END,
    NULL,
    CASE definition.session_status
        WHEN 'PUBLISHED' THEN 'Approved weekday transport plan for regular service.'
        WHEN 'REVIEWING' THEN 'Route allocation is under operational review.'
        ELSE 'Route allocation is being prepared by the transport office.'
    END,
    true, false, CURRENT_TIMESTAMP, 'SEED_DATA', CURRENT_TIMESTAMP, 'SEED_DATA'
FROM (
    SELECT DISTINCT school_code, service_date, route_direction, session_status
    FROM seed_route_definition
) definition
JOIN public.school_bus_school school
  ON school.tenant_id = 1
 AND school.code = definition.school_code
 AND school.is_deleted = false;

INSERT INTO public.school_bus_route_plan (
    tenant_id, school_id, route_code, route_name, service_date, status,
    planned_distance_km, planned_duration_min, planning_notes,
    started_at, completed_at,
    route_direction,
    start_location_type, start_school_id, start_depot_id,
    end_location_type, end_school_id, end_depot_id,
    geometry_path, geometry_source,
    planned_student_count, assigned_bus_capacity, version_no,
    planning_session_id, required_capacity,
    published_at, published_by,
    planned_start_time, planned_end_time, selected_bus_id,
    is_active, is_deleted, created_at, created_by, updated_at, updated_by
)
SELECT
    1,
    school.id,
    definition.route_code,
    definition.route_name,
    definition.service_date,
    definition.route_status,
    definition.planned_distance_km,
    definition.planned_duration_min,
    'Regular corridor planned from active student subscriptions.',
    NULL,
    NULL,
    definition.route_direction,
    CASE WHEN definition.route_direction = 'OUTBOUND' THEN 'DEPOT' ELSE 'SCHOOL' END,
    CASE WHEN definition.route_direction = 'RETURN' THEN school.id ELSE NULL END,
    CASE WHEN definition.route_direction = 'OUTBOUND' THEN depot.id ELSE NULL END,
    CASE WHEN definition.route_direction = 'OUTBOUND' THEN 'SCHOOL' ELSE 'DEPOT' END,
    CASE WHEN definition.route_direction = 'OUTBOUND' THEN school.id ELSE NULL END,
    CASE WHEN definition.route_direction = 'RETURN' THEN depot.id ELSE NULL END,
    geometry.geometry_path,
    'OSRM',
    0,
    NULL,
    1,
    session.id,
    0,
    CASE WHEN definition.session_status = 'PUBLISHED' THEN session.published_at ELSE NULL END,
    NULL,
    definition.planned_start_time,
    definition.planned_end_time,
    NULL,
    true, false, CURRENT_TIMESTAMP, 'SEED_DATA', CURRENT_TIMESTAMP, 'SEED_DATA'
FROM seed_route_definition definition
JOIN seed_route_geometry geometry
  ON geometry.route_code = definition.route_code
JOIN public.school_bus_school school
  ON school.tenant_id = 1
 AND school.code = definition.school_code
 AND school.is_deleted = false
JOIN public.school_bus_depot depot
  ON depot.tenant_id = 1
 AND depot.code = definition.depot_code
 AND depot.is_deleted = false
JOIN public.school_bus_route_planning_session session
  ON session.tenant_id = 1
 AND session.school_id = school.id
 AND session.service_date = definition.service_date
 AND session.route_direction = definition.route_direction
 AND session.created_by = 'SEED_DATA'
 AND session.is_deleted = false;

-- Start terminals.
INSERT INTO public.school_bus_route_stop (
    tenant_id, route_id, pickup_point_id, school_id, depot_id,
    location_type, stop_purpose, stop_order,
    estimated_student_count, planned_boarding_count, planned_dropoff_count,
    estimated_travel_time_from_previous, distance_from_previous_km,
    is_active, is_deleted, created_at, created_by, updated_at, updated_by
)
SELECT
    1,
    route.id,
    NULL,
    CASE WHEN definition.route_direction = 'RETURN' THEN school.id ELSE NULL END,
    CASE WHEN definition.route_direction = 'OUTBOUND' THEN depot.id ELSE NULL END,
    CASE WHEN definition.route_direction = 'OUTBOUND' THEN 'DEPOT' ELSE 'SCHOOL' END,
    'START_TERMINAL',
    1,
    0, 0, 0, 0, 0,
    true, false, CURRENT_TIMESTAMP, 'SEED_DATA', CURRENT_TIMESTAMP, 'SEED_DATA'
FROM seed_route_definition definition
JOIN public.school_bus_route_plan route
  ON route.route_code = definition.route_code
 AND route.created_by = 'SEED_DATA'
 AND route.is_deleted = false
JOIN public.school_bus_school school
  ON school.code = definition.school_code
 AND school.tenant_id = 1
 AND school.is_deleted = false
JOIN public.school_bus_depot depot
  ON depot.code = definition.depot_code
 AND depot.tenant_id = 1
 AND depot.is_deleted = false;

-- Service stops.
INSERT INTO public.school_bus_route_stop (
    tenant_id, route_id, pickup_point_id, school_id, depot_id,
    location_type, stop_purpose, stop_order,
    estimated_student_count, planned_boarding_count, planned_dropoff_count,
    estimated_travel_time_from_previous, distance_from_previous_km,
    is_active, is_deleted, created_at, created_by, updated_at, updated_by
)
SELECT
    1,
    route.id,
    point.id,
    NULL,
    NULL,
    'PICKUP_POINT',
    CASE WHEN definition.route_direction = 'OUTBOUND' THEN 'PICKUP' ELSE 'DROPOFF' END,
    point_order.ordinality::integer + 1,
    0, 0, 0,
    8 + ((point_order.ordinality::integer - 1) % 3),
    2.1 + (point_order.ordinality::integer * 0.35),
    true, false, CURRENT_TIMESTAMP, 'SEED_DATA', CURRENT_TIMESTAMP, 'SEED_DATA'
FROM seed_route_definition definition
JOIN public.school_bus_route_plan route
  ON route.route_code = definition.route_code
 AND route.created_by = 'SEED_DATA'
 AND route.is_deleted = false
CROSS JOIN LATERAL unnest(definition.point_codes) WITH ORDINALITY AS point_order(point_code, ordinality)
JOIN public.school_bus_pickup_point point
  ON point.tenant_id = 1
 AND point.code = point_order.point_code
 AND point.is_deleted = false;

-- End terminals.
INSERT INTO public.school_bus_route_stop (
    tenant_id, route_id, pickup_point_id, school_id, depot_id,
    location_type, stop_purpose, stop_order,
    estimated_student_count, planned_boarding_count, planned_dropoff_count,
    estimated_travel_time_from_previous, distance_from_previous_km,
    is_active, is_deleted, created_at, created_by, updated_at, updated_by
)
SELECT
    1,
    route.id,
    NULL,
    CASE WHEN definition.route_direction = 'OUTBOUND' THEN school.id ELSE NULL END,
    CASE WHEN definition.route_direction = 'RETURN' THEN depot.id ELSE NULL END,
    CASE WHEN definition.route_direction = 'OUTBOUND' THEN 'SCHOOL' ELSE 'DEPOT' END,
    'END_TERMINAL',
    cardinality(definition.point_codes) + 2,
    0, 0, 0, 10, 3.0,
    true, false, CURRENT_TIMESTAMP, 'SEED_DATA', CURRENT_TIMESTAMP, 'SEED_DATA'
FROM seed_route_definition definition
JOIN public.school_bus_route_plan route
  ON route.route_code = definition.route_code
 AND route.created_by = 'SEED_DATA'
 AND route.is_deleted = false
JOIN public.school_bus_school school
  ON school.code = definition.school_code
 AND school.tenant_id = 1
 AND school.is_deleted = false
JOIN public.school_bus_depot depot
  ON depot.code = definition.depot_code
 AND depot.tenant_id = 1
 AND depot.is_deleted = false;

INSERT INTO public.school_bus_route_plan_student (
    tenant_id, route_id, student_id, subscription_id,
    pickup_stop_id, dropoff_stop_id,
    is_active, is_deleted, created_at, created_by, updated_at, updated_by
)
SELECT
    1,
    route.id,
    subscription.student_id,
    subscription.id,
    CASE
        WHEN definition.route_direction = 'OUTBOUND' THEN service_stop.id
        ELSE start_terminal.id
    END,
    CASE
        WHEN definition.route_direction = 'OUTBOUND' THEN end_terminal.id
        ELSE service_stop.id
    END,
    true, false, CURRENT_TIMESTAMP, 'SEED_DATA', CURRENT_TIMESTAMP, 'SEED_DATA'
FROM seed_route_definition definition
JOIN public.school_bus_route_plan route
  ON route.route_code = definition.route_code
 AND route.created_by = 'SEED_DATA'
 AND route.is_deleted = false
JOIN public.school_bus_school school
  ON school.code = definition.school_code
 AND school.tenant_id = 1
 AND school.is_deleted = false
JOIN public.school_bus_student_subscription subscription
  ON subscription.tenant_id = 1
 AND subscription.school_id = school.id
 AND subscription.status = 'ACTIVE'
 AND subscription.is_active = true
 AND subscription.is_deleted = false
 AND subscription.effective_from <= definition.service_date
 AND (subscription.effective_to IS NULL OR subscription.effective_to >= definition.service_date)
 AND (
     (definition.service_date = date '2026-06-22' AND subscription.is_monday = true)
     OR
     (definition.service_date = date '2026-06-23' AND subscription.is_tuesday = true)
 )
 AND (
     (definition.route_direction = 'OUTBOUND' AND subscription.trip_option IN ('MORNING', 'ROUND_TRIP'))
     OR
     (definition.route_direction = 'RETURN' AND subscription.trip_option IN ('AFTERNOON', 'ROUND_TRIP'))
 )
JOIN public.school_bus_pickup_point service_point
  ON service_point.id = CASE
      WHEN definition.route_direction = 'OUTBOUND' THEN subscription.pickup_point_id
      ELSE subscription.dropoff_point_id
  END
 AND service_point.code = ANY(definition.point_codes)
 AND service_point.is_deleted = false
JOIN public.school_bus_route_stop service_stop
  ON service_stop.route_id = route.id
 AND service_stop.pickup_point_id = service_point.id
 AND service_stop.location_type = 'PICKUP_POINT'
 AND service_stop.is_deleted = false
JOIN public.school_bus_route_stop start_terminal
  ON start_terminal.route_id = route.id
 AND start_terminal.stop_purpose = 'START_TERMINAL'
 AND start_terminal.is_deleted = false
JOIN public.school_bus_route_stop end_terminal
  ON end_terminal.route_id = route.id
 AND end_terminal.stop_purpose = 'END_TERMINAL'
 AND end_terminal.is_deleted = false;

UPDATE public.school_bus_route_stop stop
SET estimated_student_count = counts.student_count,
    planned_boarding_count = CASE
        WHEN stop.stop_purpose = 'PICKUP' THEN counts.student_count
        WHEN stop.stop_purpose = 'START_TERMINAL' AND counts.route_direction = 'RETURN'
            THEN counts.route_total
        ELSE 0
    END,
    planned_dropoff_count = CASE
        WHEN stop.stop_purpose = 'DROPOFF' THEN counts.student_count
        WHEN stop.stop_purpose = 'END_TERMINAL' AND counts.route_direction = 'OUTBOUND'
            THEN counts.route_total
        ELSE 0
    END,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SEED_DATA'
FROM (
    SELECT
        stop_inner.id AS stop_id,
        route.route_direction,
        CASE
            WHEN stop_inner.stop_purpose = 'PICKUP'
                THEN count(student.id) FILTER (WHERE student.pickup_stop_id = stop_inner.id)
            WHEN stop_inner.stop_purpose = 'DROPOFF'
                THEN count(student.id) FILTER (WHERE student.dropoff_stop_id = stop_inner.id)
            WHEN stop_inner.stop_purpose = 'START_TERMINAL' AND route.route_direction = 'RETURN'
                THEN count(student.id)
            WHEN stop_inner.stop_purpose = 'END_TERMINAL' AND route.route_direction = 'OUTBOUND'
                THEN count(student.id)
            ELSE 0
        END::integer AS student_count,
        count(student.id)::integer AS route_total
    FROM public.school_bus_route_stop stop_inner
    JOIN public.school_bus_route_plan route
      ON route.id = stop_inner.route_id
     AND route.created_by = 'SEED_DATA'
     AND route.is_deleted = false
    LEFT JOIN public.school_bus_route_plan_student student
      ON student.route_id = route.id
     AND student.created_by = 'SEED_DATA'
     AND student.is_deleted = false
    WHERE stop_inner.created_by = 'SEED_DATA'
      AND stop_inner.is_deleted = false
    GROUP BY stop_inner.id, route.route_direction, stop_inner.stop_purpose
) counts
WHERE stop.id = counts.stop_id;

UPDATE public.school_bus_route_plan route
SET planned_student_count = counts.student_count,
    required_capacity = counts.student_count,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SEED_DATA'
FROM (
    SELECT
        route_inner.id AS route_id,
        count(DISTINCT student.id)::integer AS student_count
    FROM public.school_bus_route_plan route_inner
    LEFT JOIN public.school_bus_route_plan_student student
      ON student.route_id = route_inner.id
     AND student.is_deleted = false
    WHERE route_inner.created_by = 'SEED_DATA'
      AND route_inner.is_deleted = false
    GROUP BY route_inner.id
) counts
WHERE route.id = counts.route_id;

WITH target_routes AS (
    SELECT
        route.id AS route_id,
        route.planned_student_count,
        definition.trip_code,
        row_number() OVER (ORDER BY definition.trip_code) AS resource_rank
    FROM seed_route_definition definition
    JOIN public.school_bus_route_plan route
      ON route.route_code = definition.route_code
     AND route.created_by = 'SEED_DATA'
     AND route.is_deleted = false
    WHERE definition.trip_code IS NOT NULL
),
available_buses AS (
    SELECT
        bus.id,
        bus.capacity,
        row_number() OVER (ORDER BY bus.capacity, bus.plate_number) AS resource_rank
    FROM public.school_bus_bus bus
    WHERE bus.tenant_id = 1
      AND bus.status = 'AVAILABLE'
      AND bus.is_active = true
      AND bus.is_deleted = false
),
available_drivers AS (
    SELECT
        driver.id,
        row_number() OVER (ORDER BY driver.id) AS resource_rank
    FROM public.school_bus_driver_profile driver
    WHERE driver.tenant_id = 1
      AND driver.status = 'AVAILABLE'
      AND driver.is_active = true
      AND driver.is_deleted = false
),
available_attendants AS (
    SELECT
        attendant.id,
        row_number() OVER (ORDER BY attendant.id) AS resource_rank
    FROM public.school_bus_attendant_profile attendant
    WHERE attendant.tenant_id = 1
      AND attendant.status = 'AVAILABLE'
      AND attendant.is_active = true
      AND attendant.is_deleted = false
)
INSERT INTO public.school_bus_route_assignment (
    tenant_id, route_id, bus_id, driver_id, attendant_id,
    assigned_at, status, assigned_by, confirmed_at, cancelled_at, assignment_note,
    is_active, is_deleted, created_at, created_by, updated_at, updated_by
)
SELECT
    1,
    target.route_id,
    bus.id,
    driver.id,
    attendant.id,
    (route.service_date + route.planned_start_time) - interval '3 days',
    'CONFIRMED',
    NULL,
    (route.service_date + route.planned_start_time) - interval '2 days',
    NULL,
    'Vehicle crew confirmed for the scheduled corridor.',
    true, false, CURRENT_TIMESTAMP, 'SEED_DATA', CURRENT_TIMESTAMP, 'SEED_DATA'
FROM target_routes target
JOIN public.school_bus_route_plan route ON route.id = target.route_id
JOIN available_buses bus
  ON bus.resource_rank = target.resource_rank
 AND bus.capacity >= target.planned_student_count
JOIN available_drivers driver ON driver.resource_rank = target.resource_rank
JOIN available_attendants attendant ON attendant.resource_rank = target.resource_rank;

UPDATE public.school_bus_route_plan route
SET selected_bus_id = assignment.bus_id,
    assigned_bus_capacity = bus.capacity,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SEED_DATA'
FROM public.school_bus_route_assignment assignment
JOIN public.school_bus_bus bus ON bus.id = assignment.bus_id
WHERE assignment.route_id = route.id
  AND assignment.created_by = 'SEED_DATA'
  AND assignment.is_deleted = false;

INSERT INTO public.school_bus_trip_execution (
    tenant_id, trip_code, route_id, service_date, route_direction, status,
    planned_start_at, planned_end_at, started_at, completed_at,
    planned_distance_km, planned_duration_min,
    actual_distance_km, actual_duration_min, completion_note,
    bus_id, driver_id, attendant_id, route_geometry_path,
    start_location_type, start_school_id, start_depot_id,
    end_location_type, end_school_id, end_depot_id,
    cancelled_at, cancelled_by, cancellation_reason,
    is_active, is_deleted, created_at, created_by, updated_at, updated_by
)
SELECT
    1,
    definition.trip_code,
    route.id,
    route.service_date,
    route.route_direction,
    definition.trip_status,
    (route.service_date + route.planned_start_time),
    (route.service_date + route.planned_end_time),
    CASE
        WHEN definition.trip_status IN ('IN_PROGRESS', 'COMPLETED')
            THEN (route.service_date + route.planned_start_time) + interval '4 minutes'
        ELSE NULL
    END,
    CASE
        WHEN definition.trip_status = 'COMPLETED'
            THEN (route.service_date + route.planned_end_time) + interval '3 minutes'
        ELSE NULL
    END,
    route.planned_distance_km,
    route.planned_duration_min,
    CASE
        WHEN definition.trip_status = 'COMPLETED' THEN route.planned_distance_km + 0.3
        WHEN definition.trip_status = 'IN_PROGRESS' THEN round((route.planned_distance_km * 0.24)::numeric, 1)::double precision
        ELSE NULL
    END,
    CASE
        WHEN definition.trip_status = 'COMPLETED' THEN route.planned_duration_min + 3
        WHEN definition.trip_status = 'IN_PROGRESS' THEN 18
        ELSE NULL
    END,
    CASE
        WHEN definition.trip_status = 'COMPLETED' THEN 'Trip completed according to the approved route plan.'
        ELSE NULL
    END,
    assignment.bus_id,
    assignment.driver_id,
    assignment.attendant_id,
    route.geometry_path,
    route.start_location_type,
    route.start_school_id,
    route.start_depot_id,
    route.end_location_type,
    route.end_school_id,
    route.end_depot_id,
    CASE
        WHEN definition.trip_status = 'CANCELLED'
            THEN (route.service_date + route.planned_start_time) - interval '30 minutes'
        ELSE NULL
    END,
    NULL,
    CASE
        WHEN definition.trip_status = 'CANCELLED'
            THEN 'Service suspended because the assigned vehicle required an immediate safety inspection.'
        ELSE NULL
    END,
    true, false, CURRENT_TIMESTAMP, 'SEED_DATA', CURRENT_TIMESTAMP, 'SEED_DATA'
FROM seed_route_definition definition
JOIN public.school_bus_route_plan route
  ON route.route_code = definition.route_code
 AND route.created_by = 'SEED_DATA'
 AND route.is_deleted = false
JOIN public.school_bus_route_assignment assignment
  ON assignment.route_id = route.id
 AND assignment.created_by = 'SEED_DATA'
 AND assignment.is_deleted = false
WHERE definition.trip_code IS NOT NULL;

INSERT INTO public.school_bus_trip_student (
    tenant_id, trip_id, student_id, pickup_stop_id, dropoff_stop_id,
    subscription_id, status, note,
    is_active, is_deleted, created_at, created_by, updated_at, updated_by
)
SELECT
    1,
    trip.id,
    route_student.student_id,
    route_student.pickup_stop_id,
    route_student.dropoff_stop_id,
    route_student.subscription_id,
    'PLANNED',
    NULL,
    true, false, CURRENT_TIMESTAMP, 'SEED_DATA', CURRENT_TIMESTAMP, 'SEED_DATA'
FROM public.school_bus_trip_execution trip
JOIN public.school_bus_route_plan_student route_student
  ON route_student.route_id = trip.route_id
 AND route_student.created_by = 'SEED_DATA'
 AND route_student.is_deleted = false
WHERE trip.created_by = 'SEED_DATA'
  AND trip.is_deleted = false;

WITH ranked_students AS (
    SELECT
        trip_student.id,
        trip.trip_code,
        service_stop.stop_order AS service_stop_order,
        row_number() OVER (
            PARTITION BY trip_student.trip_id
            ORDER BY service_stop.stop_order, student.student_code
        ) AS trip_rank,
        count(*) OVER (PARTITION BY trip_student.trip_id) AS trip_count,
        row_number() OVER (
            PARTITION BY trip_student.trip_id,
                         CASE
                             WHEN trip.route_direction = 'OUTBOUND' THEN trip_student.pickup_stop_id
                             ELSE trip_student.dropoff_stop_id
                         END
            ORDER BY student.student_code
        ) AS stop_rank
    FROM public.school_bus_trip_student trip_student
    JOIN public.school_bus_trip_execution trip ON trip.id = trip_student.trip_id
    JOIN public.school_bus_student student ON student.id = trip_student.student_id
    JOIN public.school_bus_route_stop service_stop
      ON service_stop.id = CASE
          WHEN trip.route_direction = 'OUTBOUND' THEN trip_student.pickup_stop_id
          ELSE trip_student.dropoff_stop_id
      END
    WHERE trip_student.created_by = 'SEED_DATA'
      AND trip_student.is_deleted = false
)
UPDATE public.school_bus_trip_student trip_student
SET status = CASE
        WHEN ranked.trip_code = 'TRP000002' AND ranked.trip_rank = ranked.trip_count THEN 'ABSENT'
        WHEN ranked.trip_code = 'TRP000004' AND ranked.trip_rank = ranked.trip_count THEN 'NO_SHOW'
        WHEN ranked.trip_code = 'TRP000006' AND ranked.trip_rank = ranked.trip_count THEN 'ABSENT'
        WHEN ranked.trip_code IN ('TRP000002', 'TRP000004', 'TRP000006') THEN 'DROPPED_OFF'
        WHEN ranked.trip_code = 'TRP000003'
             AND ranked.service_stop_order = 2
             AND ranked.stop_rank <= 2 THEN 'BOARDED'
        WHEN ranked.trip_code = 'TRP000003'
             AND ranked.service_stop_order = 2 THEN 'ABSENT'
        WHEN ranked.trip_code = 'TRP000007'
             AND ranked.service_stop_order = 2
             AND ranked.stop_rank = 1 THEN 'BOARDED'
        WHEN ranked.trip_code = 'TRP000007'
             AND ranked.service_stop_order = 2 THEN 'NO_SHOW'
        WHEN ranked.trip_code = 'TRP000008' THEN 'NOT_SERVED'
        ELSE 'PLANNED'
    END,
    note = CASE
        WHEN ranked.trip_code IN ('TRP000002', 'TRP000006')
             AND ranked.trip_rank = ranked.trip_count
            THEN 'Student was absent when boarding closed.'
        WHEN ranked.trip_code = 'TRP000004' AND ranked.trip_rank = ranked.trip_count
            THEN 'Student did not arrive before departure.'
        WHEN ranked.trip_code = 'TRP000003'
             AND ranked.service_stop_order = 2
             AND ranked.stop_rank > 2
            THEN 'Student was marked absent at the active pickup stop.'
        WHEN ranked.trip_code = 'TRP000007'
             AND ranked.service_stop_order = 2
             AND ranked.stop_rank > 1
            THEN 'Student did not arrive before the first pickup departed.'
        WHEN ranked.trip_code = 'TRP000008'
            THEN 'Trip cancelled before service began.'
        ELSE NULL
    END,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SEED_DATA'
FROM ranked_students ranked
WHERE trip_student.id = ranked.id;

INSERT INTO public.school_bus_trip_stop_log (
    tenant_id, trip_id, route_stop_id, stop_order, status,
    actual_arrival_time, actual_departure_time, delay_minutes,
    actual_boarded_count, actual_dropped_count, note,
    is_active, is_deleted, created_at, created_by, updated_at, updated_by
)
SELECT
    1,
    trip.id,
    stop.id,
    stop.stop_order,
    CASE
        WHEN trip.status IN ('ASSIGNED', 'CANCELLED') THEN 'PENDING'
        WHEN trip.status = 'COMPLETED' AND stop.stop_purpose = 'END_TERMINAL' THEN 'ARRIVED'
        WHEN trip.status = 'COMPLETED' THEN 'DEPARTED'
        WHEN trip.trip_code = 'TRP000003' AND stop.stop_order = 1 THEN 'DEPARTED'
        WHEN trip.trip_code = 'TRP000003' AND stop.stop_order = 2 THEN 'BOARDING'
        WHEN trip.trip_code = 'TRP000003' THEN 'PENDING'
        WHEN trip.trip_code = 'TRP000007' AND stop.stop_order <= 2 THEN 'DEPARTED'
        ELSE 'PENDING'
    END,
    CASE
        WHEN trip.status = 'COMPLETED'
            THEN trip.started_at + make_interval(mins => (stop.stop_order - 1) * 9)
        WHEN trip.trip_code = 'TRP000003' AND stop.stop_order <= 2
            THEN trip.started_at + make_interval(mins => (stop.stop_order - 1) * 10)
        WHEN trip.trip_code = 'TRP000007' AND stop.stop_order <= 2
            THEN trip.started_at + make_interval(mins => (stop.stop_order - 1) * 11)
        ELSE NULL
    END,
    CASE
        WHEN trip.status = 'COMPLETED' AND stop.stop_purpose <> 'END_TERMINAL'
            THEN trip.started_at + make_interval(mins => (stop.stop_order - 1) * 9 + 3)
        WHEN trip.trip_code = 'TRP000003' AND stop.stop_order = 1
            THEN trip.started_at + interval '3 minutes'
        WHEN trip.trip_code = 'TRP000007' AND stop.stop_order <= 2
            THEN trip.started_at + make_interval(mins => (stop.stop_order - 1) * 11 + 3)
        ELSE NULL
    END,
    CASE WHEN trip.status IN ('COMPLETED', 'IN_PROGRESS') THEN stop.stop_order % 3 ELSE NULL END,
    CASE
        WHEN trip.route_direction = 'OUTBOUND' AND stop.stop_purpose = 'PICKUP'
        THEN (
            SELECT count(*)::integer
            FROM public.school_bus_trip_student student
            WHERE student.trip_id = trip.id
              AND student.pickup_stop_id = stop.id
              AND student.status IN ('BOARDED', 'DROPPED_OFF')
              AND student.is_deleted = false
        )
        WHEN trip.route_direction = 'RETURN' AND stop.stop_purpose = 'START_TERMINAL'
        THEN (
            SELECT count(*)::integer
            FROM public.school_bus_trip_student student
            WHERE student.trip_id = trip.id
              AND student.status IN ('BOARDED', 'DROPPED_OFF')
              AND student.is_deleted = false
        )
        ELSE 0
    END,
    CASE
        WHEN trip.route_direction = 'OUTBOUND' AND stop.stop_purpose = 'END_TERMINAL'
        THEN (
            SELECT count(*)::integer
            FROM public.school_bus_trip_student student
            WHERE student.trip_id = trip.id
              AND student.status = 'DROPPED_OFF'
              AND student.is_deleted = false
        )
        WHEN trip.route_direction = 'RETURN' AND stop.stop_purpose = 'DROPOFF'
        THEN (
            SELECT count(*)::integer
            FROM public.school_bus_trip_student student
            WHERE student.trip_id = trip.id
              AND student.dropoff_stop_id = stop.id
              AND student.status = 'DROPPED_OFF'
              AND student.is_deleted = false
        )
        ELSE 0
    END,
    CASE
        WHEN trip.status = 'CANCELLED' THEN 'Trip was cancelled before departure.'
        WHEN trip.trip_code = 'TRP000003' AND stop.stop_order = 2 THEN 'Boarding is in progress.'
        WHEN trip.trip_code = 'TRP000007' AND stop.stop_order = 2 THEN 'First pickup completed; vehicle is moving to the next stop.'
        ELSE NULL
    END,
    true, false, CURRENT_TIMESTAMP, 'SEED_DATA', CURRENT_TIMESTAMP, 'SEED_DATA'
FROM public.school_bus_trip_execution trip
JOIN public.school_bus_route_stop stop
  ON stop.route_id = trip.route_id
 AND stop.created_by = 'SEED_DATA'
 AND stop.is_deleted = false
WHERE trip.created_by = 'SEED_DATA'
  AND trip.is_deleted = false;

-- Boarding events for students who boarded or completed the trip.
INSERT INTO public.school_bus_attendance (
    tenant_id, route_id, student_id, trip_id, route_stop_id,
    attendance_type, status, event_type, event_source,
    recorded_at, recorded_by, notes,
    is_active, is_deleted, created_at, created_by, updated_at, updated_by
)
SELECT
    1,
    trip.route_id,
    student.student_id,
    trip.id,
    student.pickup_stop_id,
    'CHECKED_IN',
    'PRESENT',
    'BOARDED',
    'MANUAL',
    trip.started_at + make_interval(mins => pickup_stop.stop_order * 7),
    COALESCE(trip.attendant_id, trip.driver_id),
    'Boarding confirmed by the assigned crew.',
    true, false, CURRENT_TIMESTAMP, 'SEED_DATA', CURRENT_TIMESTAMP, 'SEED_DATA'
FROM public.school_bus_trip_student student
JOIN public.school_bus_trip_execution trip ON trip.id = student.trip_id
JOIN public.school_bus_route_stop pickup_stop ON pickup_stop.id = student.pickup_stop_id
WHERE student.created_by = 'SEED_DATA'
  AND student.status IN ('BOARDED', 'DROPPED_OFF')
  AND student.is_deleted = false;

-- Drop-off events for successfully completed students.
INSERT INTO public.school_bus_attendance (
    tenant_id, route_id, student_id, trip_id, route_stop_id,
    attendance_type, status, event_type, event_source,
    recorded_at, recorded_by, notes,
    is_active, is_deleted, created_at, created_by, updated_at, updated_by
)
SELECT
    1,
    trip.route_id,
    student.student_id,
    trip.id,
    student.dropoff_stop_id,
    'CHECKED_OUT',
    'PRESENT',
    'DROPPED_OFF',
    'MANUAL',
    CASE
        WHEN trip.route_direction = 'OUTBOUND' THEN trip.completed_at - interval '2 minutes'
        ELSE trip.started_at + make_interval(mins => dropoff_stop.stop_order * 9)
    END,
    COALESCE(trip.attendant_id, trip.driver_id),
    'Drop-off confirmed by the assigned crew.',
    true, false, CURRENT_TIMESTAMP, 'SEED_DATA', CURRENT_TIMESTAMP, 'SEED_DATA'
FROM public.school_bus_trip_student student
JOIN public.school_bus_trip_execution trip ON trip.id = student.trip_id
JOIN public.school_bus_route_stop dropoff_stop ON dropoff_stop.id = student.dropoff_stop_id
WHERE student.created_by = 'SEED_DATA'
  AND student.status = 'DROPPED_OFF'
  AND student.is_deleted = false;

-- Absence, no-show, and cancelled-service events.
INSERT INTO public.school_bus_attendance (
    tenant_id, route_id, student_id, trip_id, route_stop_id,
    attendance_type, status, event_type, event_source,
    recorded_at, recorded_by, notes,
    is_active, is_deleted, created_at, created_by, updated_at, updated_by
)
SELECT
    1,
    trip.route_id,
    student.student_id,
    trip.id,
    CASE
        WHEN student.status = 'NOT_SERVED' AND trip.route_direction = 'RETURN'
            THEN student.dropoff_stop_id
        ELSE student.pickup_stop_id
    END,
    'CHECKED_IN',
    'ABSENT',
    student.status,
    CASE WHEN student.status = 'NOT_SERVED' THEN 'SYSTEM' ELSE 'MANUAL' END,
    CASE
        WHEN student.status = 'NOT_SERVED' THEN trip.cancelled_at
        ELSE trip.started_at + interval '14 minutes'
    END,
    COALESCE(trip.attendant_id, trip.driver_id),
    student.note,
    true, false, CURRENT_TIMESTAMP, 'SEED_DATA', CURRENT_TIMESTAMP, 'SEED_DATA'
FROM public.school_bus_trip_student student
JOIN public.school_bus_trip_execution trip ON trip.id = student.trip_id
WHERE student.created_by = 'SEED_DATA'
  AND student.status IN ('ABSENT', 'NO_SHOW', 'NOT_SERVED')
  AND student.is_deleted = false;

UPDATE public.school_bus_route_planning_session session
SET total_eligible_students = summary.eligible_students,
    total_planned_students = summary.planned_students,
    total_unassigned_students = GREATEST(summary.eligible_students - summary.planned_students, 0),
    total_routes = summary.route_count,
    total_stops = summary.stop_count,
    total_distance_km = summary.total_distance_km,
    total_duration_min = summary.total_duration_min,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SEED_DATA'
FROM (
    SELECT
        session_inner.id AS session_id,
        (
            SELECT count(*)::integer
            FROM public.school_bus_student_subscription subscription
            WHERE subscription.tenant_id = session_inner.tenant_id
              AND subscription.school_id = session_inner.school_id
              AND subscription.status = 'ACTIVE'
              AND subscription.is_active = true
              AND subscription.is_deleted = false
              AND subscription.effective_from <= session_inner.service_date
              AND (subscription.effective_to IS NULL OR subscription.effective_to >= session_inner.service_date)
              AND (
                  (extract(isodow FROM session_inner.service_date) = 1 AND subscription.is_monday)
                  OR (extract(isodow FROM session_inner.service_date) = 2 AND subscription.is_tuesday)
              )
              AND (
                  (session_inner.route_direction = 'OUTBOUND'
                      AND subscription.trip_option IN ('MORNING', 'ROUND_TRIP'))
                  OR
                  (session_inner.route_direction = 'RETURN'
                      AND subscription.trip_option IN ('AFTERNOON', 'ROUND_TRIP'))
              )
        ) AS eligible_students,
        (
            SELECT count(*)::integer
            FROM public.school_bus_route_plan_student route_student
            JOIN public.school_bus_route_plan route
              ON route.id = route_student.route_id
             AND route.is_deleted = false
            WHERE route.planning_session_id = session_inner.id
              AND route_student.is_deleted = false
        ) AS planned_students,
        (
            SELECT count(*)::integer
            FROM public.school_bus_route_plan route
            WHERE route.planning_session_id = session_inner.id
              AND route.is_deleted = false
        ) AS route_count,
        (
            SELECT count(*)::integer
            FROM public.school_bus_route_stop stop
            JOIN public.school_bus_route_plan route
              ON route.id = stop.route_id
             AND route.is_deleted = false
            WHERE route.planning_session_id = session_inner.id
              AND stop.is_deleted = false
        ) AS stop_count,
        (
            SELECT COALESCE(sum(route.planned_distance_km), 0)::double precision
            FROM public.school_bus_route_plan route
            WHERE route.planning_session_id = session_inner.id
              AND route.is_deleted = false
        ) AS total_distance_km,
        (
            SELECT COALESCE(sum(route.planned_duration_min), 0)::integer
            FROM public.school_bus_route_plan route
            WHERE route.planning_session_id = session_inner.id
              AND route.is_deleted = false
        ) AS total_duration_min
    FROM public.school_bus_route_planning_session session_inner
    WHERE session_inner.created_by = 'SEED_DATA'
      AND session_inner.is_deleted = false
) summary
WHERE session.id = summary.session_id;

DROP TABLE seed_route_definition;

DO $$
DECLARE
    v_sequence_key text;
    v_expected_next bigint;
    v_keeper_id bigint;
    v_max_next bigint;
    v_non_seed_count integer;
BEGIN
    FOR v_sequence_key, v_expected_next IN
        SELECT *
        FROM (VALUES ('ROUTE'::text, 13::bigint), ('TRIP'::text, 9::bigint))
            AS expected(sequence_key, next_value)
    LOOP
        SELECT
            count(*) FILTER (WHERE created_by IS DISTINCT FROM 'SEED_DATA'),
            max(next_value)
        INTO v_non_seed_count, v_max_next
        FROM public.school_bus_code_sequence
        WHERE tenant_id = 1
          AND sequence_key = v_sequence_key
          AND is_deleted = false;

        IF v_non_seed_count > 1 THEN
            RAISE EXCEPTION
                'Multiple non-seed active code sequence rows detected for tenant 1, key %',
                v_sequence_key;
        END IF;

        SELECT id
        INTO v_keeper_id
        FROM public.school_bus_code_sequence
        WHERE tenant_id = 1
          AND sequence_key = v_sequence_key
          AND is_deleted = false
        ORDER BY
            CASE WHEN created_by IS DISTINCT FROM 'SEED_DATA' THEN 0 ELSE 1 END,
            id
        LIMIT 1;

        IF v_keeper_id IS NULL THEN
            INSERT INTO public.school_bus_code_sequence (
                tenant_id, sequence_key, next_value,
                is_active, is_deleted, created_at, created_by, updated_at, updated_by
            )
            VALUES (
                1, v_sequence_key, v_expected_next,
                true, false, CURRENT_TIMESTAMP, 'SEED_DATA', CURRENT_TIMESTAMP, 'SEED_DATA'
            );
        ELSE
            UPDATE public.school_bus_code_sequence
            SET next_value = GREATEST(COALESCE(v_max_next, 0), v_expected_next),
                is_active = true,
                updated_at = CURRENT_TIMESTAMP,
                updated_by = 'SEED_DATA'
            WHERE id = v_keeper_id;

            DELETE FROM public.school_bus_code_sequence
            WHERE tenant_id = 1
              AND sequence_key = v_sequence_key
              AND is_deleted = false
              AND id <> v_keeper_id
              AND created_by = 'SEED_DATA';
        END IF;
    END LOOP;
END $$;

CREATE OR REPLACE FUNCTION pg_temp.seed_haversine_km(
    lon1 double precision,
    lat1 double precision,
    lon2 double precision,
    lat2 double precision
)
RETURNS double precision
LANGUAGE sql
IMMUTABLE
STRICT
AS $function$
    SELECT 6371 * 2 * asin(
        sqrt(
            least(
                1,
                power(sin(radians(($4 - $2) / 2)), 2)
                + cos(radians($2)) * cos(radians($4))
                * power(sin(radians(($3 - $1) / 2)), 2)
            )
        )
    );
$function$;

DROP VIEW IF EXISTS seed_route_waypoint_match;

CREATE TEMP VIEW seed_route_waypoint_match AS
SELECT
    route.route_code,
    stop.id AS route_stop_id,
    stop.stop_order,
    stop.location_type,
    stop.stop_purpose,
    coalesce(point.name, school.name, depot.name) AS waypoint_name,
    nearest.geometry_index,
    nearest.distance_km
FROM public.school_bus_route_plan route
JOIN public.school_bus_route_stop stop
  ON stop.route_id = route.id
 AND stop.is_deleted = false
LEFT JOIN public.school_bus_pickup_point point
  ON point.id = stop.pickup_point_id
LEFT JOIN public.school_bus_school school
  ON school.id = stop.school_id
LEFT JOIN public.school_bus_depot depot
  ON depot.id = stop.depot_id
CROSS JOIN LATERAL (
    SELECT
        geometry_point.ordinality::integer AS geometry_index,
        pg_temp.seed_haversine_km(
            coalesce(point.longitude, school.longitude, depot.longitude),
            coalesce(point.latitude, school.latitude, depot.latitude),
            (geometry_point.coordinate ->> 0)::double precision,
            (geometry_point.coordinate ->> 1)::double precision
        ) AS distance_km
    FROM jsonb_array_elements(route.geometry_path::jsonb)
        WITH ORDINALITY AS geometry_point(coordinate, ordinality)
    ORDER BY distance_km, geometry_point.ordinality
    LIMIT 1
) nearest
WHERE route.created_by = 'SEED_DATA'
  AND route.is_deleted = false;

DO $$
DECLARE
    v_seed_by CONSTANT varchar(100) := 'SEED_DATA';
    v_sessions integer;
    v_routes integer;
    v_stops integer;
    v_route_students integer;
    v_assignments integer;
    v_trips integer;
    v_trip_logs integer;
    v_trip_students integer;
    v_attendance integer;
BEGIN
    SELECT count(*) INTO v_sessions
    FROM public.school_bus_route_planning_session
    WHERE created_by = v_seed_by AND is_deleted = false;
    SELECT count(*) INTO v_routes
    FROM public.school_bus_route_plan
    WHERE created_by = v_seed_by AND is_deleted = false;
    SELECT count(*) INTO v_stops
    FROM public.school_bus_route_stop
    WHERE created_by = v_seed_by AND is_deleted = false;
    SELECT count(*) INTO v_route_students
    FROM public.school_bus_route_plan_student
    WHERE created_by = v_seed_by AND is_deleted = false;
    SELECT count(*) INTO v_assignments
    FROM public.school_bus_route_assignment
    WHERE created_by = v_seed_by AND is_deleted = false;
    SELECT count(*) INTO v_trips
    FROM public.school_bus_trip_execution
    WHERE created_by = v_seed_by AND is_deleted = false;
    SELECT count(*) INTO v_trip_logs
    FROM public.school_bus_trip_stop_log
    WHERE created_by = v_seed_by AND is_deleted = false;
    SELECT count(*) INTO v_trip_students
    FROM public.school_bus_trip_student
    WHERE created_by = v_seed_by AND is_deleted = false;
    SELECT count(*) INTO v_attendance
    FROM public.school_bus_attendance
    WHERE created_by = v_seed_by AND is_deleted = false;

    IF v_sessions <> 8
       OR v_routes <> 12
       OR v_stops <> 75
       OR v_route_students <> 96
       OR v_assignments <> 8
       OR v_trips <> 8
       OR v_trip_logs <> 51
       OR v_trip_students <> 75
       OR v_attendance <> 76
    THEN
        RAISE EXCEPTION
            'Operational seed verification failed: sessions=%, routes=%, stops=%, route_students=%, assignments=%, trips=%, trip_logs=%, trip_students=%, attendance=%',
            v_sessions, v_routes, v_stops, v_route_students, v_assignments,
            v_trips, v_trip_logs, v_trip_students, v_attendance;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_route_plan route
        LEFT JOIN public.school_bus_route_plan_student route_student
          ON route_student.route_id = route.id
         AND route_student.is_deleted = false
        WHERE route.created_by = v_seed_by
          AND route.is_deleted = false
        GROUP BY route.id, route.route_code, route.planned_student_count
        HAVING route.planned_student_count <> count(route_student.id)
    ) THEN
        RAISE EXCEPTION
            'Seed validation failed: planned_student_count does not match route_plan_student count';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_route_plan_student route_student
        JOIN public.school_bus_route_plan route ON route.id = route_student.route_id
        WHERE route_student.created_by = v_seed_by
          AND route_student.is_deleted = false
          AND route.is_deleted = false
        GROUP BY
            route.school_id,
            route.service_date,
            route.route_direction,
            route_student.subscription_id
        HAVING count(*) > 1
    ) THEN
        RAISE EXCEPTION
            'Seed validation failed: duplicate subscription assignment in the same planning context';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_trip_execution trip
        LEFT JOIN public.school_bus_trip_stop_log stop_log
          ON stop_log.trip_id = trip.id
         AND stop_log.is_deleted = false
        LEFT JOIN public.school_bus_trip_student trip_student
          ON trip_student.trip_id = trip.id
         AND trip_student.is_deleted = false
        WHERE trip.created_by = v_seed_by
          AND trip.is_deleted = false
        GROUP BY trip.id, trip.trip_code
        HAVING count(DISTINCT stop_log.id) = 0
            OR count(DISTINCT trip_student.id) = 0
    ) THEN
        RAISE EXCEPTION
            'Seed validation failed: each trip must have stop logs and students';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_trip_execution trip
        JOIN public.school_bus_trip_student trip_student
          ON trip_student.trip_id = trip.id
        WHERE trip.created_by = v_seed_by
          AND trip.status = 'COMPLETED'
          AND trip_student.status = 'PLANNED'
          AND trip_student.is_deleted = false
    ) THEN
        RAISE EXCEPTION
            'Seed validation failed: completed trips still have PLANNED students';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_route_stop
        WHERE created_by = v_seed_by
          AND stop_purpose = 'SERVICE_STOP'
          AND is_deleted = false
    ) THEN
        RAISE EXCEPTION
            'Seed validation failed: SERVICE_STOP is not a valid stop_purpose';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_route_plan route
        JOIN LATERAL (
            SELECT stop.location_type, stop.stop_purpose
            FROM public.school_bus_route_stop stop
            WHERE stop.route_id = route.id
              AND stop.is_deleted = false
            ORDER BY stop.stop_order
            LIMIT 1
        ) first_stop ON true
        JOIN LATERAL (
            SELECT stop.location_type, stop.stop_purpose
            FROM public.school_bus_route_stop stop
            WHERE stop.route_id = route.id
              AND stop.is_deleted = false
            ORDER BY stop.stop_order DESC
            LIMIT 1
        ) last_stop ON true
        WHERE route.created_by = v_seed_by
          AND route.route_direction = 'OUTBOUND'
          AND route.is_deleted = false
          AND (
              first_stop.location_type <> 'DEPOT'
              OR first_stop.stop_purpose <> 'START_TERMINAL'
              OR last_stop.location_type <> 'SCHOOL'
              OR last_stop.stop_purpose <> 'END_TERMINAL'
          )
    ) THEN
        RAISE EXCEPTION
            'Seed validation failed: invalid OUTBOUND terminal structure';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_route_plan route
        JOIN LATERAL (
            SELECT stop.location_type, stop.stop_purpose
            FROM public.school_bus_route_stop stop
            WHERE stop.route_id = route.id
              AND stop.is_deleted = false
            ORDER BY stop.stop_order
            LIMIT 1
        ) first_stop ON true
        JOIN LATERAL (
            SELECT stop.location_type, stop.stop_purpose
            FROM public.school_bus_route_stop stop
            WHERE stop.route_id = route.id
              AND stop.is_deleted = false
            ORDER BY stop.stop_order DESC
            LIMIT 1
        ) last_stop ON true
        WHERE route.created_by = v_seed_by
          AND route.route_direction = 'RETURN'
          AND route.is_deleted = false
          AND (
              first_stop.location_type <> 'SCHOOL'
              OR first_stop.stop_purpose <> 'START_TERMINAL'
              OR last_stop.location_type <> 'DEPOT'
              OR last_stop.stop_purpose <> 'END_TERMINAL'
          )
    ) THEN
        RAISE EXCEPTION
            'Seed validation failed: invalid RETURN terminal structure';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_route_plan route
        WHERE route.created_by = v_seed_by
          AND route.is_deleted = false
          AND (
              route.geometry_path IS NULL
              OR length(btrim(route.geometry_path)) < 20
          )
    ) THEN
        RAISE EXCEPTION
            'Seed validation failed: route geometry is missing or unexpectedly short';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_route_plan route
        WHERE route.created_by = v_seed_by
          AND route.is_deleted = false
          AND CASE
              WHEN jsonb_typeof(route.geometry_path::jsonb) = 'array'
                  THEN jsonb_array_length(route.geometry_path::jsonb) < 20
              ELSE true
          END
    ) THEN
        RAISE EXCEPTION
            'Seed validation failed: route geometry must be a JSON coordinate array with at least 20 points';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_route_plan route
        WHERE route.created_by = v_seed_by
          AND route.is_deleted = false
          AND upper(coalesce(route.geometry_source, 'UNKNOWN')) = 'UNKNOWN'
    ) THEN
        RAISE EXCEPTION
            'Seed validation failed: route geometry_source must identify the routing provider';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM seed_route_waypoint_match waypoint
        WHERE waypoint.stop_purpose IN ('START_TERMINAL', 'END_TERMINAL')
          AND waypoint.distance_km > 0.5
    ) THEN
        RAISE EXCEPTION
            'Seed validation failed: route geometry terminal mismatch';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM seed_route_waypoint_match waypoint
        WHERE waypoint.location_type = 'PICKUP_POINT'
          AND waypoint.distance_km > 0.5
    ) THEN
        RAISE EXCEPTION
            'Seed validation failed: route geometry does not pass near all service stops';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM (
            SELECT
                waypoint.route_code,
                waypoint.stop_order,
                waypoint.geometry_index,
                lag(waypoint.geometry_index) OVER (
                    PARTITION BY waypoint.route_code
                    ORDER BY waypoint.stop_order
                ) AS previous_geometry_index
            FROM seed_route_waypoint_match waypoint
        ) ordered_waypoint
        WHERE ordered_waypoint.previous_geometry_index IS NOT NULL
          AND ordered_waypoint.geometry_index <= ordered_waypoint.previous_geometry_index
    ) THEN
        RAISE EXCEPTION
            'Seed validation failed: route geometry waypoint order is invalid';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_trip_execution trip
        JOIN public.school_bus_route_plan route ON route.id = trip.route_id
        WHERE trip.created_by = v_seed_by
          AND trip.is_deleted = false
          AND route.is_deleted = false
          AND trip.route_geometry_path IS DISTINCT FROM route.geometry_path
    ) THEN
        RAISE EXCEPTION
            'Seed validation failed: trip route_geometry_path differs from its route geometry_path';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_code_sequence
        WHERE is_deleted = false
          AND is_active = true
        GROUP BY tenant_id, sequence_key
        HAVING count(*) > 1
    ) THEN
        RAISE EXCEPTION
            'Seed validation failed: duplicate active code sequence rows';
    END IF;
END $$;
