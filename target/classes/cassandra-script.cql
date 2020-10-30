CREATE KEYSPACE IF NOT EXISTS ads WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
CREATE TABLE IF NOT EXISTS ads.ad_events
(request_id varchar,
 ad_id varchar,
 ad_title varchar,
 advertiser_cost double,
 app_id varchar,
 app_title varchar,
 impression_time bigint,
 click_time bigint,
 PRIMARY KEY (request_id));