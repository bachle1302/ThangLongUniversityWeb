package com.example.ThangLongUniversityWeb.repository;

import com.example.ThangLongUniversityWeb.entity.RegistrationRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRoundRepository extends JpaRepository<RegistrationRound, Long> {
    List<RegistrationRound> findBySemesterIdAndRoundTypeOrderByRoundNumberAsc(Long semesterId, String roundType);

    Optional<RegistrationRound> findFirstBySemesterIdAndRoundTypeAndRegistrationOpenTrueOrderByRoundNumberDesc(Long semesterId, String roundType);

    Optional<RegistrationRound> findFirstBySemesterIdAndRoundTypeOrderByRoundNumberDesc(Long semesterId, String roundType);

    boolean existsBySemesterIdAndRoundTypeAndRegistrationOpenTrue(Long semesterId, String roundType);

    @Query("select coalesce(max(r.roundNumber), 0) from RegistrationRound r where r.semester.id = :semesterId and r.roundType = :roundType")
    int findMaxRoundNumberBySemesterIdAndRoundType(@Param("semesterId") Long semesterId, @Param("roundType") String roundType);

    @Modifying
    @Query("update ClassSection cs set cs.registrationRound = :round where cs.semester.id = :semesterId and cs.registrationRound is null")
    int attachMissingClassSections(@Param("semesterId") Long semesterId, @Param("round") RegistrationRound round);
}
