package org.saur;

import org.saur.disjointset.DisjointSets;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class App {
    public static void main(String[] args) throws Exception {

        Pattern pattern = Pattern.compile("(\"\\d*\";)+(\"\\d*\")$");
        URL reference = new URL("https://github.com/PeacockTeam/new-job/releases/download/v1.0/lng-4.txt.gz");
        String outputFileName = args.length > 1 ? args[1] : "result.txt";

        System.out.println(">>> Начало работы " + new Date());

        InputStreamReader isr;
        InputStream inputFile;

        if (args.length > 0) {
            String inputFileName = args[0];
            inputFile = new FileInputStream(inputFileName);
        } else {
            inputFile = new GZIPInputStream(reference.openStream());
        }

        isr = new InputStreamReader(inputFile);
        BufferedReader reader = new BufferedReader(isr);
        String readLine = reader.readLine();
        // Если в группе две одинаковых строки - нужно оставить одну. Поэтому Set. Сразу.
        Set<String> inputLines = new HashSet<>(); // Прочитанные строки. Исходник для обработки

        while (readLine != null) {
            // Строки вида
            // "8383"200000741652251""79855053897"83100000580443402";"200000133000191"
            // являются некорректными и должны пропускаться
            if (pattern.matcher(readLine).matches()) {
                inputLines.add(readLine);
            }
            readLine = reader.readLine();
        }
        reader.close();
        inputFile.close();

        // Разбиваем строки на элементы
        List<String[]> splitLines = inputLines.stream().map(it -> it.split(";")).toList();

        System.out.println(">>> Данные получены: " + new Date());
        System.out.println("--- Количество строк: " + inputLines.size());

        // Готовим мапу для дальнейшей работы. Key - элемент строки,
        // Value - множество номеров строк в исходном списке, в которых он присутствует
        Map<Element, TreeSet<Integer>> elementsLocatedIn = new HashMap<>();
        for (int i = 0; i < splitLines.size(); i++) {
            for (int columnNumber = 0; columnNumber < splitLines.get(i).length; columnNumber++) {
                if (splitLines.get(i)[columnNumber].length() < 3) continue;
                Element element = new Element(columnNumber, splitLines.get(i)[columnNumber]);
                elementsLocatedIn.computeIfAbsent(element, key -> new TreeSet<>()).add(i); // Храним не саму строку, а индекс
            }
        }

        System.out.println(">>> Мапа подготовлена: " + new Date());

        // Изначально заполняем результат так, как будто каждая строка находится в своей группе
        // (т.е. просто инициализируем DisjointSets)
        DisjointSets result = new DisjointSets();
        result.initDisjointSets(splitLines.size());

        for (Set<Integer> elementLocatedIn : elementsLocatedIn.values()) {
            nextElement:
            // Если элемент присутствует более чем в одной строке -> объединяем эти строки.
            if (elementLocatedIn.size() > 1) {
                for (int lineNumber : elementLocatedIn) {
                    // Для этого пробегаемся по всем группам, чтобы найти ту, в которой находится элемент
                    for (int groupNumber = 0; groupNumber < result.getNodes().size(); groupNumber++) {
                        if (result.find(lineNumber).equals(groupNumber)) {
                            result.unionAll(groupNumber, elementLocatedIn);
                            break nextElement;
                        }
                    }
                }
            }
        }

        System.out.println(">>> Разбиение произведено: " + new Date());

        // Раскладываем результат в мапу по группам
        Map<Integer, Set<Integer>> groupedResult = new HashMap<>();
        for (int i = 0; i < result.getNodes().size(); i++) {
            groupedResult.computeIfAbsent(result.getNodes().get(i).getParentNode(), key -> new HashSet<>()).add(i);
        }

        // Сортируем по количеству строк в группе для вывода
        List<Set<Integer>> sortedResult = groupedResult.values().stream().sorted((set1, set2) -> set2.size() - set1.size()).toList();
        long multiStringGroups = groupedResult.values().stream().filter(it -> it.size() > 1).count();

        System.out.println(">>> Формируем результирующий файл:" + new Date());

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
        writer.write("Групп с более чем одним элементом: " + multiStringGroups + "\n\n");

        for (int i = 0; i < sortedResult.size(); i++) {
            writer.write("Группа " + (i + 1) + "\n");
            sortedResult.get(i).forEach(index -> {
                try {
                    writer.write(String.join(";", splitLines.get(index)) + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            writer.write("\n");
        }

        writer.flush();
        writer.close();

        System.out.println(">>> Окончание работы: " + new Date());
    }
}
