package Parser;
import java.util.ArrayList;;
import java.util.List;

public class Movie {

    private String name;

    private int year;

    private String id;

    private String director;

    List<Genre> genres;

    List<Star> stars;

    public Movie(){
        genres = new ArrayList<>();
        stars = new ArrayList<>();
    }

    public Movie(String name, int year, String id,String director) {
        this.name = name;
        this.year = year;
        this.id  = id;
        this.director = director;

    }
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getYear() {
        return this.year;
    }

    public void setYear(int year) {
        this.year = year;
    }
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getDirector() {
        return this.director;
    }

    public void setDirector(String director) {
        this.director = director;
    }


    public void addGenre(Genre genre) {
        this.genres.add(genre);
    }

    public String getGenreString() {
        StringBuffer sb = new StringBuffer();

        sb.append('{');
        for(Genre g : this.genres){
            sb.append(g.toString());
            sb.append(',');
        }
        sb.append('}');
        return sb.toString();
    }


    public List<Genre> getGenreList(){
        return this.genres;
    }


    public void addStar(Star star) {
        this.stars.add(star);
    }

    public String getStarString() {
        StringBuffer sb = new StringBuffer();

        sb.append('{');
        for(Star s : this.stars){
            sb.append(s.toString());
            sb.append(',');
        }
        sb.append('}');
        return sb.toString();
    }
    public List<Star> getStarsList(){
        return this.stars;
    }



    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Movie Details - ");
        sb.append("ID:" + getId());
        sb.append(", ");
        sb.append("Title:" + getName());
        sb.append(", ");
        sb.append("Director:" + getDirector());
        sb.append(", ");
        sb.append("Year:" + getYear());
        sb.append(", ");
        sb.append("Genres:" + getGenreString());
        sb.append(", ");
        sb.append(("Stars:" + getStarString()));
        sb.append(".");
        return sb.toString();
    }
}