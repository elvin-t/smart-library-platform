package org.mariaelvin.library.analytics_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.mariaelvin.library.analytics_service.dto.DashboardSummaryResponse;
import org.mariaelvin.library.analytics_service.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Analytics APIs", description = "Dashboard summary and analytics APIs")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(
            summary = "Get dashboard summary",
            description = "Returns role-based dashboard summary for Admin, Librarian, or Member"
    )
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(
            HttpServletRequest request
    ) {
        Long userId = (Long) request.getAttribute("userId");

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.getAttribute("roles");

        String rolesHeader = roles != null
                ? String.join(",", roles)
                : "";

        return ResponseEntity.ok(
                analyticsService.getDashboardSummary(userId, rolesHeader)
        );
    }
}