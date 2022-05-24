package logic.parser;

import logic.constraints.breakConstraints.BR1;
import logic.constraints.breakConstraints.BR2;
import logic.constraints.capacityConstraints.CA1;
import logic.constraints.capacityConstraints.CA2;
import logic.constraints.capacityConstraints.CA3;
import logic.constraints.capacityConstraints.CA4;
import logic.constraints.fairnessConstraints.FA2;
import logic.constraints.gameConstraints.GA1;
import logic.constraints.separationConstraints.SE1;
import logic.entities.ConstraintType;
import logic.entities.Slot;
import logic.entities.Team;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import logic.scheduler.MatchScheduler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class MyXMLParser {
    private final File file;
    private final String fileName;
    private final MatchScheduler scheduler;

    public MyXMLParser(File file) {
        scheduler = new MatchScheduler();
        this.file = file;
        this.fileName = file.getName();
    }

    public void parse() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(this.file);
        doc.getDocumentElement().normalize();

        isPhased(doc);
        parseXML(doc);
    }

    private void isPhased(Document doc) {
        NodeList structure = doc.getElementsByTagName("Structure");
        Node fNode = structure.item(0);
        Element eElement = (Element) fNode;
        String gameMode = eElement.getElementsByTagName("gameMode").item(0).getTextContent();
        this.scheduler.setPhased(gameMode.equals("P"));
    }

    private void parseXML(Document doc) {
        try {
            this.scheduler.setTeams(parseTeams(doc.getElementsByTagName("Teams")));
            this.scheduler.setSlots(parseSlots(doc.getElementsByTagName("Slots")));

            this.scheduler.setCa1List(parseCA1(doc.getElementsByTagName("CapacityConstraints")));
            this.scheduler.setCa2List(parseCA2(doc.getElementsByTagName("CapacityConstraints")));
            this.scheduler.setCa3List(parseCA3(doc.getElementsByTagName("CapacityConstraints")));
            this.scheduler.setCa4List(parseCA4(doc.getElementsByTagName("CapacityConstraints")));

            this.scheduler.setBr1List(parseBR1(doc.getElementsByTagName("BreakConstraints")));
            this.scheduler.setBr2List(parseBR2(doc.getElementsByTagName("BreakConstraints")));

            this.scheduler.setFa2List(parseFA2(doc.getElementsByTagName("FairnessConstraints")));

            this.scheduler.setGa1List(parseGA1(doc.getElementsByTagName("GameConstraints")));

            this.scheduler.setSe1List(parseSE1(doc.getElementsByTagName("SeparationConstraints")));
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static List<Team> parseTeams(NodeList teamsTag) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return (List<Team>)(List<?>) parseXMLTag(Team.class, "team", teamsTag);
    }

    private static List<Slot> parseSlots(NodeList slotsTag) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return (List<Slot>)(List<?>) parseXMLTag(Slot.class, "slot", slotsTag);
    }

    private static List<CA1> parseCA1(NodeList ca1sTag) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return (List<CA1>)(List<?>) parseXMLTag(CA1.class, "CA1", ca1sTag);
    }

    private static List<CA2> parseCA2(NodeList mainTag) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return (List<CA2>)(List<?>) parseXMLTag(CA2.class, "CA2", mainTag);
    }

    private static List<CA3> parseCA3(NodeList mainTag) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return (List<CA3>)(List<?>) parseXMLTag(CA3.class, "CA3", mainTag);
    }

    private static List<CA4> parseCA4(NodeList mainTag) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return (List<CA4>)(List<?>) parseXMLTag(CA4.class, "CA4", mainTag);
    }

    private static List<BR1> parseBR1(NodeList mainTag) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return (List<BR1>)(List<?>) parseXMLTag(BR1.class, "BR1", mainTag);
    }

    private static List<BR2> parseBR2(NodeList mainTag) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return (List<BR2>)(List<?>) parseXMLTag(BR2.class, "BR2", mainTag);
    }

    private static List<FA2> parseFA2(NodeList mainTag) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return (List<FA2>)(List<?>) parseXMLTag(FA2.class, "FA2", mainTag);
    }

    private static List<GA1> parseGA1(NodeList mainTag) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return (List<GA1>)(List<?>) parseXMLTag(GA1.class, "GA1", mainTag);
    }

    private static List<SE1> parseSE1(NodeList mainTag) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return (List<SE1>)(List<?>) parseXMLTag(SE1.class, "SE1", mainTag);
    }

    private static List<Object> parseXMLTag(Class<?> clazz, String tagName, NodeList mainTag) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < mainTag.getLength(); i++) {
            Node node = mainTag.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                NodeList tag = element.getElementsByTagName(tagName);

                for (int j = 0; j < tag.getLength(); j++) {
                    Node teamNode = tag.item(j);

                    if (teamNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element teamElement = (Element) teamNode;
                        Object newInstance = clazz.getDeclaredConstructor(List.class).newInstance(processObjectFields(clazz, teamElement));
                        result.add(newInstance);
                    }
                }
            }
        }
        return result;
    }

    private static List<Object> processObjectFields(Class<?> clazz, Element teamElement) {
        List<Object> list = new ArrayList<>();
        for(Field field : clazz.getDeclaredFields()) {
            switch (getType(field.getGenericType().toString())) {
                case "int":
                    int varInt = Integer.parseInt(teamElement.getAttribute(field.getName()));
                    list.add(varInt);
                    break;
                case "String":
                    String varString = teamElement.getAttribute(field.getName());
                    list.add(varString);
                    break;
                case "ConstraintType":
                    ConstraintType type = ConstraintType.valueOf(teamElement.getAttribute(field.getName()));
                    list.add(type);
                    break;
                case "List<List<Integer>>":
                    List<List<Integer>> listInList = splitStringToListIntegerInList(teamElement.getAttribute(field.getName()), ";", ",");
                    list.add(listInList);
                    break;
                default:
                    List<Integer> varList = splitStringToIntegerList(teamElement.getAttribute(field.getName()), ";");
                    list.add(varList);
                    break;
            }
        }
        return list;
    }

    public static String getType(String genericType) {
        if (!genericType.contains(("."))) {
            return genericType;
        }
        if (!genericType.contains("<")) {
            int lastIndex = genericType.lastIndexOf(".");
            return genericType.substring(lastIndex+1);
        }
        int count = genericType.length() - genericType.replaceAll("<","").length();
        StringBuilder result = new StringBuilder();
        if (count == 1) {
            int li1 = genericType.split("<")[0].lastIndexOf(".");
            result.append(genericType.split("<")[0].substring(li1+1));
            result.append("<");
            int li2 = genericType.split("<")[1].lastIndexOf(".");
            result.append(genericType.split("<")[1].substring(li2+1));
            return result.toString();
        }
        String[] list = genericType.split("<");
        int li1 = list[0].lastIndexOf(".");
        result.append(list[0].substring(li1+1));
        result.append("<");
        int li2 = list[1].lastIndexOf(".");
        result.append(list[1].substring(li2+1));
        result.append("<");
        int li3 = list[2].lastIndexOf(".");
        result.append(list[2].substring(li3+1));
        return result.toString();
    }

    private static List<Integer> splitStringToIntegerList(String text, String separator) {
        List<Integer> result = new ArrayList<>();
        for (String s : text.split(separator)) {
            result.add(Integer.parseInt(s));
        }
        return result;
    }

    private static List<List<Integer>> splitStringToListIntegerInList(String text, String separator1, String separator2) {
        List<List<Integer>> result = new ArrayList<>();
        for (String s1 : text.split(separator1)) {
            List<Integer> newList = new ArrayList<>();
            for (String s2 : s1.split(separator2)) {
                newList.add(Integer.parseInt(s2));
            }
            result.add(newList);
        }
        return result;
    }

    public String getFileName() {
        return fileName;
    }

    public MatchScheduler getScheduler() {
        return scheduler;
    }
}
