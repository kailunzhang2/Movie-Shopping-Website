function additem(button){
    $.ajax({
        dataType: "json",
        method: "POST",
        url: "api/CartServlet",
        data:  {"id": button[0]["id"], action: "add"},
        success: window.alert("added")
    })
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/CartServlet",
        success: (resultData) => handleStarResult(resultData)
    });
}

function subitem(button){
    $.ajax({
        dataType: "json",
        method: "POST",
        url: "api/CartServlet",
        data:  {"id": button[1]["id"], action: "sub"},
        success: window.alert("reduced")
    })
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/CartServlet",
        success: (resultData) => handleStarResult(resultData)
    });
}

function delitem(button){
    $.ajax({
        dataType: "json",
        method: "POST",
        url: "api/CartServlet",
        data:  {"id": button[2]["id"], action: "delete"},
        success: window.alert("deleted")
    })
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/CartServlet",
        success: (resultData) => handleStarResult(resultData)
    });
}

function handleStarResult(resultData) {

    let cartTableBodyElement = jQuery("#cart_table_body");
    cartTableBodyElement.html("");
    let rowHTML = "";

    console.log(resultData["previousItems"]);
    for (let i = 0; i < Math.min(10, resultData["previousItems"].length); i++) {
        rowHTML += "<tr>";

        let carted = resultData["previousItems"][i]["movieId"]
        //rowHTML += "<th>" + resultData[i]["movieId"] +"</th>";
        rowHTML += "<th>" + resultData["previousItems"][i]["movieTitle"] + "</th>";
        rowHTML += "<th>" + resultData["previousItems"][i]["quantity"] + "</th>";
        rowHTML += "<th>" + resultData["previousItems"][i]["price"] + "</th>";
        rowHTML += "<th>" + resultData["previousItems"][i]["totalPrice"] + "</th>";
        //rowHTML += "<th>" + resultData[i]["total price"] + "</th>";
        rowHTML += "<th>" + "<button id=" + carted + " onclick=additem(" + carted + ") > ADD </button>" + "</th>";
        rowHTML += "<th>" + "<button id=" + carted + " onclick=subitem(" + carted + ") > SUB </button>" + "</th>";
        rowHTML += "<th>" + "<button id=" + carted + " onclick=delitem(" + carted + ") > DEL </button>" + "</th>";
        console.log(carted)

        rowHTML += "</tr>";
    }

    cartTableBodyElement.append(rowHTML);
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/CartServlet",
    success: (resultData) => handleStarResult(resultData)
});
