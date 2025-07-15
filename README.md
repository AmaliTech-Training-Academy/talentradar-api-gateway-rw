# Talent Radar API Gateway

A Spring Cloud Gateway-based API Gateway for the Talent Radar AI-driven talent management system.

## Overview

This API Gateway serves as the entry point for all Talent Radar microservices, providing:
- Service discovery and routing
- Request filtering and transformation
- Security and authentication
- Rate limiting and request validation
- Resilience and fault tolerance

## Prerequisites

- Java 17 or higher
- Maven 3.6.3 or higher
- Spring Boot 3.x
- Spring Cloud Gateway
- Config Server (for external configuration)
- Service Discovery (Eureka/Consul)

## Configuration

The gateway uses external configuration through Spring Cloud Config Server. Required environment variables:

```yaml
SPRING_APPLICATION_NAME=api-gateway
SPRING_CLOUD_CONFIG_URI=http://config-server:8888
```

## Running the Application

1. Ensure all required services (Config Server, Service Discovery) are running
2. Build the project:
   ```bash
   mvn clean install
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## Features

- Dynamic routing configuration
- Request/response logging
- Rate limiting


## Security

The gateway implements:
- JWT token validation
- CORS configuration
- Request validation
- Security headers
- Rate limiting

## Monitoring

The gateway exposes Actuator endpoints for monitoring:
- `/actuator/health`
- `/actuator/metrics`
- `/actuator/gateway/routes`

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support, please open an issue in the GitHub repository.
