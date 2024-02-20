package org.saur;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println(">>> Начало работы " + new Date());

        URL website = new URL("https://github.com/PeacockTeam/new-job/releases/download/v1.0/lng-4.txt.gz");
        GZIPInputStream gzis = new GZIPInputStream(website.openStream());

        InputStreamReader isr = new InputStreamReader(gzis);
        BufferedReader reader = new BufferedReader(isr);

        String readedLine = reader.readLine();
        List<String[]> inputLines = new ArrayList<>(); // Прочитанные строки. Исходник для обработки

        while (readedLine != null) {
            inputLines.add(readedLine.split("[;\\n]"));
            readedLine = reader.readLine();
        }
        gzis.close();

        System.out.println(">>> Данные получены: " + new Date());

        List<String[]> uniqueLines = inputLines.stream().distinct().toList();

        // Готовим мапу для дальнейшей работы. Key - элемент, Value - множество номеров строк в исходном списке, в которых он присутствует
        Map<Element, Set<Integer>> elementsLocatedIn = new HashMap<>();
        for (int i = 0; i < uniqueLines.size(); i++) {
            for (int columnNumber = 0; columnNumber < uniqueLines.get(i).length; columnNumber++) {
                Element element = new Element(columnNumber, uniqueLines.get(i)[columnNumber]);
                elementsLocatedIn.computeIfAbsent(element, key -> new HashSet<>()).add(i); // Индекс вместо строки
            }
        }

        System.out.println(">>> Мапа подготовлена: " + new Date());

        // Заполняем результат так, как будто каждая строка находится в своей группе
        DisjointSets result = new DisjointSets();
        result.initDisjointSets(uniqueLines.size());

        for (Map.Entry<Element, Set<Integer>> elementLocatedIn : elementsLocatedIn.entrySet()) {
            nextElement:
            if (elementLocatedIn.getValue().size() > 1) {
                for (int lineNumber : elementLocatedIn.getValue()) {
                    for (int groupNumber = 0; groupNumber < result.getNodes().size(); groupNumber++) {
                        if (result.find(lineNumber).equals(groupNumber)) {
                            result.unionAll(groupNumber, elementLocatedIn.getValue());
                            break nextElement;
                        }
                    }
                }
            }
        }

        System.out.println(">>> Разбиение произведено: " + new Date());

        Map<Integer, Set<Integer>> groupedResult = new HashMap<>();

        for (int i = 0; i < result.getNodes().size(); i++) {
            groupedResult.computeIfAbsent(result.getNodes().get(i).getParentNode(), key -> new HashSet<>()).add(i);
        }

        List<Set<Integer>> sortedResult = groupedResult.values().stream().sorted((set1, set2) -> set2.size() - set1.size()).toList();

        System.out.println(groupedResult.size());
        System.out.println(sortedResult.size());

        String outputFileName = "result.txt";

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
        for (int i = 0; i < sortedResult.size(); i++) {
            writer.write("Группа " + (i + 1) + "\n");
            sortedResult.get(i).forEach(index -> {
                try {
                    writer.write(Arrays.toString(uniqueLines.get(index)) + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            writer.write("\n");
        }

        System.out.println(">>> Окончание работы: " + new Date());
    }
}
