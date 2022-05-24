package logic.entities;

import java.util.List;

public class Slot {
    private int id;
    private String name;

    public Slot(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Slot(List<Object> list) {
        this.id = (int) list.get(0);
        this.name = (String) list.get(1);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Slot{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
