package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.Notification;
import com.example.ThangLongUniversityWeb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    Optional<Notification> findByIdAndRecipient(Long id, User recipient);
}
