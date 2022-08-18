# Spring-Boot Outbox Pattern

### Description
This is spring-boot outbox pattern example.

### Use case
![](./asset/use_case.png)

### Features
- [x] Order Service
- [x] Stock Service
- [ ] Inventory fulfilment
- [x] MongoDB Replica
- [x] Kafka and Connector
- [x] Sample Data

### Build package then run docker compose.
 ```bash
./mvnw clean install
docker-compose -f docker/docker-compose.yml up -d
 ```

### Ensure docker containers are up and running.

![](./asset/docker_running_process.png)

### Run test via order service
 ```bash
# Get order no. OD-001
curl --location --request GET 'http://localhost:9081/orders/OD-001'

# Create new order no. OD-101 as status "NEW"
curl --location --request POST 'http://localhost:9081/orders' \
--header 'Content-Type: application/json' \
--data-raw '{"version":0,"orderNo":"OD-101","orderDate":"2022-08-16T13:41:20.441+00:00","deliveryDate":"2022-08-16T13:41:20.441+00:00","status":"NEW","storeCode":"TH-101","storeName":"ร้านถูกดี สาขา 101","items":[{"barcode":"001","qty":20},{"barcode":"002","qty":50}]}'
 
 # Update order no. OD-101 to status "PROCESSING". At this step, stock will be allocated for this processing order. 
curl --location --request PATCH 'http://localhost:9081/orders/OD-101/processing'
 ```

### Logging
 ```bash
docker logs -f order-service
docker logs -f stock-service
docker logs -f connect
 ```

### References
- https://microservices.io/patterns/data/transactional-outbox.html
- https://debezium.io/documentation/reference/stable/connectors/mongodb.html
- https://www.mongodb.com/docs/manual/core/replica-set-oplog