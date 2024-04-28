package com.foodshare.notification.sse.controller;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.foodshare.chat.dto.ChatRoomDto;
import com.foodshare.chat.service.ChatRoomService;
import com.foodshare.chat.service.MongoQueryBuilder;
import com.foodshare.notification.sse.component.SseEmitters;
import com.foodshare.notification.sse.service.HeartbeatService;
import com.foodshare.notification.sse.service.SseEmitterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class SseController {

	private final SseEmitters sseEmitters;
	private final HeartbeatService heartbeatService;

	@Autowired
	ChatRoomService chatRoomService;
	@Autowired
	SseEmitterService sseEmitterService;
	@Autowired
	MongoQueryBuilder mongoQueryBuilder;
	public SseController(SseEmitters sseEmitters, HeartbeatService heartbeatService) {
		this.sseEmitters = sseEmitters;
		this.heartbeatService = heartbeatService;
	}

	@GetMapping(value = "/sse/connect/{userId}", produces = "text/event-stream")
	public ResponseEntity<SseEmitter> connect(@PathVariable String userId) {
		SseEmitter newEmitter = new SseEmitter(300_000L); // 5분 연결 유지

		sseEmitters.add(userId, newEmitter);

		newEmitter.onCompletion(() -> sseEmitters.remove(userId, newEmitter));
		newEmitter.onTimeout(() -> {
			newEmitter.complete();
			sseEmitters.remove(userId, newEmitter);
			sseEmitters.removeExistingEmitter(userId);
		});

		try {
			sseEmitters.sendEvent(newEmitter, "connect", "connected");

			long totalUnreadCount = mongoQueryBuilder.countTotalUnreadMessages(userId);
			sseEmitterService.sendUnreadCountUpdate(userId, totalUnreadCount);

		} catch (Exception e) {
			log.error("SSE 연결 오류: {}", e.getMessage());
			newEmitter.completeWithError(e);
			sseEmitters.remove(userId, newEmitter);
		}

		heartbeatService.startHeartbeat(newEmitter, 30, TimeUnit.SECONDS);

		return ResponseEntity.ok(newEmitter);
	}

}