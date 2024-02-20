package org.saur;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class App {
    public static void main(String[] args) throws Exception {
        Pattern pattern = Pattern.compile("(\"\\d*\";)+(\"\\d*\")$");

        System.out.println(">>> Начало работы " + new Date());

        URL website = new URL("https://github.com/PeacockTeam/new-job/releases/download/v1.0/lng-4.txt.gz");
        GZIPInputStream gzis = new GZIPInputStream(website.openStream());

        InputStreamReader isr = new InputStreamReader(gzis);
        BufferedReader reader = new BufferedReader(isr);

        String readedLine = reader.readLine();
        Set<String> inputLines = new HashSet<>(); // Прочитанные строки. Исходник для обработки

        while (readedLine != null) {
            if (pattern.matcher(readedLine).matches())
                inputLines.add(readedLine);
            readedLine = reader.readLine();
        }
        gzis.close();

        List<String[]> splitLines = inputLines.stream().map(it -> it.split("[;\\n]")).toList();

//        List<String> inputStrings = new ArrayList<>();
//        inputStrings.add("1; 2; 5; 8; 3 \n");
//        inputStrings.add("3; 2; 6; 1; 7 \n");
//        inputStrings.add("4; 8; 5; 2; 0; 5 \n");
//        inputStrings.add("9; 1; 3; 6; 4 \n");
//        inputStrings.add("9; 1; 3; 6; 4 \n");
//        inputStrings.add("1; 2; 6; 8; 7; 4; 1 \n");
//
//        List<String[]> inputLines = new ArrayList<>();
//        inputLines.add("1; 2; 5; 8; 3 \n".split("[;\\n]"));
//        inputLines.add("3; 2; 6; 1; 7 \n".split("[;\\n]"));
//        inputLines.add("4; 8; 5; 2; 0; 5 \n".split("[;\\n]"));
//        inputLines.add("9; 1; 3; 6; 4 \n".split("[;\\n]"));
//        inputLines.add("9; 1; 3; 6; 4 \n".split("[;\\n]"));
//        inputLines.add("1; 2; 6; 8; 7; 4; 1 \n".split("[;\\n]"));
//
//        Set<String> uniqueSet = new HashSet<>(inputStrings);
//        System.out.println("---------> " + uniqueSet.size());

        System.out.println(">>> Данные получены: " + new Date());
        System.out.println("--- Количество строк изначально: " + inputLines.size());

//        List<String[]> uniqueLines = inputLines.stream().distinct().toList();
        System.out.println("--- Количество строк после удаления повторов: " + splitLines.size());

        // Готовим мапу для дальнейшей работы. Key - элемент, Value - множество номеров строк в исходном списке, в которых он присутствует
        Map<Element, Set<Integer>> elementsLocatedIn = new HashMap<>();
        for (int i = 0; i < splitLines.size(); i++) {
            for (int columnNumber = 0; columnNumber < splitLines.get(i).length; columnNumber++) {
                Element element = new Element(columnNumber, splitLines.get(i)[columnNumber]);
                elementsLocatedIn.computeIfAbsent(element, key -> new HashSet<>()).add(i); // Индекс вместо строки
            }
        }

        System.out.println(">>> Мапа подготовлена: " + new Date());

        // Заполняем результат так, как будто каждая строка находится в своей группе
        DisjointSets result = new DisjointSets();
        result.initDisjointSets(splitLines.size());

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
        long multiStringGroups = groupedResult.values().stream().filter(it -> it.size() > 1).count();

        System.out.println(">>> Формируем результирующий файл:" + new Date());

        String outputFileName = "result.txt";
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));

        writer.write("Групп с более чем одним элементом: " + multiStringGroups + "\n\n");

        for (int i = 0; i < sortedResult.size(); i++) {
            writer.write("Группа " + (i + 1) + "\n");
            sortedResult.get(i).forEach(index -> {
                try {
                    writer.write(Arrays.toString(splitLines.get(index)) + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            writer.write("\n");
        }

        System.out.println(">>> Окончание работы: " + new Date());
    }
}
