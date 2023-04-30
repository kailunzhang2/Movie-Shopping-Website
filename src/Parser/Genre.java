package Parser;

public class Genre {
    //create hashmap private
    private String name;

    private Integer id;
    private boolean broken;

    public Genre() {
    }

    public Genre(Integer id, String name) {
        this.name = name;
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            this.broken = true;
        } else {
            this.broken = false;
            this.name = name;
        }
    }

    public void setBroken(boolean b) {
        this.broken = b;
    }

    public boolean getBroken() {
        return this.broken;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ID:" + getId());
        sb.append("Name:" + getName());
        return sb.toString();
    }
}

