package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.response.ChatRoomResponse;
import com.example.ThangLongUniversityWeb.dto.response.ChatMessageResponse;
import com.example.ThangLongUniversityWeb.dto.response.ChatUserSearchResponse;
import com.example.ThangLongUniversityWeb.dto.request.ChatMessageRequest;
import com.example.ThangLongUniversityWeb.dto.request.ChatRoomRequest;
import com.example.ThangLongUniversityWeb.dto.request.MarkMessageAsReadRequest;
import com.example.ThangLongUniversityWeb.entity.User;
import com.example.ThangLongUniversityWeb.enums.MessageType;
import com.example.ThangLongUniversityWeb.repository.UserRepository;
import com.example.ThangLongUniversityWeb.service.ChatRoomService;
import com.example.ThangLongUniversityWeb.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.ThangLongUniversityWeb.service.CloudinaryService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Minimal REST APIs to support chat UI:
 * - list my rooms
 * - create/get private 1-1 room
 * - load message history
 * - search users by username
 *
 * Security: already guarded by SecurityConfig: "/api/chat/**"
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/rooms")
    public ResponseEntity<?> getMyRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200));
        Page<ChatRoomResponse> rooms = chatRoomService.getMyChatRooms(pageable);
        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/rooms/private")
    public ResponseEntity<ChatRoomResponse> getOrCreatePrivateRoom(@RequestParam Long otherUserId) {
        return ResponseEntity.ok(chatRoomService.getOrCreatePrivateChatRoom(otherUserId));
    }

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createRoom(@RequestBody ChatRoomRequest request) {
        return ResponseEntity.ok(chatRoomService.createChatRoom(request));
    }

    @DeleteMapping("/rooms/{roomId}/members/me")
    public ResponseEntity<Void> leaveRoom(@PathVariable Long roomId) {
        chatRoomService.leaveChatRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<?> getRoomMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200));
        Page<ChatMessageResponse> msgs = chatMessageService.getChatHistory(roomId, pageable);
        return ResponseEntity.ok(msgs);
    }

    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ChatMessageResponse> sendRoomMessage(
            @PathVariable Long roomId,
            @RequestBody ChatMessageRequest request
    ) {
        request.setChatRoomId(roomId);
        return ResponseEntity.ok(chatMessageService.sendMessage(request));
    }

    @PostMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> markRoomAsRead(@PathVariable Long roomId) {
        chatMessageService.markAsRead(MarkMessageAsReadRequest.builder()
                .chatRoomId(roomId)
                .build());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rooms/{roomId}/files")
    public ResponseEntity<Page<ChatMessageResponse>> getRoomFiles(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200));
        return ResponseEntity.ok(chatMessageService.getFileHistory(roomId, pageable));
    }

    @GetMapping("/rooms/{roomId}/links")
    public ResponseEntity<Page<ChatMessageResponse>> getRoomLinks(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200));
        return ResponseEntity.ok(chatMessageService.getLinkHistory(roomId, pageable));
    }

    @PostMapping(value = "/rooms/{roomId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatMessageResponse> uploadRoomFile(
            @PathVariable Long roomId,
            @RequestPart("file") MultipartFile file
    ) throws Exception {
        if (file.isEmpty()) {
            throw new RuntimeException("File khong duoc de trong");
        }

        String originalName = Path.of(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename())
                .getFileName()
                .toString();

        String secureUrl = cloudinaryService.uploadFile(file);

        ChatMessageRequest request = ChatMessageRequest.builder()
                .chatRoomId(roomId)
                .content(originalName)
                .type(MessageType.FILE)
                .mediaUrl(secureUrl)
                .fileName(originalName)
                .fileSize(file.getSize())
                .build();
        return ResponseEntity.ok(chatMessageService.sendMessage(request));
    }

    @GetMapping("/files/{roomId}/{fileName}")
    public ResponseEntity<Resource> downloadRoomFile(
            @PathVariable Long roomId,
            @PathVariable String fileName
    ) throws Exception {
        Path dir = Path.of("uploads", "chat", String.valueOf(roomId)).toAbsolutePath().normalize();
        Path target = dir.resolve(fileName).normalize();
        if (!target.startsWith(dir) || !Files.exists(target)) {
            throw new RuntimeException("File khong ton tai");
        }
        Resource resource = new UrlResource(target.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<ChatUserSearchResponse>> searchUsers(@RequestParam(name = "q") String q) {
        String query = q == null ? "" : q.trim();
        if (query.isEmpty()) return ResponseEntity.ok(List.of());

        List<User> users = userRepository.searchChatUsers(query).stream()
                .sorted(Comparator.comparing(User::getUsername))
                .limit(20)
                .toList();
        List<ChatUserSearchResponse> out = users.stream().map(this::mapChatUser).collect(Collectors.toList());

        return ResponseEntity.ok(out);
    }

    private ChatUserSearchResponse mapChatUser(User user) {
        String fullName = user.getUsername();
        String code = null;
        String subtitle = user.getRole().name();

        if (user.getStudent() != null) {
            fullName = user.getStudent().getFullName() == null ? fullName : user.getStudent().getFullName();
            code = user.getStudent().getStudentCode();
            subtitle = "Sinh vien" + (code == null ? "" : " - " + code);
        } else if (user.getTeacher() != null) {
            fullName = user.getTeacher().getFullName() == null ? fullName : user.getTeacher().getFullName();
            code = user.getTeacher().getTeacherCode();
            subtitle = "Giang vien" + (code == null ? "" : " - " + code);
        }

        return ChatUserSearchResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .code(code)
                .fullName(fullName)
                .subtitle(subtitle)
                .avatarUrl("https://ui-avatars.com/api/?name=" + fullName + "&background=random")
                .build();
    }
}
