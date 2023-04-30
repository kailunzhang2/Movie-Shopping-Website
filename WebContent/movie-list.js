let sorting = "titleRating";
let order1 = "ASC";
let order2 = "ASC";
let pagesize = 10;
let pagenumber = 1;
let count = 10;

function getParameterByName(target) {
    let url = window.location.href;
    target = target.replace(/[\[\]]/g, "\\$&");

    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

const form = document.querySelector("form");
form.addEventListener("submit", (event) => {
    const data = new FormData(form);
    let output = "";
    for (const entry of data) {
        output = `${output}${entry[1]},`;
    };
    sorting = output.split(",")[0];
    order1 = output.split(",")[1];
    order2 = output.split(",")[2];
    pagesize = output.split(",")[3];
    handleButtonResult(sorting, order1, order2, pagesize, 1);
    event.preventDefault();
}, false);

const prev = document.querySelector("#prev");
const next = document.querySelector('#next');

prev.addEventListener('click', updatePrevButton);
next.addEventListener('click', updateNextButton);

function updatePrevButton() {
    if(pagenumber > 1) pagenumber -= 1;
    handleButtonResult(sorting, order1, order2, pagesize, pagenumber);
}
function updateNextButton() {
    console.log("pagenumber: " + pagenumber);
    pagenumber += 1;
    if(pagenumber <= (count / pagesize) + 1){
        handleButtonResult(sorting, order1, order2, pagesize, pagenumber);
    }else{
        pagenumber -= 1;
    }

}

function additem(button){
    $.ajax({
        dataType: "json",
        method: "POST",
        url: "api/CartServlet",
        data:  {"id": button["id"], action: "add"},
    })
    window.alert("added");
}

function handleStarResult(resultData) {
    let movieTableBodyElement = jQuery("#movie_table_body");
    movieTableBodyElement.html("");

    if(resultData[0]["moviesearch"].localeCompare("yes") === 0)
    {
        if (resultData[0]["sorting"].localeCompare("titleRating") === 0)
            $("#sortingChoice1").prop("checked", true);
        else
            $("#sortingChoice2").prop("checked", true);

        if (resultData[0]["order1"].localeCompare("ASC") === 0)
            $("#orderChoice1").prop("checked", true);
        else
            $("#orderChoice2").prop("checked", true);

        if (resultData[0]["order2"].localeCompare("ASC") === 0)
            $("#orderChoice3").prop("checked", true);
        else
            $("#orderChoice4").prop("checked", true);

        if(resultData[0]["limit"].localeCompare("10") === 0)
            $("#pageChoice1").prop("checked", true);
        else if(resultData[0]["limit"].localeCompare("25") === 0)
            $("#pageChoice2").prop("checked", true);
        else if(resultData[0]["limit"].localeCompare("50") === 0)
            $("#pageChoice3").prop("checked", true);
        else if(resultData[0]["limit"].localeCompare("100") === 0)
            $("#pageChoice4").prop("checked", true);

        pagenumber = (String)(parseInt(resultData[0]["offset"]) + parseInt(pagesize)) / parseInt(pagesize);
    }
    for (let i = 1; i < resultData.length; i++) {
        let cartid = resultData[i]['movie_id'];
        let rowHTML = "";
        rowHTML += "<tr>";

        rowHTML += "<th>" +
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">' + resultData[i]["movie_name"] + '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["director"] + "</th>";

        rowHTML += "<th>";
        let genres = resultData[i]["genres"].split(",")
        rowHTML += '<a href="movie-list.html?genre=' + genres[0] + '">' + genres[0] + '</a>';
        for(let i = 1; i < genres.length; i++){
            rowHTML += ', <a href="movie-list.html?genre=' + genres[i] + '">' + genres[i] + '</a>';
        }
        rowHTML += "</th>";

        rowHTML += "<th>";
        let starsID = resultData[i]["starID"].split(',');
        let starsName = resultData[i]["starsName"].split(',')
        rowHTML += '<a href="single-star.html?id=' + starsID[0] + '">' + starsName[0] + '</a>';
        for(let i = 0; i < starsID.length; i++){
            rowHTML += ', <a href="single-star.html?id=' + starsID[i] + '">' + starsName[i] + '</a>';
        }
        rowHTML += "</th>";

        rowHTML += "<th>" + resultData[i]["rating"] + "</th>";
        rowHTML += "<th>" + "<button id=" + cartid + " onclick=additem(" + cartid + ") > ADD </button>" + "</th>";

        rowHTML += "</tr>";
        count = resultData[i]["count"];
        movieTableBodyElement.append(rowHTML);
    }
}

let genreName = getParameterByName('genre');

let titleName = getParameterByName('title');
let yearName = getParameterByName('year');
let directorName = getParameterByName('director');
let starName = getParameterByName('star');
let search = getParameterByName('search');

function handleButtonResult(sorting, order1, order2, pagesize, pagenumber) {
    let dataString = {"sorting": sorting,"order1": order1, "order2": order2,
        "jump": "no", "limit": pagesize, "offset": (pagenumber - 1) * pagesize};
    if (search != null){
        jQuery.ajax({
            dataType: "json",
            method: "GET",
            url: "api/movies-search?title=" + titleName
                + "&year=" + yearName
                + "&director=" + directorName
                + "&star=" + starName,
            data: dataString,
            success: (resultData) => handleStarResult(resultData)
        });
    }
    else if (genreName != null){
        jQuery.ajax({
            dataType: "json",
            method: "GET",
            url: "api/single-genre?genre=" + genreName,
            data: dataString,
            success: (resultData) => handleStarResult(resultData)
        });
    }
    else if (titleName != null){
        jQuery.ajax({
            dataType: "json",
            method: "GET",
            url: "api/single-title?title=" + titleName,
            data: dataString,
            success: (resultData) => handleStarResult(resultData)
        });
    }
    else{
        jQuery.ajax({
            dataType: "json",
            method: "GET",
            url: "api/movies-search",
            success: (resultData) => handleStarResult(resultData)
        });
    }
}

handleButtonResult(sorting, order1, order2, pagesize, pagenumber);