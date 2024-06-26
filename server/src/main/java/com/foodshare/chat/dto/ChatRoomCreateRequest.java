package com.foodshare.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomCreateRequest {
	private String firstUserMobileNumber;
	private String secondUserMobileNumber;
	private String foodId;
}