package org.mariaelvin.library.notification_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mariaelvin.library.notification_service.dto.NotificationRequest;
import org.mariaelvin.library.notification_service.dto.NotificationResponse;
import org.mariaelvin.library.notification_service.exception.InvalidNotificationRequestException;
import org.mariaelvin.library.notification_service.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/notifications")
@RequiredArgsConstructor
@Tag(name = "Internal Notification APIs", description = "Internal notification APIs used by microservices")
public class InternalNotificationController {

    private final NotificationService notificationService;

    @Value("${app.internal.token}")
    private String internalToken;

    @Operation(summary = "Send notification from internal service")
    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @Valid @RequestBody NotificationRequest request) {

        validateInternalToken(token);

        NotificationResponse response = notificationService.sendNotification(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private void validateInternalToken(String token) {

        if (token == null || !token.equals(internalToken)) {
            throw new InvalidNotificationRequestException("Invalid internal service token");
        }
    }
}