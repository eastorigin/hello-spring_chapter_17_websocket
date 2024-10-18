package com.ktdsuniversity.edu.hello_spring.ws.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.ktdsuniversity.edu.hello_spring.bbs.dao.BoardDao;
import com.ktdsuniversity.edu.hello_spring.bbs.vo.BoardVO;

/**
 * /ws로 통신하는 모든 요청들을 처리
 * 특정 사용자가 전송한 메시지를 /ws에 접속한 모든 클라이언트에게 전달하는 역할 수행
 * 일종의 브로커 역할
 */
@Component
public class HelloSpringWebSocketHandler extends TextWebSocketHandler {

	private static final Logger logger = LoggerFactory.getLogger(HelloSpringWebSocketHandler.class);
	
	@Autowired
	private BoardDao boardDao;
	
	/**
	 * 웹소켓에 연결되어 있는 사용자(WebSocketSession)를 관리하는 변수
	 * Key: 접속된 사용자의 이메일 (PK)
	 * Value: 웹소켓 session 정보
	 */
	private Map<String, WebSocketSession> connectedSessionMap;
	
	/**
	 * 다 대 다 통신을 위한 세션 그룹
	 * 참여자 모두 그룹에서 나갔다면, 해당 그룹아이디를 그룹에서 제거한다
	 * 
	 * Key: 그룹 아이디
	 * Value: 그룹 참여자 이메일(PK) 목록
	 */
	private Map<String, List<String>> sessionGroupMap;
	
	/**
	 * JSON <--> JavaObject 변환
	 */
	private Gson gson;
	
	public HelloSpringWebSocketHandler() {
		this.connectedSessionMap = new HashMap<>();
		this.sessionGroupMap = new HashMap<>();
		this.gson = new Gson();
	}
	
	/**
	 * 소켓에 연결된 사용자가 /ws로 메세지를 보내면 handleTextMessage가 받는다.
	 * 원하는 사용자에게 메시지를 전송해준다.
	 * @param session : text data를 보낸 접속자의 정보
	 * @param message : 서버로 보내진 text data
	 */
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		
		// message.getPayload() => 사용자가 보낸 텍스트 메시지를 꺼낸다
		String payload = message.getPayload();
		
		// payload에서 email을 추출
		// payload에서 action을 추출
		// payload에서 message를 추출
		// --> payload(JSON 형태)를 Map으로 변환
		// 		--> Gson Library 필요
		Map<String, String> payloadMap = this.gson.fromJson(payload, Map.class);
		String action = payloadMap.get("action");
		String email = payloadMap.get("email");
		String receiveMessage = payloadMap.get("message");
		
		if(action.equals("LOGIN")) {
			// session에 접속함.
			
			boolean loggedSession = this.connectedSessionMap.containsKey(email);
			this.connectedSessionMap.put(email, session);
			
			if(!loggedSession) {
				
				// 모든 사용자에게 전송할 메시지
				Map<String, String> textMessageMap = new HashMap<>();
				textMessageMap.put("sender", email);
				textMessageMap.put("action", action);
				textMessageMap.put("message", receiveMessage);
				
				sendToOtherSessions(textMessageMap, session);
			}
		}else if(action.equals("REPLY_ALARAM")) {
			
			int boardId = Integer.parseInt(payloadMap.get("boardId"));
			String url = payloadMap.get("url");
			
			BoardVO boardVO = this.boardDao.selectOneBoard(boardId);
			String boardWriter = boardVO.getEmail();
			
			Map<String, String> textMessageMap = new HashMap<>();
			textMessageMap.put("action", "REPLY_ALARAM");
			textMessageMap.put("message", receiveMessage);
			textMessageMap.put("url", url);
			
			sendToOneSession(textMessageMap, boardWriter);
		}
		
		else if(action.equals("REQUEST_CHAT")) {
			Map<String, String> textMessageMap = new HashMap<>();
			textMessageMap.put("action", "REQUEST_CHAT");
			textMessageMap.put("message", receiveMessage);
			textMessageMap.put("from", payloadMap.get("email"));
			textMessageMap.put("to", payloadMap.get("to"));
			
			String groupId = UUID.randomUUID().toString();
			textMessageMap.put("groupId", groupId);
			
			sendToOneSession(textMessageMap, payloadMap.get("to"));
		}
		else if(action.equals("RESPONSE_CHAT")) {
			
			Map<String, String> textMessageMap = new HashMap<>();
			textMessageMap.put("action", "RESPONSE_CHAT");
			textMessageMap.put("isConnect", payloadMap.get("isConnect"));
			textMessageMap.put("from", payloadMap.get("email"));
			textMessageMap.put("to", payloadMap.get("to"));
			
			// isConnect가 "true"일 때, 두 사용자를 한 그룹으로 묶어준다
			String isConnect = payloadMap.get("isConnect");
			if(isConnect.equals("true")) {
				String groupId = payloadMap.get("groupId");
				
				textMessageMap.put("groupId", groupId);
				
				if(this.sessionGroupMap.containsKey(groupId)) {
					this.sessionGroupMap.get(groupId).add(payloadMap.get("email"));
				}else {
					List<String> groupMember = new ArrayList<>();
					groupMember.add(payloadMap.get("email"));
					groupMember.add(payloadMap.get("to"));
					
					this.sessionGroupMap.put(groupId, groupMember);
				}
			}
			sendToOneSession(textMessageMap, payloadMap.get("to"));
		}
		else if(action.equals("SEND_CHAT")) {
			
			Map<String, String> textMessageMap = new HashMap<>();
			textMessageMap.put("action", "SEND_CHAT");
			textMessageMap.put("sender", payloadMap.get("sender"));
			textMessageMap.put("groupId", payloadMap.get("groupId"));
			textMessageMap.put("chatMessage", payloadMap.get("chatMessage"));
			
			sendToGroupMembers(payloadMap.get("groupId"), textMessageMap);
		}
		else if(action.equals("QUIT_CHAT")) {
			
			String sender = payloadMap.get("sender");
			String groupId = payloadMap.get("groupId");
			
			Map<String, String> textMessageMap = new HashMap<>();
			textMessageMap.put("action", "QUIT_CHAT");
			textMessageMap.put("sender", sender);
			textMessageMap.put("groupId", payloadMap.get("groupId"));
			
			this.sessionGroupMap.get(payloadMap.get("groupId"))
								.removeIf(groupEmail -> groupEmail.equals(sender));
			
			if(this.sessionGroupMap.get(groupId).size() == 0) {
				this.sessionGroupMap.remove(groupId);
			}
			else {
				sendToGroupMembers(payloadMap.get("groupId"), textMessageMap);
			}
		}
	}
	
	/**
	 * 전송자를 제외한 모든 세션에게 메세지를 전달한다
	 * @param textMessageMap : 세션에 보낼 메세지
	 * @param session : 전송자
	 */
	private void sendToOtherSessions(Map<String, String> textMessageMap, WebSocketSession session) {
		
		// 맵을 JSON으로 바꿔서 TextMessage 객체로 만든다
		TextMessage textMessage = new TextMessage(this.gson.toJson(textMessageMap));
				
		// 모든 사용자에게 알림을 전송
		this.connectedSessionMap.entrySet()
								.stream()
								.filter(entry -> entry.getValue() != session)
								.forEach(entry -> {
									try {
										WebSocketSession each = entry.getValue();
										if(each.isOpen()) { // 연결이 끊어지지 않았다면
											/*
											* 세션 호출 직렬화
											* 한 세션이 동시에 여러가지를 주고 받게되었을 경우
											* 실패가 발생하고 연결을 강제로 끊어버리게 된다.
											* 순차로 실행될 수 있도록 직렬화한다.
											*/
											synchronized (each) {
												each.sendMessage(textMessage);
											}
										}
									} catch (IOException e) {
										logger.error(e.getMessage(), e);
									}
								});
	}
	
	private void sendToOneSession(Map<String, String> textMessageMap, String receiverEmail) {
		
		// 1. receiverEmail 사용자가 로그인해 있는가?
		if(!this.connectedSessionMap.containsKey(receiverEmail)) {
			// receiverEmail 사용자가 로그인 하지 않았음
			// TODO 일괄 전송 준비
			return;
		}
		
		TextMessage textMessage = new TextMessage(this.gson.toJson(textMessageMap));
		WebSocketSession session = this.connectedSessionMap.get(receiverEmail);
		if(session.isOpen()) {
			try {
				synchronized (session) {
					session.sendMessage(textMessage);
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	private void sendToGroupMembers(String groupId, Map<String, String> textMessageMap) {
		TextMessage textMessage = new TextMessage(this.gson.toJson(textMessageMap));
		
		this.sessionGroupMap.get(groupId)
							.stream()
							.map(email -> this.connectedSessionMap.get(email))
							.forEach(session -> {
								if(session.isOpen()) {
									try {
										synchronized (session) {
											session.sendMessage(textMessage);
										}
									} catch (IOException e) {
										logger.error(e.getMessage(), e);
									}
								}
							});;
	}
}
