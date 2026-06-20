package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.RegistrationTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationTimeSlotRepository extends JpaRepository<RegistrationTimeSlot, Long> {
    List<RegistrationTimeSlot> findByRegistrationRoundId(Long registrationRoundId);
}
