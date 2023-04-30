package Parser;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Star {
    private String name;
    private String id;

    List<String> starNames;

    public Star(){
        starNames = new ArrayList<String>();
    }

    public Star(String id, String name) {
        this.name = name;
        this.id  = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addStar(String s) {
        this.starNames.add(s);
    }
    public String getStarString() {
        StringBuffer sb = new StringBuffer();

        Iterator<String> it = starNames.iterator();
        sb.append('{');
        for(String m : this.starNames){
            sb.append(m);
            sb.append(',');
        }
        sb.append('}');
        return sb.toString();
    }
    public List<String> getStarNames(){
        return this.starNames;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Star in Movie Details - ");
        sb.append("ID:" + getId());
        sb.append(", ");
        sb.append("Name:" + getName());
        sb.append(", ");
        sb.append("Star Names:" + getStarString());
        sb.append(".");

        return sb.toString();
    }


}

