# Spring Security Observability Lab

This lab protects Actuator endpoints and records security failures as Prometheus metrics.

## Security Rule

```text
/actuator/health
/actuator/info
/actuator/prometheus
-> public

other /actuator endpoints
-> ADMIN role required
```

Admin test account:

```text
username: admin
password: admin123
```

Non-admin test account:

```text
username: user
password: user123
```

## Test Flow

### 1. Public Health Check

```http
GET http://localhost:8080/actuator/health
```

Expected:

```text
200 OK
```

### 2. Protected Metrics Without Login

```http
GET http://localhost:8080/actuator/metrics
```

Expected:

```text
401 Unauthorized
```

PromQL:

```promql
increase(security_auth_failures_total[5m])
```

Note:

```text
/actuator/prometheus is intentionally public in this lab so Prometheus can scrape security metrics.
/actuator/metrics stays protected for manual access tests.
```

### 3. Protected Metrics With Admin Login

Postman Authorization:

```text
Type: Basic Auth
Username: admin
Password: admin123
```

Request:

```http
GET http://localhost:8080/actuator/metrics
```

Expected:

```text
200 OK
```

### 4. Protected Metrics With Non-Admin Login

Postman Authorization:

```text
Type: Basic Auth
Username: user
Password: user123
```

Request:

```http
GET http://localhost:8080/actuator/metrics
```

Expected:

```text
403 Forbidden
```

PromQL:

```promql
increase(security_access_denied_total[5m])
```

## Why This Matters

Security observability asks:

```text
Who tried to access protected operational surfaces?
How often did authentication fail?
Which endpoints were targeted?
```

This extends the agent behavior lab:

```text
agent behavior metrics
+ policy metrics
+ security access metrics
```

Core message:

```text
Operational security events should be measured, not only blocked.
```
