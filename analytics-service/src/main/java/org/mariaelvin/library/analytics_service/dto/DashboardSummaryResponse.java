package org.mariaelvin.library.analytics_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardSummaryResponse {

    private long totalUsers;

    private long totalBooks;
    private long availableBooks;
    private long lowStockBooks;

    private long borrowRecords;
    private long pendingFines;
    private long notifications;

    private boolean memberView;
}