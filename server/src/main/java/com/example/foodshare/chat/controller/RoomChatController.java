package com.example.foodshare.chat.controller;import java.util.List;import org.springframework.http.HttpStatus;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.GetMapping;import org.springframework.web.bind.annotation.PathVariable;import org.springframework.web.bind.annotation.PostMapping;import org.springframework.web.bind.annotation.RequestBody;import org.springframework.web.bind.annotation.RequestMapping;import org.springframework.web.bind.annotation.RequestParam;import org.springframework.web.bind.annotation.RestController;import org.springframework.web.server.ResponseStatusException;import com.example.foodshare.chat.dto.ChatRoomCreationDto;import com.example.foodshare.chat.service.ChatRoomService;import com.example.foodshare.domain.ChatRoom;@RestController@RequestMapping("/api")public class RoomChatController {	ChatRoomService chatRoomService;	//채팅방 목록 조회 // 리스트	@GetMapping("/ListRooms")	public ResponseEntity<List<ChatRoom>> getUserRooms(@RequestParam String userId) {		List<ChatRoom> rooms = chatRoomService.findRoomsByUserId(userId);		return ResponseEntity.ok(rooms);	}	// 채팅방 생성	@PostMapping("/createRoom")	public ResponseEntity<ChatRoom> createRoom(@RequestBody ChatRoomCreationDto chatRoomCreationDto) {		ChatRoom newRoom = chatRoomService.createChatRoom(chatRoomCreationDto.getFirstUserMobileNumber(),			chatRoomCreationDto.getSecondUserMobileNumber());		return new ResponseEntity<>(newRoom, HttpStatus.CREATED);	}	// 채팅방 상세 조회	@GetMapping("/detailRoom/{roomId}")	public ResponseEntity<ChatRoom> getRoom(@PathVariable String roomId, @RequestParam String userId) {		ChatRoom room = chatRoomService.findRoomByIdAndUserId(roomId, userId)			.orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "채팅방 오류 "));		return ResponseEntity.ok(room);	}}