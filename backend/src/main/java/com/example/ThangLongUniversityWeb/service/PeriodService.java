package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.PeriodRequest;
import com.example.ThangLongUniversityWeb.dto.response.PeriodResponse;
import com.example.ThangLongUniversityWeb.entity.Period;
import com.example.ThangLongUniversityWeb.exception.ConflictException;
import com.example.ThangLongUniversityWeb.repository.PeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PeriodService {

    private final PeriodRepository periodRepository;

    @Cacheable(cacheNames = "periods")
    public List<PeriodResponse> getAllPeriods() {
        return periodRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(cacheNames = {"periods", "classSectionOptions"}, allEntries = true)
    public PeriodResponse createPeriod(PeriodRequest request) {
        if (request.getPeriodNumber() == null) {
            throw new RuntimeException("Số tiết không được để trống");
        }
        periodRepository.findByPeriodNumber(request.getPeriodNumber()).ifPresent(existing -> {
            throw new RuntimeException("Tiết " + request.getPeriodNumber() + " đã tồn tại");
        });

        Period period = new Period();
        period.setPeriodNumber(request.getPeriodNumber());
        period.setStartTime(request.getStartTime());
        period.setEndTime(request.getEndTime());

        return toResponse(periodRepository.save(period));
    }

    @Transactional
    @CacheEvict(cacheNames = {"periods", "classSectionOptions"}, allEntries = true)
    public PeriodResponse updatePeriod(Long id, PeriodRequest request) {
        Period period = periodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tiết học!"));

        if (request.getPeriodNumber() != null && !request.getPeriodNumber().equals(period.getPeriodNumber())) {
            periodRepository.findByPeriodNumber(request.getPeriodNumber()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new RuntimeException("Tiết " + request.getPeriodNumber() + " đã tồn tại");
                }
            });
            period.setPeriodNumber(request.getPeriodNumber());
        }
        period.setStartTime(request.getStartTime());
        period.setEndTime(request.getEndTime());

        return toResponse(periodRepository.save(period));
    }

    @Transactional
    @CacheEvict(cacheNames = {"periods", "classSectionOptions"}, allEntries = true)
    public void deletePeriod(Long id) {
        Period period = periodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay tiet hoc!"));
        try {
            periodRepository.delete(period);
            periodRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Khong the xoa tiet hoc vi dang duoc su dung trong lich lop hoc phan.");
        }
    }

    private PeriodResponse toResponse(Period period) {
        return PeriodResponse.builder()
                .id(period.getId())
                .periodNumber(period.getPeriodNumber())
                .startTime(period.getStartTime())
                .endTime(period.getEndTime())
                .build();
    }
}
