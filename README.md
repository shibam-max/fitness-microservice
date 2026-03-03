# Fitness Microservices

A Spring Boot microservices architecture for a fitness tracking application with service discovery, data persistence, and event streaming capabilities.

## Project Overview

This project implements a distributed fitness application using microservices architecture with the following components:

- **Eureka Server**: Service discovery and registration
- **User Service**: User management and authentication
- **Activity Service**: Activity tracking and management
- **AI Service**: Recommendations and AI-powered features

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Eureka Server (8761)                  │
│                   Service Discovery                      │
└─────────────────────────────────────────────────────────┘
           │                     │                    │
           ▼                     ▼                    ▼
    ┌────────────┐      ┌─────────────┐      ┌──────────┐
    │   User     │      │  Activity   │      │   AI     │
    │  Service   │      │   Service   │      │ Service  │
    │  (8081)    │      │   (8082)    │      │ (8083)   │
    │ PostgreSQL │      │  MongoDB    │      │ MongoDB  │
    └────────────┘      └─────────────┘      └──────────┘
                              │
                              ▼
                         ┌─────────────┐
                         │   Kafka     │
                         │   (9092)    │
                         └─────────────┘
```

## Technology Stack

### Core Framework
- **Spring Boot**: 4.0.0 - 4.0.2
- **Spring Cloud**: 2025.1.0
- **Java**: 25.0.1

### Data Persistence
- **PostgreSQL**: User service database
- **MongoDB**: Activity and AI service databases (localhost:27017)

### Service Communication
- **Spring Cloud Netflix Eureka**: Service discovery and registration
- **Spring Cloud LoadBalancer**: Client-side load balancing
- **Spring WebFlux**: Reactive WebClient for inter-service communication
- **Apache Kafka**: Event streaming

### Additional Libraries
- **Lombok**: Code generation
- **Jackson**: JSON serialization
- **JPA/Hibernate**: ORM for PostgreSQL

## Services

### 1. Eureka Server
**Port**: 8761  
**Purpose**: Service discovery and registration center

**Features**:
- Register and discover microservices
- Health monitoring
- Dashboard at `http://localhost:8761`

**Dependencies**:
- spring-cloud-starter-netflix-eureka-server
- Spring Boot Starter Test

### 2. User Service
**Port**: 8081  
**Database**: PostgreSQL (localhost:5432/fitness-micro-user)  
**Purpose**: User management, registration, and validation

**Dependencies**:
- spring-boot-starter-data-jpa
- spring-boot-starter-validation
- spring-boot-starter-webmvc
- spring-cloud-starter-netflix-eureka-client
- postgresql

**Endpoints**:
- `POST /api/users/register` - Register a new user
- `GET /api/users/{userId}` - Get user profile
- `GET /api/users/{userId}/validate` - Validate user existence

**Database Credentials**:
```properties
Username: postgres
Password: admin@123
Database: fitness-micro-user
```

**Entity**:
- `User`: id, email, password, firstName, lastName, role (USER/ADMIN), createdAt, updatedAt

### 3. Activity Service
**Port**: 8082  
**Database**: MongoDB (localhost:27017/aiactivityfitness)  
**Purpose**: Track user activities and fitness metrics

**Dependencies**:
- spring-boot-starter-mongodb
- spring-boot-starter-kafka
- spring-boot-starter-webmvc
- spring-boot-starter-webflux
- spring-cloud-starter-netflix-eureka-client
- spring-cloud-starter-loadbalancer

**Endpoints**:
- `POST /api/activities` - Track a new activity

**Kafka Configuration**:
- Topic: `activity-events`
- Bootstrap Server: localhost:9092

**Features**:
- Validates user existence via User Service
- Publishes activity events to Kafka
- Uses WebClient with LoadBalancer for service-to-service communication

**Request Example**:
```json
{
  "userId": "user-id",
  "type": "RUNNING",
  "duration": 45,
  "caloriesBurned": 420,
  "startTime": "2026-01-04T06:30:00",
  "additionalMetrics": {
    "distanceKm": 7.5,
    "averagePace": "6:00",
    "steps": 145
  }
}
```

### 4. AI Service
**Port**: 8083  
**Database**: MongoDB (localhost:27017/airecommendationfitness)  
**Purpose**: Provide fitness recommendations based on user activity

**Dependencies**:
- spring-boot-starter-mongodb
- spring-boot-starter-kafka
- spring-boot-starter-webmvc
- spring-cloud-starter-netflix-eureka-client

**Endpoints**:
- `GET /api/recommendations/user/{userId}` - Get recommendations for a user
- `GET /api/recommendations/activity/{activityId}` - Get recommendations for an activity

**Kafka Configuration**:
- Bootstrap Server: localhost:9092
- Consumes: `activity-events` topic

## Prerequisites

### System Requirements
- **Java**: OpenJDK 25.0.1 or compatible
- **Maven**: 3.9.x or higher (Maven Wrapper provided)
- **Git**: For version control

### Services Required (External)
- **PostgreSQL**: Running on localhost:5432
- **MongoDB**: Running on localhost:27017
- **Kafka**: Running on localhost:9092 (Zookeeper on default ports)

## Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd fitness microservices
```

### 2. Verify Prerequisites
Ensure PostgreSQL, MongoDB, and Kafka are running:

```bash
# PostgreSQL
psql -U postgres -c "CREATE DATABASE fitness-micro-user;"

# MongoDB
mongosh

# Kafka (if not running)
kafka-server-start.sh $KAFKA_HOME/config/server.properties
```

### 3. Build All Services
```bash
# Build Eureka
cd eureka
./mvnw clean package -DskipTests

# Build User Service
cd ../userservice/userservice
./mvnw clean package -DskipTests

# Build Activity Service
cd ../../activityservice
./mvnw clean package -DskipTests

# Build AI Service
cd ../aiservice
./mvnw clean package -DskipTests
```

## Running the Application

### Startup Sequence (Important - Order Matters)

**Terminal 1 - Start Eureka Server**:
```bash
cd eureka
./mvnw spring-boot:run
# Service will be available at http://localhost:8761
```

**Terminal 2 - Start User Service**:
```bash
cd userservice/userservice
./mvnw spring-boot:run
# Service will register with Eureka
# Database: PostgreSQL on localhost:5432
```

**Terminal 3 - Start Activity Service**:
```bash
cd activityservice
./mvnw spring-boot:run
# Service will register with Eureka
# Depends on User Service for validation
```

**Terminal 4 - Start AI Service** (Optional):
```bash
cd aiservice
./mvnw spring-boot:run
# Service will register with Eureka
```

> **Important**: Services must start in this order to ensure proper registration with Eureka and inter-service communication.

## Configuration Files

### Activity Service (`application.yml`)
```yaml
spring:
  application:
    name: activityservice
  mongodb:
    uri: mongodb://localhost:27017/aiactivityfitness
  kafka:
    bootstrap-server: localhost:9092

server:
  port: 8082

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/

kafka:
  topic:
    name: activity-events
```

### User Service (`application.properties`)
```properties
spring.application.name=user-service
spring.datasource.url=jdbc:postgresql://localhost:5432/fitness-micro-user
spring.datasource.username=postgres
spring.datasource.password=admin@123
server.port=8081
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

### AI Service (`application.yml`)
```yaml
spring:
  application:
    name: aiservice
  mongodb:
    uri: mongodb://localhost:27017/airecommendationfitness

server:
  port: 8083

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

### Eureka Server (`application.yml`)
```yaml
spring:
  application:
    name: eureka

server:
  port: 8761

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

## API Examples

### User Service

**Register User**:
```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Get User Profile**:
```bash
curl http://localhost:8081/api/users/user-id
```

**Validate User**:
```bash
curl http://localhost:8081/api/users/user-id/validate
```

### Activity Service

**Track Activity**:
```bash
curl -X POST http://localhost:8082/api/activities \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-id",
    "type": "RUNNING",
    "duration": 45,
    "caloriesBurned": 420,
    "startTime": "2026-01-04T06:30:00",
    "additionalMetrics": {
      "distanceKm": 7.5,
      "averagePace": "6:00",
      "steps": 145
    }
  }'
```

### AI Service

**Get User Recommendations**:
```bash
curl http://localhost:8083/api/recommendations/user/user-id
```

**Get Activity Recommendations**:
```bash
curl http://localhost:8083/api/recommendations/activity/activity-id
```

## Service Discovery & Communication

The project uses **Spring Cloud Netflix Eureka** for service discovery:

- **Service Registration**: Each microservice registers itself with Eureka on startup
- **Client-Side Load Balancing**: Spring Cloud LoadBalancer handles request distribution
- **Service-to-Service Communication**: Uses Spring WebClient with `@LoadBalanced` annotation

### Inter-Service Calls

**Activity Service → User Service**:
- Activity Service validates user existence before tracking activities
- Uses WebClient with service name: `user-service`
- Implements timeout (5 seconds) and fallback logic

## Troubleshooting

### Service Registration Issues

**Symptom**: "Registered Applications size is zero"

**Solution**:
1. Ensure Eureka is running on port 8761
2. Verify services have `@EnableDiscoveryClient` annotation
3. Check `eureka.client.serviceUrl.defaultZone` configuration
4. Restart services in the correct order

### Database Connection Issues

**PostgreSQL**:
```bash
# Verify connection
psql -U postgres -h localhost -d fitness-micro-user
```

**MongoDB**:
```bash
# Verify connection
mongosh --eval "db.adminCommand('ping')"
```

### Kafka Issues

**Verify Kafka is running**:
```bash
# Check Kafka topics
kafka-topics.sh --list --bootstrap-server localhost:9092

# Create activity-events topic if missing
kafka-topics.sh --create --topic activity-events \
  --bootstrap-server localhost:9092 \
  --partitions 1 --replication-factor 1
```

### WebClient DNS Resolution Errors

**Error**: `Failed to resolve 'user-service'`

**Solution**:
1. Verify User Service is registered in Eureka
2. Check service name matches configuration
3. Use service name (lowercase with hyphens): `user-service`
4. Ensure LoadBalancer is enabled with `@LoadBalanced`

## Project Structure

```
fitness-microservices/
├── eureka/                          # Service Discovery Server
│   ├── src/main/java/com/fitness/eureka/
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
│
├── userservice/userservice/         # User Management Service
│   ├── src/main/java/com/fitness/userservice/
│   │   ├── UserserviceApplication.java
│   │   ├── controller/
│   │   ├── services/
│   │   ├── models/
│   │   ├── dto/
│   │   └── UserRepository.java
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
├── activityservice/                 # Activity Tracking Service
│   ├── src/main/java/com/fitness/activityservice/
│   │   ├── ActivityserviceApplication.java
│   │   ├── controller/
│   │   ├── service/
│   │   ├── model/
│   │   ├── dto/
│   │   ├── config/
│   │   └── ActivityRepository.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
│
├── aiservice/                       # AI Recommendations Service
│   ├── src/main/java/com/fitness/aiservice/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── model/
│   │   └── repository/
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
│
└── README.md                        # This file
```

## Dependencies Overview

### Spring Boot Starters
- `spring-boot-starter-webmvc`: REST API development
- `spring-boot-starter-webflux`: Reactive programming for WebClient
- `spring-boot-starter-mongodb`: MongoDB integration
- `spring-boot-starter-data-jpa`: JPA/Hibernate ORM
- `spring-boot-starter-kafka`: Kafka integration
- `spring-boot-starter-validation`: Bean validation

### Spring Cloud
- `spring-cloud-starter-netflix-eureka-server`: Service registry
- `spring-cloud-starter-netflix-eureka-client`: Service discovery
- `spring-cloud-starter-loadbalancer`: Client-side load balancing

### Data & Drivers
- `postgresql`: PostgreSQL JDBC driver
- `mongodb-driver-sync`: MongoDB synchronous driver

### Development
- `lombok`: Reduce boilerplate code

## Known Issues & Limitations

1. **Development Environment Only**: Current configuration is suitable for development. Production deployment requires:
   - Secure password management
   - Environment-specific configurations
   - Container orchestration (Docker/Kubernetes)
   - Load balancing and failover mechanisms

2. **Service-to-Service Timeouts**: Activity Service has a 5-second timeout when calling User Service. Network latency or service unavailability may cause requests to fail gracefully.

3. **MongoDB Connectivity**: Both Activity and AI services use MongoDB. Ensure MongoDB is accessible at `localhost:27017` in development.

4. **Kafka Event Processing**: Activity events are published to Kafka but not actively consumed in the current implementation.

## Future Enhancements

- [ ] API authentication and authorization (JWT)
- [ ] Distributed logging and tracing (ELK Stack, Jaeger)
- [ ] Circuit breaker pattern (Resilience4j)
- [ ] API rate limiting and throttling
- [ ] Metrics and monitoring (Prometheus, Grafana)
- [ ] Docker and Kubernetes deployment configurations
- [ ] Integration tests and contract testing
- [ ] API documentation (Swagger/OpenAPI)
- [ ] Database migration scripts (Flyway/Liquibase)

## Contributing

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Commit changes: `git commit -m "Add your feature"`
3. Push to branch: `git push origin feature/your-feature`
4. Open a Pull Request

## License

[Add your license here]

## Contact

For issues or questions, please open an issue in the repository.

---

**Last Updated**: March 3, 2026  
**Version**: 0.0.1-SNAPSHOT
