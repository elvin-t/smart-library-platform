package org.mariaelvin.library.user_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.mariaelvin.library.user_service.dto.MembershipStatus;
import org.mariaelvin.library.user_service.dto.MembershipType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_membership_status", columnList = "membership_status")
        }
)
public class User {

    /**
     * This ID comes from Auth Service.
     * auth_users.id = users.id
     */
    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_type", nullable = false, length = 50)
    private MembershipType membershipType = MembershipType.STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_status", nullable = false, length = 50)
    private MembershipStatus membershipStatus = MembershipStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (this.membershipType == null) {
            this.membershipType = MembershipType.STANDARD;
        }

        if (this.membershipStatus == null) {
            this.membershipStatus = MembershipStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}