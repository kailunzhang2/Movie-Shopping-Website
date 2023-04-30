package edu.uci.ics.fabflixmobile.data.model;

import androidx.annotation.NonNull;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private final String name;
    private final String year;
    private final String id;
    private final String director;
    String stars;
    String genres;

    public Movie(String name, String year, String id, String director, String stars, String genres) {
        this.name = name;
        this.year = year;
        this.id = id;
        this.director = director;
        this.stars = stars;
        this.genres = genres;
    }

    public String getName() {
        return name;
    }

    public String getYear() {
        return year;
    }
    public String getId() {
        return id;
    }
    public String getDirector() {
        return director;
    }
    public String getStars(){
        String message = "";
        String[] temp = stars.split(",");
        if(temp.length > 3){
            message = temp[0] +", "+ temp[1] +", "+ temp[2];
        }
        else if(temp.length == 2){
            message = temp[0] +", "+ temp[1];
        }
        else if(temp.length == 1){
            message = temp[0];
        }
        return message;
    }
    public String getGenres(){
        String message = "";
        String[] temp = genres.split(",");
        if(temp.length >= 3){
            message = temp[0] +", "+ temp[1] +", "+ temp[2];
        }
        else if(temp.length == 2){
            message = temp[0] +", "+ temp[1];
        }
        else if(temp.length == 1){
            message = temp[0];
        }
        return message;
    }
    public String getAllStars(){return stars;}
    public String getAllGenres(){return genres;}

    @NonNull
    public String toString(){
        return name +":"+ year +":"+ id +":"+ director +":"+ getStars() +":"+ getGenres();
    }
}