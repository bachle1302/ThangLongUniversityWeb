package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, String> {
}
