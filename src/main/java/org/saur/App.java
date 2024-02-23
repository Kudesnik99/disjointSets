package org.saur;

import org.saur.data.Element;
import org.saur.disjointset.DisjointSets;
import org.saur.util.InputOutput;

import java.util.*;

public class App {
    public static void main(String[] args) throws Exception {
        String outputFileName = args.length > 1 ? args[1] : "result.txt";

        // Считываем исходные данные
        Set<String> inputLines = InputOutput.readLines(args);
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
                        result.unionAll(elementLocatedIn, groupNumber);
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
        InputOutput.writeResult(splitLines, sortedResult, multiStringGroups, outputFileName);

        System.out.println(">>> Окончание работы: " + new Date());
    }
}
