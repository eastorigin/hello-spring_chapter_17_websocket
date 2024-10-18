function receiveHandler(socket) {
  if (socket) {
    // 웹 소켓 서버에서 메세지가 전달되었을 때 동작되는 이벤트
    socket.onmessage = function (message) {
      console.log(message);

      var receiveData = message.data;

      // JSON -> Object Literal 변경
      var receiveMessage = JSON.parse(receiveData);

      if (receiveMessage.action === "LOGIN") {
        alert(receiveMessage.message);
      } else if (receiveMessage.action === "REPLY_ALARAM") {
        if (confirm(receiveMessage.message)) {
          location.href = receiveMessage.url;
        }
      } else if (receiveMessage.action === "REQUEST_CHAT") {
        if (confirm(receiveMessage.message)) {
          // 1. dialog를 띄운다
          $(".chat-dialog").data("group-id", receiveMessage.groupId);
          $(".chat-dialog")[0].show();

          // 2. 요청을 수락하는 메세지를 대화 요청자에게 보낸다
          sendResponseForChat(
            receiveMessage.to,
            receiveMessage.from,
            receiveMessage.groupId,
            true
          );
        } else {
          // 요청을 거부하는 메세지를 대화 요청자에게 보낸다
          sendResponseForChat(
            receiveMessage.to,
            receiveMessage.from,
            receiveMessage.groupId,
            false
          );
        }
      } else if (receiveMessage.action === "RESPONSE_CHAT") {
        if (receiveMessage.isConnect === "true") {
          $(".chat-dialog").data("group-id", receiveMessage.groupId);
          $(".chat-dialog")[0].show();
        } else {
          alert("상대방이 대화를 거절했습니다");
        }
      } else if (receiveMessage.action === "SEND_CHAT") {
        $(".chat-dialog")[0].show();
        $(".chat-dialog").data("group-id", receiveMessage.groupId);

        var chat = $("<div class='chat'></div>");
        chat.append(
          `<div>${receiveMessage.sender} : ${receiveMessage.chatMessage}</div>`
        );

        // if (userEmail === receiveMessage.sender) {
        //   chat.css("text-align", left);
        // } else {
        //   chat.css("text-align", right);
        // }

        $(".chat-history").append(chat);
      } else if (receiveMessage.action === "QUIT_CHAT") {
        $(".chat-dialog")[0].show();
        $(".chat-dialog").data("group-id", receiveMessage.groupId);

        var chat = $("<div class='chat'></div>");
        chat.append(`<div>${receiveMessage.sender} : 퇴장했습니다</div>`);

        $(".chat-history").append(chat);
      }
    };
  }
}
