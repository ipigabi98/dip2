package logic.wrappers;

import logic.entities.Match;

public class MatchDataWrapper {
    private final int round;
    private final Match match;

    public MatchDataWrapper(MatchDataWrapperBuilder builder) {
        this.round = builder.round;
        this.match = builder.match;
    }

    public int getRound() {
        return round;
    }

    public Match getMatch() {
        return match;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MatchDataWrapper{");
        sb.append("round=").append(round);
        sb.append(", match=").append(match);
        sb.append('}');
        return sb.toString();
    }

    public static class MatchDataWrapperBuilder {
        private final int round;
        private final Match match;

        public MatchDataWrapperBuilder(int round, Match match) {
            this.round = round;
            this.match = match;
        }

        public MatchDataWrapper build() {
            return new MatchDataWrapper(this);
        }
    }
}
