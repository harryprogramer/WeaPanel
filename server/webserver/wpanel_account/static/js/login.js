const submit_btn = document.getElementById("submit");
const spinner = document.getElementById("btn_spinner");
const username_form = document.getElementById("username");
const password_form = document.getElementById("password");

function login(username, password){
    const request = new XMLHttpRequest();

    const json = {
        "username": username,
        "password": password
    };

    request.open('POST', '/auth/login', true);
    request.setRequestHeader("Content-Type", "application/json")
    request.onload = function () {
    if (request.readyState === request.DONE) {
        spinner.classList.remove("fa-spinner");
        spinner.classList.remove("fa-spin");
        submit_btn.disabled = false;
        const form = $("#login-form")
        if (form[0].checkValidity() === false) {
          event.preventDefault()
          event.stopPropagation()
        }
        form.addClass('was-validated');
        if (request.status === 200) {
            const obj = JSON.parse(request.responseText);
            window.location.replace("../auth/performSession/" + obj.token);
        }else if (request.status === 401){
            const obj = JSON.parse(request.responseText);
            $("#form-message").html(obj.message);
        }else if(request.status === 501){
            const obj = JSON.parse(request.responseText);
            window.location.replace("../activate?token=" + obj.token);
        }else {
            $("#form-message").html("Auth server have a some issues with this request, we're sorry. Try again later");
        }
    }
    };
    spinner.classList.add("fa-spinner");
    spinner.classList.add("fa-spin");
    submit_btn.disabled = true;
    try {
        request.send(JSON.stringify(json));
    }catch (e) {
         $("#form-message").html("No connection with server");
    }
}

submit_btn.addEventListener("click", function(){
    login(username_form.value, password_form.value);
});



