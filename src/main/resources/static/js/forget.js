$(function(){
    $("#verifyCodeBtn").click(getVerifyCode);
});

function getVerifyCode() {
    // js使用的是id，不是name
    // 因为是id选择器，还有其他类型的选择器
    var email = $("#your-email").val();

    if(!email) {
        alert("请先填写您的邮箱！");
        return false;
    }

    // 以下为jQuery代码
    $.get(
        CONTEXT_PATH + "/forget/code",
        {"email":email},
        // 匿名的回调函数
        // data为服务器返回给浏览器的数据
        function(data) {
            // 将data由字符串对象转化为js对象
            data = $.parseJSON(data);
            if(data.code == 0) {
                alert("验证码已发送至您的邮箱,请登录邮箱查看!");
            } else {
                alert(data.msg);
            }
        }
    );
}