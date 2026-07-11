package org.mariaelvin.library.analytics_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mariaelvin.library.analytics_service.client.BookServiceClient;
import org.mariaelvin.library.analytics_service.client.BorrowServiceClient;
import org.mariaelvin.library.analytics_service.client.NotificationServiceClient;
import org.mariaelvin.library.analytics_service.client.UserServiceClient;
import org.mariaelvin.library.analytics_service.dto.BorrowRecordResponse;
import org.mariaelvin.library.analytics_service.dto.DashboardSummaryResponse;
import org.mariaelvin.library.analytics_service.dto.PageResponse;
import org.mariaelvin.library.analytics_service.exception.ErrorCode;
import org.mariaelvin.library.analytics_service.exception.ExternalServiceException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final UserServiceClient userServiceClient;
    private final BookServiceClient bookServiceClient;
    private final BorrowServiceClient borrowServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public DashboardSummaryResponse getDashboardSummary(
            Long userId,
            String rolesHeader
    ) {
        boolean isMember = hasRole(rolesHeader, "MEMBER");
        boolean isAdmin = hasRole(rolesHeader, "ADMIN");
        boolean isLibrarian = hasRole(rolesHeader, "LIBRARIAN");

        long totalUsers = isAdmin ? safeTotalUsers() : 0;

        long totalBooks = safeTotalBooks();
        long availableBooks = safeAvailableBooks();

        long lowStockBooks = (isAdmin || isLibrarian)
                ? safeLowStockBooks()
                : 0;

        PageResponse<BorrowRecordResponse> borrowResponse =
                safeBorrowRecords(isMember, userId);

        long borrowRecords = borrowResponse != null
                ? borrowResponse.getTotalElements()
                : 0;

        long pendingFines = calculatePendingFines(borrowResponse);

        long notifications = safeNotifications(isMember, userId);

        return DashboardSummaryResponse.builder()
                .totalUsers(totalUsers)
                .totalBooks(totalBooks)
                .availableBooks(availableBooks)
                .lowStockBooks(lowStockBooks)
                .borrowRecords(borrowRecords)
                .pendingFines(pendingFines)
                .notifications(notifications)
                .memberView(isMember)
                .build();
    }

    private boolean hasRole(String rolesHeader, String role) {
        return rolesHeader != null && rolesHeader.toUpperCase().contains(role);
    }

    private long safeTotalUsers() {
        try {
            return userServiceClient.getUsers().size();
        } catch (Exception ex) {
            log.warn("Failed to fetch total users", ex);

            throw new ExternalServiceException(
                    ErrorCode.ANALYTICS_003,
                    "Unable to fetch user count from user service",
                    ex
            );
        }
    }

    private long safeTotalBooks() {
        try {
            PageResponse<?> response = bookServiceClient.getBooks(0, 1);

            return response != null ? response.getTotalElements() : 0;
        } catch (Exception ex) {
            log.warn("Failed to fetch total books. Returning fallback value 0", ex);
            return 0;
        }
    }

    private long safeAvailableBooks() {
        try {
            PageResponse<?> response = bookServiceClient.getAvailableBooks(0, 1);
            return response != null ? response.getTotalElements() : 0;
        } catch (Exception ex) {
            log.warn("Failed to fetch available books", ex);
            return 0;
        }
    }

    private long safeLowStockBooks() {
        try {
            PageResponse<?> response =
                    bookServiceClient.getLowStockBooks(2, 0, 1);

            return response != null ? response.getTotalElements() : 0;
        } catch (Exception ex) {
            log.warn("Failed to fetch low stock books", ex);
            return 0;
        }
    }

    private PageResponse<BorrowRecordResponse> safeBorrowRecords(
            boolean isMember,
            Long userId
    ) {
        try {
            if (isMember && userId != null) {
                return borrowServiceClient.getBorrowRecordsByUser(userId, 0, 100);
            }

            return borrowServiceClient.getAllBorrowRecords(0, 100);
        } catch (Exception ex) {
            log.warn("Failed to fetch borrow records", ex);
            return null;
        }
    }

    private long calculatePendingFines(
            PageResponse<BorrowRecordResponse> borrowResponse
    ) {
        if (borrowResponse == null || borrowResponse.getContent() == null) {
            return 0;
        }

        return borrowResponse.getContent()
                .stream()
                .filter(record ->
                        record.getFineAmount() != null
                                && record.getFineAmount().compareTo(BigDecimal.ZERO) > 0
                                && !Boolean.TRUE.equals(record.getFinePaid())
                )
                .count();
    }

    private long safeNotifications(boolean isMember, Long userId) {
        try {
            PageResponse<?> response;

            if (isMember && userId != null) {
                response = notificationServiceClient
                        .getNotificationsByUser(userId, 0, 1);
            } else {
                response = notificationServiceClient
                        .getAllNotifications(0, 1);
            }

            return response != null ? response.getTotalElements() : 0;

        } catch (Exception ex) {
            log.warn("Failed to fetch notifications", ex);
            return 0;
        }
    }
}