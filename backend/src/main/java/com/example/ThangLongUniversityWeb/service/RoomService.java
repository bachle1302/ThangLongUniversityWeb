package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.RoomRequest;
import com.example.ThangLongUniversityWeb.dto.response.RoomResponse;
import com.example.ThangLongUniversityWeb.entity.Room;
import com.example.ThangLongUniversityWeb.exception.ConflictException;
import com.example.ThangLongUniversityWeb.repository.RoomRepository;
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
public class RoomService {

    private static final List<String> ROOM_TYPES = List.of("LECTURE", "LAB", "AUDITORIUM");
    private static final List<String> ROOM_STATUSES = List.of("AVAILABLE", "MAINTENANCE");

    private final RoomRepository roomRepository;

    @Cacheable(cacheNames = "rooms")
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(cacheNames = {"rooms", "classSectionOptions", "adminDashboard"}, allEntries = true)
    public RoomResponse createRoom(RoomRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Tên phòng không được để trống");
        }
        if (request.getCapacity() == null || request.getCapacity() <= 0) {
            throw new RuntimeException("Sức chứa phòng phải lớn hơn 0");
        }
        roomRepository.findByName(request.getName().trim()).ifPresent(existing -> {
            throw new RuntimeException("Phòng " + request.getName() + " đã tồn tại");
        });

        Room room = new Room();
        room.setName(request.getName().trim());
        room.setCapacity(request.getCapacity());
        room.setType(normalizeRoomType(request.getType()));
        room.setStatus(normalizeRoomStatus(request.getStatus()));

        return toResponse(roomRepository.save(room));
    }

    @Transactional
    @CacheEvict(cacheNames = {"rooms", "classSectionOptions", "adminDashboard"}, allEntries = true)
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng học!"));

        if (request.getName() != null && !request.getName().isBlank()) {
            roomRepository.findByName(request.getName().trim()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new RuntimeException("Phòng " + request.getName() + " đã tồn tại");
                }
            });
            room.setName(request.getName().trim());
        }

        if (request.getCapacity() != null) {
            if (request.getCapacity() <= 0) {
                throw new RuntimeException("Sức chứa phòng phải lớn hơn 0");
            }
            room.setCapacity(request.getCapacity());
        }

        if (request.getType() != null && !request.getType().isBlank()) {
            room.setType(normalizeRoomType(request.getType()));
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            room.setStatus(normalizeRoomStatus(request.getStatus()));
        }

        return toResponse(roomRepository.save(room));
    }

    @Transactional
    @CacheEvict(cacheNames = {"rooms", "classSectionOptions", "adminDashboard"}, allEntries = true)
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay phong hoc!"));
        try {
            roomRepository.delete(room);
            roomRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Khong the xoa phong hoc vi dang duoc su dung trong lop hoc phan hoac lich hoc.");
        }
    }

    private RoomResponse toResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .capacity(room.getCapacity())
                .type(room.getType() != null ? room.getType() : "LECTURE")
                .status(room.getStatus() != null ? room.getStatus() : "AVAILABLE")
                .build();
    }

    private String normalizeRoomType(String value) {
        String type = value == null || value.isBlank() ? "LECTURE" : value.trim().toUpperCase();
        if (!ROOM_TYPES.contains(type)) {
            throw new RuntimeException("Loai phong khong hop le");
        }
        return type;
    }

    private String normalizeRoomStatus(String value) {
        String status = value == null || value.isBlank() ? "AVAILABLE" : value.trim().toUpperCase();
        if (!ROOM_STATUSES.contains(status)) {
            throw new RuntimeException("Trang thai phong khong hop le");
        }
        return status;
    }
}
