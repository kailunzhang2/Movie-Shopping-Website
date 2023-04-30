/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function additem(button){
    console.log(button["id"]);
    $.ajax({
        dataType: "json",
        method: "POST",
        url: "api/CartServlet",
        data:  {"id": button["id"], action: "add"},
    })
    window.alert("added");
}

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "single_movie_info"
    let movieInfoElement = jQuery("#single_movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append("<p>" + resultData[0]["movie_title"] + " (" + resultData[0]["movie_year"] + ")</p>");

    console.log("handleResult: populating movie table from resultData" + resultData[0]['genres']);

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#single_movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    let cartid = resultData[0]['movie_id'];
    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML += "<th>" + resultData[0]["movie_director"] + "</th>";

    rowHTML += "<th>";
    let genres = resultData[0]["genres"].split(',');
    rowHTML += '<a href="movie-list.html?genre=' + genres[0] + '">' + genres[0] + '</a>';
    for(let i = 1; i < genres.length; i++){
        rowHTML += ', <a href="movie-list.html?genre=' + genres[i] + '">' + genres[i] + '</a>';
    }
    rowHTML += "</th>";


    rowHTML += "<th>";
    let starsID = resultData[0]["starID"].split(',');
    let starsName = resultData[0]["starsName"].split(',');
    rowHTML += '<a href="single-star.html?id=' + starsID[0] + '">' +  starsName[0] + '</a>';
    for(let i = 1; i < starsID.length; i++){
        rowHTML += ', <a href="single-star.html?id=' + starsID[i] + '">' +  starsName[i] + '</a>';
    }
    rowHTML += "</th>";

    rowHTML += "<th>" + resultData[0]["movie_rating"] + "</th>";
    rowHTML += "<th>" + "<button id=" + cartid + " onclick=additem(" + cartid + ") > ADD </button>" + "</th>";
    rowHTML += "</tr>";

    // Append the row created to the table body, which will refresh the page
    movieTableBodyElement.append(rowHTML);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    error: function(xhr, status, error) {
        var err = eval("(" + xhr.responseText + ")");
        alert(err.Message);
    },
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});