var socket = undefined;

$().ready(function () {
  var userEmail = $(".member-menu").data("email");

  // 브라우저 렌더링이 끝나면 웹소켓에 연결한다
  // socket 변수 ==> 소켓과 연결되어있는 파이프
  socket = new SockJS("/ws");

  // 소켓 연결이 정상적으로 이루어졌을 때의 동작을 작성
  socket.onopen = function () {
    receiveHandler(socket);

    // 연결된 이후에 웹소켓 서버로 메시지를 전송한다!
    var sendMessage = {
      email: userEmail,
      action: "LOGIN",
      message: userEmail + "님이 접속했습니다!",
    };

    socket.send(JSON.stringify(sendMessage));
  };
});

function sendAlarmWriteReply(boardId) {
  var userEmail = $(".member-menu").data("email");
  var sendMessage = {
    email: userEmail,
    action: "REPLY_ALARAM",
    boardId: `${boardId}`,
    url: "/board/view?id=" + boardId,
    message: "새로운 댓글이 등록되었습니다. 확인하러 가시겠습니까?",
  };
  socket.send(JSON.stringify(sendMessage));
}

/**
 *
 * @param {*} to 채팅 받는 사람, 여기서 to 처음 시작
 * @param {*} me 채팅 보내는 사람
 */
function requestChat(to, me) {
  var sendMessage = {
    email: me,
    action: "REQUEST_CHAT",
    to: to,
    message: me + "회원이 대화를 요청했습니다. 수락하시겠습니까?",
  };

  socket.send(JSON.stringify(sendMessage));
}

/**
 *
 * @param {*} from 채팅 수락 응답을 받는 사람 (지가 요청을 해서 수락 메세지를 지가 받는거임)
 * @param {*} to 채팅 수락하 사람 (최초로 채팅 요청을 받은 사람)
 * @param {*} isConnect 모데 궁 안에 들어옴
 */
function sendResponseForChat(from, to, groupId, isConnect) {
  var sendMessage = {
    email: from,
    action: "RESPONSE_CHAT",
    to: to,
    isConnect: `${isConnect}`,
    groupId: groupId,
  };

  socket.send(JSON.stringify(sendMessage));
}

function sendChat(groupId, chatMessage) {
  var userEmail = $(".member-menu").data("email");

  var sendMessage = {
    sender: userEmail,
    action: "SEND_CHAT",
    groupId: groupId,
    chatMessage: chatMessage,
  };

  socket.send(JSON.stringify(sendMessage));
}

function quitChat(groupId) {
  var userEmail = $(".member-menu").data("email");

  var sendMessage = {
    sender: userEmail,
    action: "QUIT_CHAT",
    groupId: groupId,
  };

  socket.send(JSON.stringify(sendMessage));
}
