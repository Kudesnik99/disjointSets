package org.saur;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
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

        System.out.println(">>> Данные получены " + new Date());

//        List<String[]> uniqueLines = inputLines.stream().distinct().toList();

//        List<String[]> inputLines = new ArrayList<>();
//        inputLines.add("1; 2; 5; 8; 3 \n".split("[;\\n]"));
//        inputLines.add("3; 2; 6; 1; 7 \n".split("[;\\n]"));
//        inputLines.add("4; 8; 5; 2; 0; 5 \n".split("[;\\n]"));
//        inputLines.add("9; 1; 3; 6; 4 \n".split("[;\\n]"));
//        inputLines.add("1; 2; 6; 8; 7; 4; 1 \n".split("[;\\n]"));

        // Вывод исходных данных.
        // inputLines.forEach(it -> System.out.print(Arrays.stream(it).reduce((subtotal, element) -> subtotal + ";" + element).orElse("Ошибка") + "\n"));


        // Готовим мапу для дальнейшей работы. Key - элемент, Value - множество номеров строк в исходном списке, в которых он присутствует
        Map<Element, Set<Integer>> elementsLocatedIn = new HashMap<>();
        for (int i = 0; i < inputLines.size(); i++) {
            for (int columnNumber = 0; columnNumber < inputLines.get(i).length; columnNumber++) {
                Element element = new Element(columnNumber, inputLines.get(i)[columnNumber]);
                elementsLocatedIn.computeIfAbsent(element, key -> new HashSet<>()).add(i); // Индекс вместо строки
            }
        }

        System.out.println(">>> Мапа подготовлена " + new Date());

//         Вывод мапы
//         for (Map.Entry<Element, Set<Integer>> elementLocatedIn : elementsLocatedIn.entrySet()) {
//             System.out.println(elementLocatedIn.getKey());
//             System.out.println(elementLocatedIn.getValue().stream().map(Object::toString)
//                     .reduce((subtotal, element) -> subtotal + ";" + element).orElse("Ошибка") + "\n");
//         }

        // Готовим шаблон для результата
        DisjointSets result = new DisjointSets();
        result.initDisjointSets(inputLines.size());

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

        System.out.println(">>> Разбиение произведено " + new Date());

        Map<Integer, Set<Integer>> groupedResult = new HashMap<>();

        for (int i = 0; i < result.getNodes().size(); i++) {
            groupedResult.computeIfAbsent(result.getNodes().get(i).getParentNode(), key -> new HashSet<>()).add(i);
        }

        List<Set<Integer>> sortedResult = groupedResult.values().stream().sorted((set1, set2) -> set2.size() - set1.size()).toList();

        System.out.println(groupedResult.size());
        System.out.println(sortedResult.size());

        String outputFileName = "result.txt";

        BufferedWriter writter = new BufferedWriter(new FileWriter(outputFileName));
        for (int i = 0; i < sortedResult.size(); i++) {
            writter.write("Группа " + (i + 1) + "\n");
            sortedResult.get(i).forEach(index -> {
                try {
                    writter.write(Arrays.toString(inputLines.get(index)) + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            writter.write("\n");
        }

        // Вывод результатов
//        int groupNumber = 1;
//        boolean groupIsEmpty = false;
//        for (int i = 0; i < inputLines.size(); i++) {
//            if (!groupIsEmpty) {
//                System.out.println("\n" + "Группа № " + groupNumber);
//                groupIsEmpty = true;
//                groupNumber++;
//            }
//            for (int j = 0; j < result.getNodes().size(); j++) {
//                if (result.getNodes().get(j).getParentNode() == i) {
//                    System.out.println(Arrays.toString(inputLines.get(j)));
//                    groupIsEmpty = false;
//                }
//            }
//        }
        System.out.println(">>> Окончание работы " + new Date());
    }
}
