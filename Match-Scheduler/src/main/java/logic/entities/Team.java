package logic.entities;

import java.util.List;
import java.util.Objects;

public class Team {
    private String name;
    private int id;

    public Team(int id) {
        this.id = id;
    }

    public Team(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public Team(List<Object> list) {
        this.name = (String) list.get(0);
        this.id = (int) list.get(1);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setPosition(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Team{");
        sb.append("name='").append(name).append('\'');
        sb.append(", id=").append(id);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return id == team.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
