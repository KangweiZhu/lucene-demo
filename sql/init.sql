-- 建库（可选）
CREATE DATABASE IF NOT EXISTS taxi;
USE taxi;

-- 创建开发用账号
CREATE USER 'dev'@'%' IDENTIFIED BY 'dev123';
GRANT ALL PRIVILEGES ON *.* TO 'dev'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

-- 第一张表 --
# CREATE TABLE taxi_trip (
#     id BIGINT PRIMARY KEY AUTO_INCREMENT,
#     pickup_location VARCHAR(255),
#     dropoff_location VARCHAR(255),
#     passenger_count INT,
#     total_amount DECIMAL(10, 2),
#     pickup_datetime DATETIME
# );

CREATE TABLE raw_taxi_trip (
            VendorID INT,
            tpep_pickup_datetime DATETIME,
            tpep_dropoff_datetime DATETIME,
            passenger_count INT,
            trip_distance DOUBLE,
            RatecodeID INT,
            store_and_fwd_flag CHAR(1),
            PULocationID INT,
            DOLocationID INT,
            payment_type INT,
            fare_amount DOUBLE,
            extra DOUBLE,
            mta_tax DOUBLE,
            tip_amount DOUBLE,
            tolls_amount DOUBLE,
            improvement_surcharge DOUBLE,
            total_amount DOUBLE,
            congestion_surcharge DOUBLE,
            airport_fee DOUBLE
);

-- 直接插会报错 [22001][1292] Data truncation: Incorrect datetime value: '01/01/2023 12:32:10 AM' for column 'tpep_pickup_datetime' at row 1
-- 因为美式时间不兼容mysql的MM/DD/YYYY hh:mm:ss AM/PM
-- 先改成VarChar，后面再转
# LOAD DATA INFILE '/var/lib/mysql-files/2023_Yellow_Taxi_Trip_Data.csv'
# INTO TABLE raw_taxi_trip
# FIELDS TERMINATED BY ','
# OPTIONALLY ENCLOSED BY '"'
# IGNORE 1 LINES;

DROP TABLE IF EXISTS raw_taxi_trip;
DROP TABLE IF EXISTS taxi_trip;

CREATE TABLE raw_taxi_trip (
            VendorID INT NULL,
            tpep_pickup_datetime VARCHAR(25),
            tpep_dropoff_datetime VARCHAR(25),
            passenger_count INT NULL,
            trip_distance DOUBLE NULL,
            RatecodeID INT NULL,
            store_and_fwd_flag CHAR(1),
            PULocationID INT NULL,
            DOLocationID INT NULL,
            payment_type INT NULL,
            fare_amount DOUBLE NULL,
            extra DOUBLE NULL,
            mta_tax DOUBLE NULL,
            tip_amount DOUBLE NULL,
            tolls_amount DOUBLE NULL,
            improvement_surcharge DOUBLE NULL,
            total_amount DOUBLE NULL,
            congestion_surcharge DOUBLE NULL,
            airport_fee DOUBLE NULL
);

LOAD DATA INFILE '/var/lib/mysql-files/2023_Yellow_Taxi_Trip_Data.csv'
    INTO TABLE raw_taxi_trip
    FIELDS TERMINATED BY ','
    OPTIONALLY ENCLOSED BY '"'
    IGNORE 1 LINES
    (
     VendorID,
     tpep_pickup_datetime,
     tpep_dropoff_datetime,
     @passenger_count,
     @trip_distance,
     @RatecodeID,
     store_and_fwd_flag,
     @PULocationID,
     @DOLocationID,
     @payment_type,
     @fare_amount,
     @extra,
     @mta_tax,
     @tip_amount,
     @tolls_amount,
     @improvement_surcharge,
     @total_amount,
     @congestion_surcharge,
     @airport_fee
        )
    SET
        passenger_count = NULLIF(@passenger_count, ''),
        trip_distance = NULLIF(@trip_distance, ''),
        RatecodeID = NULLIF(@RatecodeID, ''),
        PULocationID = NULLIF(@PULocationID, ''),
        DOLocationID = NULLIF(@DOLocationID, ''),
        payment_type = NULLIF(@payment_type, ''),
        fare_amount = NULLIF(@fare_amount, ''),
        extra = NULLIF(@extra, ''),
        mta_tax = NULLIF(@mta_tax, ''),
        tip_amount = NULLIF(@tip_amount, ''),
        tolls_amount = NULLIF(@tolls_amount, ''),
        improvement_surcharge = NULLIF(@improvement_surcharge, ''),
        total_amount = NULLIF(@total_amount, ''),
        congestion_surcharge = NULLIF(@congestion_surcharge, ''),
        airport_fee = NULLIF(@airport_fee, '');
-- 14 m 10 s 497 ms

SELECT COUNT(*) FROM raw_taxi_trip;
-- 38310226

-- 新增一个转换成datetime的字段
ALTER TABLE raw_taxi_trip
    ADD COLUMN pickup_dt DATETIME,
    ADD COLUMN dropoff_dt DATETIME;

UPDATE raw_taxi_trip
SET pickup_dt = STR_TO_DATE(tpep_pickup_datetime, '%m/%d/%Y %r')
WHERE tpep_pickup_datetime IS NOT NULL;

CREATE TABLE taxi_trip (
            id BIGINT PRIMARY KEY AUTO_INCREMENT,
            pickup_location VARCHAR(255),
            dropoff_location VARCHAR(255),
            passenger_count INT,
            total_amount DECIMAL(10, 2),
            pickup_datetime DATETIME
);

INSERT INTO taxi_trip (pickup_location, dropoff_location, passenger_count, total_amount, pickup_datetime)
SELECT
    CONCAT('Zone-', PULocationID),
    CONCAT('Zone-', DOLocationID),
    passenger_count,
    total_amount,
    pickup_dt
FROM raw_taxi_trip
WHERE pickup_dt IS NOT NULL AND passenger_count IS NOT NULL;
