function checkServerStatus(){
    console.log("Sprawdzanie statusu serwera...")
    const url = "/api/status";
    const form_ok = document.getElementById("server-ok");
    const form_issues = document.getElementById("server-issues");
    const form_error = document.getElementById("server-error");
    const form_checking = document.getElementById("server-checking");

    const request = new XMLHttpRequest();

    request.open('GET', url, true);

    request.onload = function () {
    if (request.readyState === request.DONE) {
        if (request.status === 200) {
            const response = JSON.parse(request.responseText);
            if(response.status === "OK"){
                hideElement(form_checking);
                showElement(form_ok);
            }else {
                hideElement(form_checking);
                showElement(form_issues);
            }
        }else {
           hideElement(form_checking);
           showElement(form_error);
        }
            console.log("Status serwera: " + request.status);
        }
    }
    let i = 0;
    for(;;) {
        i++;
        try {
            request.send();
            break;
        } catch (e) {
            if(i >= 3) {
                hideElement(form_checking);
                showElement(form_error);
                break;
            }
        }
    }
}