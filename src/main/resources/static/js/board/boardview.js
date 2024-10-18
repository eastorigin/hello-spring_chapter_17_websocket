$().ready(function () {
  $(".chat-btn").on("click", function () {
    // 상대방에게 채팅을 요청
    var to = $(this).data("you-email");
    var me = $(".member-menu").data("email");

    requestChat(to, me);
  });
});
