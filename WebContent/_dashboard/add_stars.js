let add_stars_form = $("#add_star_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    console.log("handle adding star");
    console.log(resultDataString);
    let resultDataJson = resultDataString;

    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") {
        console.log("added star");
        $("#add_star_error_message").text(resultDataJson["successMessage"]);
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["errorMessage"]);
        $("#add_star_error_message").text(resultDataJson["errorMessage  "]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log(add_stars_form.serialize());
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/add_stars", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: add_stars_form.serialize(),
            success: handleLoginResult
        }
    );
}

// Bind the submit action of the form to a handler function
add_stars_form.submit(submitLoginForm);