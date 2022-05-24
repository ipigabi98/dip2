package logic.entities;

import java.util.Objects;

public class Match implements Comparable<Match> {
    private Team homeTeam;
    private Team awayTeam;
    private int index;

    public Match(int index) {
        this.index = index;
    }

    public Match(Team homeTeam, Team awayTeam) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return index == match.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Match{");
        sb.append("homeTeam=").append(homeTeam);
        sb.append(", awayTeam=").append(awayTeam);
        sb.append(", index=").append(index);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(Match o) {
        return this.index - o.index;
    }
}
