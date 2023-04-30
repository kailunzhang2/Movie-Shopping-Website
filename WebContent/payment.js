function handleResult(resultData)
{
    if (resultData["status"] === "success")
    {

        window.location.replace("confirmation.html");
    }
    else
    {
        $("#login_error_message").text(resultData['message']);
    }
}

function submitForm(formSubmitEvent)
{
    console.log("checking");
    formSubmitEvent.preventDefault();
    $.post("api/payment",
        $("#creditCard_form").serialize(),
        (resultData) => handleResult(resultData))
}

$("#creditCard_form").submit((event) => submitForm(event));