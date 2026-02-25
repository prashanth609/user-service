# User Service (Java 17 + Spring Boot + MySQL)

A small CRUD User Service with:
- **Layered architecture** (controller → service → repository)
- **Profiles**: `dev` and `prod` via `application-dev.properties` / `application-prod.properties`
- **Spring Boot Actuator** endpoints
- **No Docker** and **No Flyway** (schema uses JPA `ddl-auto`)

## Prerequisites
- Java 17
- Maven 3.9+
- MySQL 8+

## 1) Create DB (MySQL)
```sql
CREATE DATABASE user_service;
```

## 2) Configure credentials
Edit:
- `src/main/resources/application-dev.properties` (local dev)
- `src/main/resources/application-prod.properties` (prod-like)

## 3) Run
### Dev profile
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Prod profile
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 4) Build jar
```bash
mvn clean package
java -jar target/user-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

## API
Base path: `/api/v1/users`

### Create
`POST /api/v1/users`
```json
{
  "name": "Ranjith",
  "email": "ranjith@example.com",
  "phone": "9876543210"
}
```

### Get all
`GET /api/v1/users`

### Get by id
`GET /api/v1/users/{id}`

### Update
`PUT /api/v1/users/{id}`

### Delete
`DELETE /api/v1/users/{id}`

## Actuator
- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/metrics`
- `GET /actuator/prometheus` (enabled)

> In **dev** we expose a few common endpoints. In **prod** we expose only health/info by default (configurable).

## Postman
Import `postman/User-Service-MySQL.postman_collection.json`

## Spring Boot Auto-Configuration Debug Report & Custom Condition

Spring Boot decides which auto-configurations to apply based on a set of **conditions**
(classpath, properties, beans, environment, etc.). When something enables/disables a feature unexpectedly,
the fastest way to understand why is the **Condition Evaluation Report**.

### 1) Enable the debug report

#### Option A: Run with `--debug`
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments="--debug"
```

Or when running the jar:
```bash
java -jar target/user-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev --debug
```

What you’ll see in logs:
- **Positive matches**: auto-configs that were applied and why
- **Negative matches**: auto-configs that were skipped and why
- **Unconditional classes**: always applied configs

### 2) View the report via Actuator: `/actuator/conditions`

If Actuator is enabled and the endpoint is exposed, Spring Boot provides the same report as JSON.

In **dev profile**, you can expose it (recommended only for dev):

`src/main/resources/application-dev.properties`
```properties
management.endpoints.web.exposure.include=health,info,metrics,env,loggers,conditions
management.endpoint.health.show-details=always
```

Now you can check:
```bash
curl http://localhost:8080/actuator/conditions
```

### 3) Typical reasons an auto-configuration is skipped

Common condition types you’ll see in the report:

- **@ConditionalOnClass**: runs only if a class is present on the classpath
- **@ConditionalOnProperty**: runs only if a property is set (or has a value)
- **@ConditionalOnBean / @ConditionalOnMissingBean**: depends on whether a bean exists already

### 4) Turn on more detailed condition logging (optional)
```properties
logging.level.org.springframework.boot.autoconfigure=DEBUG
```

> Use this sparingly—logs get noisy.

---

## Custom Condition (Feature Toggle Style)

Sometimes you want your own configuration to load only when a feature is enabled.

### Option 1: Use built-in `@ConditionalOnProperty` (simplest)

Example: enable a feature only when `app.features.audit=true`

```java
@Configuration
@ConditionalOnProperty(prefix = "app.features", name = "audit", havingValue = "true")
public class AuditConfig {

    @Bean
    public AuditService auditService() {
        return new AuditService();
    }
}
```

`application-dev.properties`
```properties
app.features.audit=true
```

`application-prod.properties`
```properties
app.features.audit=false
```

### Option 2: Create a custom `@Conditional` annotation (advanced)

#### Step A: Create a `Condition`
```java
public class MyFeatureEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String value = context.getEnvironment().getProperty("app.features.myFeature", "false");
        return "true".equalsIgnoreCase(value);
    }
}
```

#### Step B: Create a custom annotation
```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(MyFeatureEnabledCondition.class)
public @interface ConditionalOnMyFeatureEnabled {
}
```

#### Step C: Use it in a bean method
```java
@Bean
@ConditionalOnMyFeatureEnabled
public MyFeatureService myFeatureService() {
    return new MyFeatureService();
}
```

#### Step D: Toggle via properties
```properties
app.features.myFeature=true
```

### How to verify your condition worked

1) Check logs using `--debug`  
2) Check actuator in dev:
```bash
curl http://localhost:8080/actuator/conditions
```
3) Confirm the bean exists only when enabled.
