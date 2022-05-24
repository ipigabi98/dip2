package rest.data.utilities;

import logic.entities.Team;
import logic.parser.MyXMLParser;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import rest.data.dto.ResultDto;
import rest.data.dto.constraints.*;
import rest.data.exceptions.InvalidXMLException;
import rest.data.exceptions.TryAgainLaterException;
import rest.data.requests.UploadManualInputsRequest;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class XMLUtility {

    public static Document createXMLContent(UploadManualInputsRequest request) throws ParserConfigurationException {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        // Instance element
        Element instance = document.createElement("Instance");
        document.appendChild(instance);

        // Resources element
        Element resources = document.createElement("Resources");
        instance.appendChild(resources);

        // Constraints element
        Element constraints = document.createElement("Constraints");
        instance.appendChild(constraints);

        // BreakConstraints element
        Element breakConstraintsElement = document.createElement("BreakConstraints");
        constraints.appendChild(breakConstraintsElement);

        // CapacityConstraints element
        Element capacityConstraintsElement = document.createElement("CapacityConstraints");
        constraints.appendChild(capacityConstraintsElement);

        // FairnessConstraints element
        Element fairnessConstraintsElement = document.createElement("FairnessConstraints");
        constraints.appendChild(fairnessConstraintsElement);

        // GameConstraints element
        Element gameConstraintsElement = document.createElement("GameConstraints");
        constraints.appendChild(gameConstraintsElement);

        // SeparationConstraints element
        Element separationConstraintsElement = document.createElement("SeparationConstraints");
        constraints.appendChild(separationConstraintsElement);

        setRoundRobin(request.getRoundRobin(), instance, document);
        setTeams(request.getTeams(), resources, document);

        setBR1(request.getBr1(), breakConstraintsElement, document);
        setBR2(request.getBr2(), breakConstraintsElement, document);

        setCA1(request.getCa1(), capacityConstraintsElement, document);
        setCA2(request.getCa2(), capacityConstraintsElement, document);
        setCA3(request.getCa3(), capacityConstraintsElement, document);
        setCA4(request.getCa4(), capacityConstraintsElement, document);

        setFA2(request.getFa2(), fairnessConstraintsElement, document);

        setGA1(request.getGa1(), gameConstraintsElement, document);

        setSE1(request.getSe1(), separationConstraintsElement, document);

        return document;
    }

    public static byte[] writeDocumentToByteArray(Document document) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(bos);
        transformer.transform(domSource, result);
        return bos.toByteArray();
    }

    public static List<ResultDto> getResultsFromByteArray(byte[] result, Long dataId) throws IOException, TryAgainLaterException, InvalidXMLException, ParserConfigurationException, SAXException {
        String path = "files/temporary/from_byte_" + dataId + ".xml";
        File file = new File(path);
        if (!file.createNewFile()) {
            throw new TryAgainLaterException();
        };
        OutputStream os = new FileOutputStream(file);
        os.write(result);
        os.close();

        List<ResultDto> resultList = parseResult(file);

        if (!file.delete()) {
            throw new TryAgainLaterException();
        }
        return resultList;
    }

    private static List<ResultDto> parseResult(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();

        Element solutionElement = (Element) doc.getElementsByTagName("Solution").item(0);
        Element gamesElement = (Element) solutionElement.getElementsByTagName("Games").item(0);
        NodeList scheduledMatch = gamesElement.getElementsByTagName("ScheduledMatch");

        List<ResultDto> resultList = new ArrayList<>();
        for (int i = 0; i < scheduledMatch.getLength(); i++) {
            Element match = (Element) scheduledMatch.item(i);
            String round = match.getAttribute("slot");
            String homeTeam = match.getAttribute("home");
            String awayTeam = match.getAttribute("away");
            ResultDto newResultDto = new ResultDto(round, homeTeam, awayTeam);

            resultList.add(newResultDto);
        }

        return resultList;
    }

    private static void setRoundRobin(String value, Element parent, Document document) {
        // Structure element
        Element structure = document.createElement("Structure");
        parent.appendChild(structure);

        // Format element
        Element format = document.createElement("Format");
        structure.appendChild(format);

        // Format element
        Element gameMode = document.createElement("gameMode");
        format.appendChild(gameMode);

        gameMode.setTextContent(value);
    }

    private static void setTeams(List<Team> teams, Element parent, Document document) {
        // Teams element
        Element teamsElement = document.createElement("Teams");
        parent.appendChild(teamsElement);

        for (Team team : teams) {
            Element teamElement = document.createElement("team");

            addAttribute("id", team.getId(), teamElement, document);
            addAttribute("name", team.getName(), teamElement, document);

            teamsElement.appendChild(teamElement);
        }
    }

    private static void setBR1(List<BR1Dto> br1, Element parent, Document document) {
        if (br1 == null) {
            return;
        }

        for (BR1Dto data : br1) {
            Element breakConstraintElement = document.createElement("BR1");

            //addAttribute("id", br1Dto.getId(), breakConstraintElement, document);
            addAttribute("intp", data.getIntp(), breakConstraintElement, document);
            addAttribute("mode2", data.getMode2(), breakConstraintElement, document);
            addAttribute("penalty", data.getPenalty(), breakConstraintElement, document);
            addAttribute("slots", convertListToString(data.getSlots()), breakConstraintElement, document);
            addAttribute("teams", data.getTeams(), breakConstraintElement, document);
            addAttribute("type", data.getType(), breakConstraintElement, document);

            parent.appendChild(breakConstraintElement);
        }
    }

    private static void setBR2(List<BR2Dto> br2, Element parent, Document document) {
        if (br2 == null) {
            return;
        }

        for (BR2Dto data : br2) {
            Element breakConstraintElement = document.createElement("BR2");

            //addAttribute("id", br1Dto.getId(), breakConstraintElement, document);
            addAttribute("intp", data.getIntp(), breakConstraintElement, document);
            addAttribute("homeMode", data.getHomeMode(), breakConstraintElement, document);
            addAttribute("penalty", data.getPenalty(), breakConstraintElement, document);
            addAttribute("slots", convertListToString(data.getSlots()), breakConstraintElement, document);
            addAttribute("teams", convertListToString(data.getTeams()), breakConstraintElement, document);
            addAttribute("type", data.getType(), breakConstraintElement, document);

            parent.appendChild(breakConstraintElement);
        }
    }

    private static void setCA1(List<CA1Dto> ca1, Element parent, Document document) {
        if (ca1 == null) {
            return;
        }

        for (CA1Dto data : ca1) {
            Element breakConstraintElement = document.createElement("CA1");

            //addAttribute("id", br1Dto.getId(), breakConstraintElement, document);
            addAttribute("max", data.getMax(), breakConstraintElement, document);
            addAttribute("min", data.getMin(), breakConstraintElement, document);
            addAttribute("mode", data.getMode(), breakConstraintElement, document);
            addAttribute("penalty", data.getPenalty(), breakConstraintElement, document);
            addAttribute("slots", convertListToString(data.getSlots()), breakConstraintElement, document);
            addAttribute("teams", data.getTeams(), breakConstraintElement, document);
            addAttribute("type", data.getType(), breakConstraintElement, document);

            parent.appendChild(breakConstraintElement);
        }
    }

    private static void setCA2(List<CA2Dto> ca2, Element parent, Document document) {
        if (ca2 == null) {
            return;
        }

        for (CA2Dto data : ca2) {
            Element breakConstraintElement = document.createElement("CA2");

            //addAttribute("id", br1Dto.getId(), breakConstraintElement, document);
            addAttribute("max", data.getMax(), breakConstraintElement, document);
            addAttribute("min", data.getMin(), breakConstraintElement, document);
            addAttribute("mode1", data.getMode1(), breakConstraintElement, document);
            addAttribute("penalty", data.getPenalty(), breakConstraintElement, document);
            addAttribute("slots", convertListToString(data.getSlots()), breakConstraintElement, document);
            addAttribute("teams1", data.getTeams1(), breakConstraintElement, document);
            addAttribute("teams2", convertListToString(data.getTeams2()), breakConstraintElement, document);
            addAttribute("type", data.getType(), breakConstraintElement, document);

            parent.appendChild(breakConstraintElement);
        }
    }

    private static void setCA3(List<CA3Dto> ca3, Element parent, Document document) {
        if (ca3 == null) {
            return;
        }

        for (CA3Dto data : ca3) {
            Element breakConstraintElement = document.createElement("CA3");

            //addAttribute("id", br1Dto.getId(), breakConstraintElement, document);
            addAttribute("intp", data.getIntp(), breakConstraintElement, document);
            addAttribute("max", data.getMax(), breakConstraintElement, document);
            addAttribute("min", data.getMin(), breakConstraintElement, document);
            addAttribute("mode1", data.getMode1(), breakConstraintElement, document);
            addAttribute("penalty", data.getPenalty(), breakConstraintElement, document);
            addAttribute("teams1", convertListToString(data.getTeams1()), breakConstraintElement, document);
            addAttribute("teams2", convertListToString(data.getTeams2()), breakConstraintElement, document);
            addAttribute("type", data.getType(), breakConstraintElement, document);

            parent.appendChild(breakConstraintElement);
        }
    }

    private static void setCA4(List<CA4Dto> ca4, Element parent, Document document) {
        if (ca4 == null) {
            return;
        }

        for (CA4Dto data : ca4) {
            Element breakConstraintElement = document.createElement("CA4");

            //addAttribute("id", br1Dto.getId(), breakConstraintElement, document);
            addAttribute("max", data.getMax(), breakConstraintElement, document);
            addAttribute("min", data.getMin(), breakConstraintElement, document);
            addAttribute("mode1", data.getMode1(), breakConstraintElement, document);
            addAttribute("mode2", data.getMode2(), breakConstraintElement, document);
            addAttribute("penalty", data.getPenalty(), breakConstraintElement, document);
            addAttribute("teams1", convertListToString(data.getTeams1()), breakConstraintElement, document);
            addAttribute("slots", convertListToString(data.getSlots()), breakConstraintElement, document);
            addAttribute("teams2", convertListToString(data.getTeams2()), breakConstraintElement, document);
            addAttribute("type", data.getType(), breakConstraintElement, document);

            parent.appendChild(breakConstraintElement);
        }
    }

    private static void setFA2(List<FA2Dto> fa2, Element parent, Document document) {
        if (fa2 == null) {
            return;
        }

        for (FA2Dto data : fa2) {
            Element breakConstraintElement = document.createElement("FA2");

            //addAttribute("id", br1Dto.getId(), breakConstraintElement, document);
            addAttribute("intp", data.getIntp(), breakConstraintElement, document);
            addAttribute("mode", data.getMode(), breakConstraintElement, document);
            addAttribute("penalty", data.getPenalty(), breakConstraintElement, document);
            addAttribute("slots", convertListToString(data.getSlots()), breakConstraintElement, document);
            addAttribute("teams", convertListToString(data.getTeams()), breakConstraintElement, document);
            addAttribute("type", data.getType(), breakConstraintElement, document);

            parent.appendChild(breakConstraintElement);
        }
    }

    private static void setGA1(List<GA1Dto> ga1, Element parent, Document document) {
        if (ga1 == null) {
            return;
        }

        for (GA1Dto data : ga1) {
            Element breakConstraintElement = document.createElement("GA1");

            //addAttribute("id", br1Dto.getId(), breakConstraintElement, document);
            addAttribute("max", data.getMax(), breakConstraintElement, document);
            addAttribute("min", data.getMin(), breakConstraintElement, document);
            addAttribute("meetings", convertMatrixToString(data.getMeetings()), breakConstraintElement, document);
            addAttribute("penalty", data.getPenalty(), breakConstraintElement, document);
            addAttribute("slots", convertListToString(data.getSlots()), breakConstraintElement, document);
            addAttribute("type", data.getType(), breakConstraintElement, document);

            parent.appendChild(breakConstraintElement);
        }
    }

    private static void setSE1(List<SE1Dto> se1, Element parent, Document document) {
        if (se1 == null) {
            return;
        }

        for (SE1Dto data : se1) {
            Element breakConstraintElement = document.createElement("SE1");

            //addAttribute("id", br1Dto.getId(), breakConstraintElement, document);
            addAttribute("min", data.getMin(), breakConstraintElement, document);
            addAttribute("penalty", data.getPenalty(), breakConstraintElement, document);
            addAttribute("teams", convertListToString(data.getTeams()), breakConstraintElement, document);
            addAttribute("type", data.getType(), breakConstraintElement, document);

            parent.appendChild(breakConstraintElement);
        }
    }

    private static void addAttribute(String attributeName, Object attributeValue, Element parent, Document document) {
        Attr attribute = document.createAttribute(attributeName);
        attribute.setValue(String.valueOf(attributeValue));
        parent.setAttributeNode(attribute);
    }

    private static String convertListToString(List<Integer> numbers) {
        return numbers.stream().map(String::valueOf)
                .collect(Collectors.joining(";"));
    }

    private static String convertMatrixToString(List<List<Integer>> matrix) {
        StringBuilder result = new StringBuilder();
        for (List<Integer> list : matrix) {
            StringBuilder tmp = new StringBuilder();
            for (Integer i : list) {
                tmp.append(",").append(i);
            }
            tmp.delete(0, 1);
            result.append(";").append(tmp.toString());
        }
        result.delete(0, 1);
        return result.toString();
    }
}
