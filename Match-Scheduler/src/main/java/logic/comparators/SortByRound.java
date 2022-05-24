package logic.comparators;

import logic.wrappers.MatchDataWrapper;

import java.util.Comparator;

public class SortByRound implements Comparator<MatchDataWrapper> {

    @Override
    public int compare(MatchDataWrapper o1, MatchDataWrapper o2) {
        return o1.getRound() - o2.getRound();
    }
}
