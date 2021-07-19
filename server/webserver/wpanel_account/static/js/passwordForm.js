const submit_btn = document.getElementById("submit");
const spinner = document.getElementById("btn_spinner");
const form_message = document.getElementById("form-message");

$("#submit").click(function () {
    submit_btn.disabled = true;
    spinner.classList.add("fa-spinner");
    spinner.classList.add("fa-spin");
    const emailForm = $("#email");
    if(emailForm.length !== 0){
        const request = new XMLHttpRequest();

        const jsonRequest = {
            email: emailForm.val(),
            token: null,
            password: null
        }

        request.open('POST', '/auth/reset/password', true);
        request.setRequestHeader("Content-Type", "application/json");
        request.onload = function () {
             if (request.readyState === request.DONE) {
                spinner.classList.remove("fa-spinner");
                spinner.classList.remove("fa-spin");
                submit_btn.disabled = false;
                if (request.status === 200) {
                    const obj = JSON.parse(request.responseText);
                    form_message.style.color = "green";
                    $("#form-message").html(obj.message);
                }else if (request.status === 400){
                    const obj = JSON.parse(request.responseText);
                    $("#form-message").html(obj.message);
            }else {
                $("#form-message").html("Server have a some issues with this request, we're sorry. Try again later");
            }
        }
    };

        request.send(JSON.stringify(jsonRequest));
    }
})
