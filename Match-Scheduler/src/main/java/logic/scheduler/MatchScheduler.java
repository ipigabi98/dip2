package logic.scheduler;

import logic.constraintSolvers.BasicConstraintSolver;
import logic.constraintSolvers.ConstraintSolver;
import logic.constraintSolvers.PhasedConstraintSolver;
import logic.constraintSolvers.breakCS.BR1ConstraintSolver;
import logic.constraintSolvers.breakCS.BR2ConstraintSolver;
import logic.constraintSolvers.capacityCS.CA1ConstraintSolver;
import logic.constraintSolvers.capacityCS.CA2ConstraintSolver;
import logic.constraintSolvers.capacityCS.CA3ConstraintSolver;
import logic.constraintSolvers.capacityCS.CA4ConstraintSolver;
import logic.constraintSolvers.fairnessCS.FA2ConstraintSolver;
import logic.constraintSolvers.gameCS.GA1ConstraintSolver;
import logic.constraintSolvers.separationCS.SE1ConstraintSolver;
import logic.constraintSplitters.FA2Splitter;
import logic.constraints.SuperConstraint;
import logic.constraints.breakConstraints.BR1;
import logic.constraints.breakConstraints.BR2;
import logic.constraints.capacityConstraints.CA1;
import logic.constraints.capacityConstraints.CA2;
import logic.constraints.capacityConstraints.CA3;
import logic.constraints.capacityConstraints.CA4;
import logic.constraints.fairnessConstraints.FA2;
import logic.constraints.gameConstraints.GA1;
import logic.constraints.separationConstraints.SE1;
import logic.entities.Match;
import logic.entities.Slot;
import logic.entities.Team;
import gurobi.*;
import logic.parser.ValidatorFileMaker;
import logic.wrappers.MatchDataWrapper;
import logic.wrappers.ObjectiveWrapper;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MatchScheduler {
    private List<Team> teams;
    private final List<Match> matches;
    private final Map<MatchDataWrapper, GRBVar> variableByMatch;
    private List<Slot> slots;
    private final Map<GRBVar, Integer> objectiveMap = new HashMap<>();
    private final Map<GRBVar, ObjectiveWrapper> objectiveFinalMap = new HashMap<>();
    private boolean isPhased;

    private GRBModel model;

    private List<BR1> br1List;
    private List<BR2> br2List;
    private List<CA1> ca1List;
    private List<CA2> ca2List;
    private List<CA3> ca3List;
    private List<CA4> ca4List;
    private List<FA2> fa2List;
    private List<GA1> ga1List;
    private List<SE1> se1List;

    public MatchScheduler() {
        this.teams = new ArrayList<>();
        this.matches = new ArrayList<>();
        this.variableByMatch = new HashMap<>();
        this.slots = new ArrayList<>();

        this.br1List = new ArrayList<>();
        this.br2List = new ArrayList<>();
        this.ca1List = new ArrayList<>();
        this.ca2List = new ArrayList<>();
        this.ca3List = new ArrayList<>();
        this.ca4List = new ArrayList<>();
        this.fa2List = new ArrayList<>();
        this.ga1List = new ArrayList<>();
        this.se1List = new ArrayList<>();
    }

    public void setPhased(boolean phased) {
        isPhased = phased;
    }

    public boolean isPhased() {
        return isPhased;
    }

    public byte[] optimize() {
        removeDuplicates();
        try {
            GRBEnv env = new GRBEnv(true );
            env.start();
            this.model = new GRBModel(env);

            init();

            if (this.isPhased) {
                addPhasedConstraints();
            }
            addBasicConstraints();
            addHardAndSoftConstraints();
            setObjective();

            /*System.out.println("TUNE STARTED");
            this.model.tune();
            System.out.println("GET BEST PARAMS");
            this.model.getTuneResult(0);
            System.out.println("OPTIMIZE/WRITE");
            this.model.write("optimized.prm");*/

            this.model.read("files/parameters/optimized.prm");
            this.model.optimize();

            //this.model.write("test8.lp");

            //print();

            /*printTable();
            writeResultToFile();
            printNotSatisfiedConstraints();*/

            ValidatorFileMaker fileMaker = new ValidatorFileMaker(this.variableByMatch);
            byte[] result = fileMaker.writeXMLResult();

            this.model.dispose();
            env.dispose();

            return result;

        } catch (GRBException | ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void init() {
        initMatches();
        initVariables();
        splitConstraints();
    }

    private void initMatches() {
        Team pos1 = this.teams.get(1);
        int size = this.teams.size();
        do {
            for (int i = 0; i < (size/2); i++) {
                Team t1 = this.teams.get(i);
                Team t2 = this.teams.get(size-1-i);
                Match newMatch = new Match(t1, t2);
                Match rematch = new Match(t2, t1);
                this.matches.add(newMatch);
                this.matches.add(rematch);
            }
            Team t = this.teams.remove(1);
            this.teams.add(t);
        } while (!this.teams.get(1).equals(pos1));
        for (int i = 0; i < this.matches.size(); i++) {
            this.matches.get(i).setIndex(i);
        }
    }

    private void initVariables() {
        try {
            for (int i = 0; i < this.slots.size(); i++) {
                for (Match m : matches) {
                    MatchDataWrapper.MatchDataWrapperBuilder builder = new MatchDataWrapper.MatchDataWrapperBuilder(i, m);
                    String homeTeam = String.valueOf(m.getHomeTeam().getId());
                    String awayTeam = String.valueOf(m.getAwayTeam().getId());
                    GRBVar x = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, i + "#" + homeTeam + "-" + awayTeam);
                    variableByMatch.put(builder.build(), x);
                }
            }
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    private void removeDuplicates() {
        this.br1List = (List<BR1>) getNoDuplicatesList(this.br1List);
        this.br2List = (List<BR2>) getNoDuplicatesList(this.br2List);
        this.ca1List = (List<CA1>) getNoDuplicatesList(this.ca1List);
        this.ca2List = (List<CA2>) getNoDuplicatesList(this.ca2List);
        this.ca3List = (List<CA3>) getNoDuplicatesList(this.ca3List);
        this.ca4List = (List<CA4>) getNoDuplicatesList(this.ca4List);
        this.fa2List = (List<FA2>) getNoDuplicatesList(this.fa2List);
        this.ga1List = (List<GA1>) getNoDuplicatesList(this.ga1List);
        this.se1List = (List<SE1>) getNoDuplicatesList(this.se1List);
    }

    private List<? extends SuperConstraint> getNoDuplicatesList(List<? extends SuperConstraint> list) {
        Set<SuperConstraint> uniqueElements = new HashSet<>(list);
        return new ArrayList<>(uniqueElements);
    }

    private void splitConstraints() {
        this.fa2List = (List<FA2>) new FA2Splitter(this.fa2List).splitConstraint();
    }

    private void addPhasedConstraints() throws GRBException {
        PhasedConstraintSolver solver = new PhasedConstraintSolver(this);
        this.model = solver.addPhasedConstraints(this.model);
    }

    private void addBasicConstraints() throws GRBException {
        BasicConstraintSolver solver = new BasicConstraintSolver(this);
        this.model = solver.addBCEveryTeamPlaysOncePerWeek(this.model);
        this.model = solver.addBCEveryTeamPlaysWithEachOppOnceAtHomeAndOnceAway(this.model);
    }

    private void addHardAndSoftConstraints() throws GRBException {
        ConstraintSolver br1ConstraintSolver = new BR1ConstraintSolver(this);
        this.model = br1ConstraintSolver.addHardConstraints(model);
        this.model = br1ConstraintSolver.addSoftConstraints(model);

        ConstraintSolver br2ConstraintSolver = new BR2ConstraintSolver(this);
        this.model = br2ConstraintSolver.addHardConstraints(model);
        this.model = br2ConstraintSolver.addSoftConstraints(model);

        ConstraintSolver ca1ConstraintSolver = new CA1ConstraintSolver(this);
        this.model = ca1ConstraintSolver.addHardConstraints(model);
        this.model = ca1ConstraintSolver.addSoftConstraints(model);

        ConstraintSolver ca2ConstraintSolver = new CA2ConstraintSolver(this);
        this.model = ca2ConstraintSolver.addHardConstraints(model);
        this.model = ca2ConstraintSolver.addSoftConstraints(model);

        ConstraintSolver ca3ConstraintSolver = new CA3ConstraintSolver(this);
        this.model = ca3ConstraintSolver.addHardConstraints(model);
        this.model = ca3ConstraintSolver.addSoftConstraints(model);

        ConstraintSolver ca4ConstraintSolver = new CA4ConstraintSolver(this);
        this.model = ca4ConstraintSolver.addHardConstraints(model);
        this.model = ca4ConstraintSolver.addSoftConstraints(model);

        ConstraintSolver fa2ConstraintSolver = new FA2ConstraintSolver(this);
        this.model = fa2ConstraintSolver.addHardConstraints(model);
        this.model = fa2ConstraintSolver.addSoftConstraints(model);

        ConstraintSolver ga1ConstraintSolver = new GA1ConstraintSolver(this);
        this.model = ga1ConstraintSolver.addHardConstraints(model);
        this.model = ga1ConstraintSolver.addSoftConstraints(model);

        ConstraintSolver se1ConstraintSolver = new SE1ConstraintSolver(this);
        this.model = se1ConstraintSolver.addHardConstraints(model);
        this.model = se1ConstraintSolver.addSoftConstraints(model);
    }

    public void addObjectiveTerm(GRBVar var, ObjectiveWrapper wrapper) {
        this.objectiveFinalMap.put(var, wrapper);
    }

    public void addObjectiveTerm(GRBVar var, int penalty) {
        this.objectiveMap.put(var, penalty);
    }

    private void setObjective() throws GRBException {
        GRBLinExpr expr = new GRBLinExpr();
        for (GRBVar var : this.objectiveMap.keySet()) {
            expr.addTerm(this.objectiveMap.get(var), var);
        }
        model.setObjective(expr, GRB.MINIMIZE);
    }

    public void print() throws GRBException {
        Map<Integer, List<Match>> result = new TreeMap<>();
        for (MatchDataWrapper mdw : variableByMatch.keySet()) {
            if (variableByMatch.get(mdw).get(GRB.DoubleAttr.X) > 0.1) {
                if (!result.containsKey(mdw.getRound())) {
                    result.put(mdw.getRound(), new ArrayList<>());
                }
                result.get(mdw.getRound()).add(mdw.getMatch());
            }
        }

        for (Integer i : result.keySet()) {
            System.out.println(i + ".forduló:");
            for (Match m : result.get(i)) {
                System.out.print("\t" + m.getHomeTeam().getName() + " - " + m.getAwayTeam().getName());
                System.out.println();
            }
            System.out.println();
        }
    }

    public void printTable() throws GRBException {
        StringBuilder header = new StringBuilder();
        header.append("+----------");
        for (Match match : matches) {
            header.append("----------");
        }
        int i1 = header.lastIndexOf("-");
        header.replace(i1, i1+1, "+");
        header.append("\n");
        header.append("|  Round  |");
        for (Match match : matches) {
            header.append("  ").append(match.getHomeTeam().getId()).append(" - ").append(match.getAwayTeam().getId()).append("  |");
        }
        header.append("\n");
        header.append("+----------");
        for (Match match : matches) {
            header.append("----------");
        }
        int i2 = header.lastIndexOf("-");
        header.replace(i2, i2+1, "+");

        for (int i = 1; i <= slots.size(); i++) {
            header.append("\n|  ").append(i).append("      |");
            for (Match match : matches) {
                for (MatchDataWrapper mdw : variableByMatch.keySet()) {
                    if (mdw.getRound() == (i-1) && mdw.getMatch().getIndex() == match.getIndex()) {
                        if (variableByMatch.get(mdw).get(GRB.DoubleAttr.X) > 0.2) {
                            header.append("    1    |");
                        } else {
                            header.append("    0    |");
                        }
                    }
                }
            }
            header.append("\n");
            header.append("+----------");
            for (Match match : matches) {
                header.append("----------");
            }
            int indexIn = header.lastIndexOf("-");
            header.replace(indexIn, indexIn+1, "+");
        }
        header.append("\n");
        System.out.println(header.toString());
    }

    public void writeResultToFile() throws GRBException, FileNotFoundException {
        StringBuilder header = new StringBuilder();
        header.append("Round;");
        for (Match match : matches) {
            header.append(match.getHomeTeam().getId()).append(" - ").append(match.getAwayTeam().getId()).append(";");
        }
        int i1 = header.lastIndexOf(";");
        header.replace(i1, i1 + 1, "");
        for (int i = 1; i <= slots.size(); i++) {
            header.append("\n");
            header.append(i).append(";");
            for (Match match : matches) {
                for (MatchDataWrapper mdw : variableByMatch.keySet()) {
                    if (mdw.getRound() == (i-1) && mdw.getMatch().getIndex() == match.getIndex()) {
                        if (variableByMatch.get(mdw).get(GRB.DoubleAttr.X) > 0.2) {
                            header.append("1;");
                        } else {
                            header.append("0;");
                        }
                    }
                }
            }
            int i2 = header.lastIndexOf(";");
            header.replace(i2, i2 + 1, "");
        }
        OutputStream os = new FileOutputStream("result.csv");
        PrintWriter w = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
        w.print(header.toString());
        w.flush();
        w.close();
    }

    public void printNotSatisfiedConstraints() throws GRBException {
        List<String> names = new ArrayList<>();
        for (GRBVar var : this.objectiveMap.keySet()) {
            if (var.get(GRB.DoubleAttr.X) > 0.1) {
                names.add(var.get(GRB.StringAttr.VarName));
            }
        }
        Collections.sort(names);
        for (String s : names) {
            System.out.println(s);
        }
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public void setSlots(List<Slot> slots) {
        if (slots == null || slots.isEmpty()) {
            int numberOfTeams = this.teams.size();
            int numberOfSlots = (numberOfTeams - 1)*2;

            List<Slot> newSlots = new ArrayList<>();
            for (int i = 0; i < numberOfSlots; i++) {
                Slot newSlot = new Slot(i, "Slot " + i);
                newSlots.add(newSlot);
            }

            this.slots = newSlots;
        } else {
            this.slots = slots;
        }
    }

    public List<BR1> getBr1List() {
        return br1List;
    }

    public void setBr1List(List<BR1> br1List) {
        this.br1List = br1List;
    }

    public List<BR2> getBr2List() {
        return br2List;
    }

    public void setBr2List(List<BR2> br2List) {
        this.br2List = br2List;
    }

    public List<CA1> getCa1List() {
        return ca1List;
    }

    public void setCa1List(List<CA1> ca1List) {
        this.ca1List = ca1List;
    }

    public List<CA2> getCa2List() {
        return ca2List;
    }

    public void setCa2List(List<CA2> ca2List) {
        this.ca2List = ca2List;
    }

    public List<CA3> getCa3List() {
        return ca3List;
    }

    public void setCa3List(List<CA3> ca3List) {
        this.ca3List = ca3List;
    }

    public List<CA4> getCa4List() {
        return ca4List;
    }

    public void setCa4List(List<CA4> ca4List) {
        this.ca4List = ca4List;
    }

    public List<FA2> getFa2List() {
        return fa2List;
    }

    public void setFa2List(List<FA2> fa2List) {
        this.fa2List = fa2List;
    }

    public List<GA1> getGa1List() {
        return ga1List;
    }

    public void setGa1List(List<GA1> ga1List) {
        this.ga1List = ga1List;
    }

    public List<SE1> getSe1List() {
        return se1List;
    }

    public void setSe1List(List<SE1> se1List) {
        this.se1List = se1List;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public Map<MatchDataWrapper, GRBVar> getVariableByMatch() {
        return variableByMatch;
    }
}