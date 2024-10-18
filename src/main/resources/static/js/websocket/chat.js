$().ready(function () {
  $(".send-chat-message-btn").on("click", function () {
    var groupId = $(".chat-dialog").data("group-id");
    var chatMessage = $(".chat-message").val();

    $(".chat-message").val("");

    sendChat(groupId, chatMessage);
  });

  $(".close-dialog-btn").on("click", function () {
    var groupId = $(".chat-dialog").data("group-id");
    $(".chat-message").removeData("group-id");
    $(".chat-dialog")[0].close();

    quitChat(groupId);
  });
});
