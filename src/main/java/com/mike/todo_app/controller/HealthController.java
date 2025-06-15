package com.mike.todo_app.controller;

import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

  @Autowired
  private DataSource dataSource;

  /**
   * Basic application health check
   * GET /api/health
   */
  @GetMapping("/spring")
  public ResponseEntity<Map<String, Object>> healthCheck() {
    Map<String, Object> response = new HashMap<>();
    response.put("status", "UP");
    response.put("timestamp", LocalDateTime.now());
    response.put("application", "Todo App");
    response.put("version", "1.0.0");

    return ResponseEntity.ok(response);
  }

  /**
   * Database connectivity check
   * GET /api/health/database
   */
  @GetMapping("/database")
  public ResponseEntity<Map<String, Object>> databaseCheck() {
    Map<String, Object> response = new HashMap<>();

    try (Connection connection = dataSource.getConnection()) {
      response.put("status", "UP");
      response.put("database", connection.getCatalog());
      response.put("timestamp", LocalDateTime.now());
      response.put("message", "Database connection successful");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("status", "DOWN");
      response.put("timestamp", LocalDateTime.now());
      response.put("message", "Database connection failed: " + e.getMessage());

      return ResponseEntity.status(503).body(response);
    }
  }

  /**
   * Complete system status check
   * GET /api/health/status
   */
  @GetMapping("/status")
  public ResponseEntity<Map<String, Object>> systemStatus() {
    Map<String, Object> response = new HashMap<>();
    Map<String, Object> checks = new HashMap<>();
    
    // Application check
    checks.put("application", Map.of(
      "status", "UP",
      "message", "Application is running"
    ));
    
    // Database check
    try (Connection connection = dataSource.getConnection()) {
      checks.put("database", Map.of(
        "status", "UP",
        "database", connection.getCatalog(),
        "message", "Database connection successful"
      ));
    } catch (Exception e) {
      checks.put("database", Map.of(
        "status", "DOWN",
        "error", e.getMessage(),
        "message", "Database connection failed"
      ));
    }

    // Overral status
    boolean allUp = checks.values().stream().allMatch(check -> "UP".equals(((Map<?, ?>) check).get("status")));

    response.put("status", allUp ? "UP" : "DOWN");
    response.put("timestamp", LocalDateTime.now());
    response.put("checks", checks);

    return ResponseEntity.ok(response);
  }
}