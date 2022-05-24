package logic.parser;

import logic.comparators.SortByRound;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBVar;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import logic.wrappers.MatchDataWrapper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ValidatorFileMaker {
    private Map<MatchDataWrapper, GRBVar> result;

    public ValidatorFileMaker(Map<MatchDataWrapper, GRBVar> result) {
        this.result = result;
    }

    public byte[] writeXMLResult() throws ParserConfigurationException, GRBException, TransformerException {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        // root element
        Element root = document.createElement("Solution");
        document.appendChild(root);

        // MetaData element
        Element metaData = document.createElement("MetaData");
        root.appendChild(metaData);

        // Games element
        Element games = document.createElement("Games");
        root.appendChild(games);

        List<MatchDataWrapper> sortedList = this.result.keySet().stream().sorted(new SortByRound()).collect(Collectors.toList());

        for (MatchDataWrapper mdw : sortedList) {
            if (this.result.get(mdw).get(GRB.DoubleAttr.X) > 0.2) {
                String homeTeam = mdw.getMatch().getHomeTeam().getName();
                String awayTeam = mdw.getMatch().getAwayTeam().getName();
                int slotId = mdw.getRound();
                games = createScheduledMatchElement(document, games, homeTeam, awayTeam, slotId);
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(bos);
        transformer.transform(domSource, result);
        return bos.toByteArray();
    }

    private Element createScheduledMatchElement(Document document, Element games, String homeTeam, String awayTeam, int slotId) {
        Element scheduledMatch = document.createElement("ScheduledMatch");

        Attr home = document.createAttribute("home");
        home.setValue(String.valueOf(homeTeam));
        scheduledMatch.setAttributeNode(home);

        Attr away = document.createAttribute("away");
        away.setValue(String.valueOf(awayTeam));
        scheduledMatch.setAttributeNode(away);

        Attr slot = document.createAttribute("slot");
        slot.setValue(String.valueOf(slotId));
        scheduledMatch.setAttributeNode(slot);

        games.appendChild(scheduledMatch);

        return games;
    }
}
