let search_form = $("#search_form");

function submitSearchForm(formSubmitEvent) {
    console.log("submit login form");
    formSubmitEvent.preventDefault();
    window.location.replace("movie-list.html?search=true&"+search_form.serialize()); // delete search=true
}
search_form.submit(submitSearchForm);