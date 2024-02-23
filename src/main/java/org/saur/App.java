package org.saur;

import org.saur.disjointset.DisjointSets;
import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
            System.out.println("Usage: java [-Xmx1G] -jar dsu-1.0.jar [input_file [output_file]]");
            System.out.println("Default input file is: https://github.com/PeacockTeam/new-job/releases/download/v1.0/lng-4.txt.gz");
            System.out.println("Default input file is: result.txt");
            inputFile = new GZIPInputStream(reference.openStream());
        }

        isr = new InputStreamReader(inputFile);

        // Если в группе две одинаковых строки - нужно оставить одну. Поэтому Set. Сразу.
        Set<String> inputLines; // Прочитанные строки. Исходник для обработки

        try (BufferedReader reader = new BufferedReader(isr)) {
            // Строки вида "8383"200000741652251""79855053897"83100000580443402";"200000133000191"
            // являются некорректными и должны пропускаться. Поэтому matcher.
            inputLines = reader.lines().filter(it -> pattern.matcher(it).matches()).collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        inputFile.close();

        // Разбиваем строки на элементы
        List<String[]> splitLines = inputLines.stream().map(line -> line.split(";")).toList();
        // Находим максимальное кол-во элементов в строке
        int maxColumnsCount = splitLines.stream().map(it -> it.length).max(Comparator.comparingInt(Integer::intValue)).orElse(0);

        System.out.println(">>> Данные получены: " + new Date());
        System.out.println(">>> Количество неповторяющихся строк: " + inputLines.size());

        // Готовим мапу для дальнейшей работы:
        // Key - элемент строки, состоящий из номера столбца и значения,
        // Value - множество номеров строк в исходном (только разбитом на элементы) списке, в которых присутствует элемент
        Map<Element, TreeSet<Integer>> elementsFromColumns = new HashMap<>();
        for (int columnNumber = 0; columnNumber < maxColumnsCount; columnNumber++) {
            for (int lineNumber = 0; lineNumber < splitLines.size(); lineNumber++) {
                // Прекращаем проход по элементам, если они закончились (не самая длинная строка) и отфильтровываем пустые элементы
                if (splitLines.get(lineNumber).length <= columnNumber || splitLines.get(lineNumber)[columnNumber].length() < 3) continue;
                Element element = new Element(columnNumber, splitLines.get(lineNumber)[columnNumber]);
                elementsFromColumns.computeIfAbsent(element, key -> new TreeSet<>()).add(lineNumber);
            }

            // Пробегаемся ещё раз по всем строкам, чтобы удалить множества, состоящие из одной строки. Они нам не интересны,
            // т.к. наличие только одной строки означает, что каждый элемент присутствует ТОЛЬКО в этой строке и объединять её
            // ни с кем не потребуется.
            for (String[] splitLine : splitLines) {
                // Прекращаем проход по элементам, если они закончились и отфильтровываем пустые элементы
                if (splitLine.length <= columnNumber || splitLine[columnNumber].length() < 3) continue;
                Element element = new Element(columnNumber, splitLine[columnNumber]);
                if (elementsFromColumns.get(element).size() < 2) elementsFromColumns.remove(element);
            }
        }

        System.out.println(">>> Мапа подготовлена: " + new Date());

        // Изначально заполняем результат так, как будто каждая строка находится в своей группе
        // (т.е. просто инициализируем DisjointSets)
        DisjointSets result = new DisjointSets();
        result.initDisjointSets(splitLines.size());

        for (Set<Integer> elementLocatedIn : elementsFromColumns.values()) {
            nextElement:
            // Объединяем группы строк
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

        System.out.println(">>> Разбиение произведено: " + new Date());

        // Раскладываем результат в мапу по группам
        Map<Integer, Set<Integer>> groupedResult = new HashMap<>();
        for (int i = 0; i < result.getNodes().size(); i++) {
            groupedResult.computeIfAbsent(result.getNodes().get(i).getParentNode(), key -> new HashSet<>()).add(i);
        }

        // Сортируем по количеству строк в группе для вывода
        List<Set<Integer>> sortedResult = groupedResult.values().stream().sorted((set1, set2) -> set2.size() - set1.size()).toList();
        long multiStringGroups = groupedResult.values().stream().filter(group -> group.size() > 1).count();

        System.out.println(">>> Формируем результирующий файл:" + new Date());

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
        writer.write("Групп с более чем одним элементом: " + multiStringGroups + "\n\n");
        System.out.println("--- Групп с более чем одним элементом: " + multiStringGroups);

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
