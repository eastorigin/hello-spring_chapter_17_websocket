<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <title>Insert title here</title>
  </head>
  <body>
    <dialog class="chat-dialog">
      <div class="chat-history"></div>
      <div>
        <textarea class="chat-message"></textarea>
        <button type="button" class="send-chat-message-btn">전송</button>
        <button type="button" class="close-dialog-btn">끝내기</button>
      </div>
    </dialog>
    <div class="right-align">
      <ul
        class="horizontal-list member-menu"
        data-email="${sessionScope._LOGIN_USER.email}"
      >
        <c:choose>
          <c:when test="${empty sessionScope._LOGIN_USER}">
            <li>
              <a href="/member/regist">회원가입</a>
            </li>
            <li>
              <a href="/member/login">로그인</a>
            </li>
          </c:when>
          <c:otherwise>
            <li>
              ${sessionScope._LOGIN_USER.name}
              (${sessionScope._LOGIN_USER.email})
            </li>
            <li>
              <a href="/member/logout">로그아웃</a>
            </li>
            <li>
              <a href="/member/delete-me">탈퇴</a>
            </li>
          </c:otherwise>
        </c:choose>
      </ul>
    </div>
  </body>
</html>
