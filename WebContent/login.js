let login_form = $("#login_form");

function handleLoginResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    if (resultDataJson["status"] === "success") {
        window.location.replace("index.html");
    } else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#login_error_message").text(resultDataJson["message"]);
    }
}


function submitLoginForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();
    let dataString = login_form.serialize();
    dataString["Android"] = "false";
    $.ajax(
        "api/login", {
            method: "POST",
            data: dataString,
            success: handleLoginResult,
            error: $("#login_error_message").text("no recaptcha")
        }
    );
}

login_form.submit(submitLoginForm);

