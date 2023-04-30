let employee_login_form = $("#employee_login_form");

function handleLoginResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    if (resultDataJson["status"] === "success") {
        window.location.replace("_dashboard/home.html");
    } else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#employee_login_error_message").text(resultDataJson["message"]);
    }
}


function submitLoginForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();
    console.log("check it");
    $.ajax(
        "api/_dash_login", {
            method: "POST",
            data: employee_login_form.serialize(),
            success: handleLoginResult
        }
    );
}

employee_login_form.submit(submitLoginForm);
