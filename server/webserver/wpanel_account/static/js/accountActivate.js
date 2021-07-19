const formMessage = $("#form-message")
const urlParams = new URLSearchParams(window.location.search);
const accountName = $("#accountName");
const phoneNumber = $("#phoneNumber");
const newPassword = $("#newPassword");
const repeatedPassword = $("#repeatedPassword");

function activateAccount(){
    const request = new XMLHttpRequest();

    request.open('POST', '/auth/activate', true);
    request.setRequestHeader("Content-Type", "application/json");

    request.onload = function () {
             if (request.readyState === request.DONE) {
                if (request.status === 201) {
                    const obj = JSON.parse(request.responseText);
                    document.getElementById("form-message").style.color = "green";
                    $("#form-message").html(obj.message);
                    setTimeout(function(){
                        window.location.href = '/login';
                     }, 5000)
                }else if (request.status === 404){
                    const obj = JSON.parse(request.responseText);
                    document.getElementById("form-message").style.color = "red";
                    $("#form-message").html(obj.message);
            }else {
                $("#form-message").html("Server have a some issues with this request, we're sorry. Try again later");
            }
        }
    };

    const requestStruct = {
        token: urlParams.get("token"),
        name: accountName.val(),
        number: phoneNumber.val(),
        password: newPassword.val()
    }

    request.send(JSON.stringify(requestStruct));
}

$("#submit").click(function () {
    formMessage.css("color", "red");

    if(accountName.val().length < 0b100){
        formMessage.html("Nazwa użytkownika musi być dłuższa niż 4 znaki");
    }

    if(phoneNumber.val().length === 0b0){
        formMessage.html("Pole numeru telefonu nie moze byc puste");
    }

    if(newPassword.val().length < 0b1000){
        formMessage.html("Hasło musi być dluższe niż 8 znaków");
    }

    if(repeatedPassword.val() !== newPassword.val()){
        formMessage.html("Hasło musi być takie same");
    }

    activateAccount();
})