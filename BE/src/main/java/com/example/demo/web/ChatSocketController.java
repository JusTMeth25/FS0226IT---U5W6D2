package com.example.demo.web;

import com.example.demo.dto.ApiError;
import com.example.demo.dto.MessageDto;
import com.example.demo.dto.ReceiptUpdate;
import com.example.demo.dto.SendMessageRequest;
import com.example.demo.model.MessageStatus;
import com.example.demo.service.ChatNotifier;
import com.example.demo.service.ChatService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

	private final ChatService chatService;
	private final ChatNotifier notifier;

	/**
	 * The client publishes on /app/chat.send. The sender is read from the session
	 * Principal, so a sender field in the payload would be ignored.
	 * The message is stored first and only then delivered to whoever is connected.
	 */
	@MessageMapping("/chat.send")
	public void send(@Valid @Payload SendMessageRequest request, Principal principal) {
		MessageDto saved = chatService.send(principal.getName(), request);
		notifier.sendMessage(saved);

		// The recipient had an open channel, so the message is delivered: record it and
		// move the tick on the sender's side.
		if (notifier.isOnline(saved.recipientUsername()) && chatService.markDelivered(saved.id())) {
			notifier.sendReceipt(saved.senderUsername(), new ReceiptUpdate(
					MessageStatus.DELIVERED,
					saved.recipientUsername(),
					List.of(saved.id()),
					Instant.now()));
		}
	}

	@MessageExceptionHandler
	public void handleException(Exception exception, Principal principal) {
		if (principal == null) {
			return;
		}
		notifier.sendError(principal.getName(), ApiError.of(400, "MessageError", exception.getMessage()));
	}
}
