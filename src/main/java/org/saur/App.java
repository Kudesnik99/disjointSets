package org.saur;

import org.saur.data.Element;
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
                if (splitLines.get(lineNumber).length <= columnNumber || splitLines.get(lineNumber)[columnNumber].length() < 3)
                    continue;
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
        System.out.println(">>> Размер: " + elementsFromColumns.size());

        List<Set<Integer>> groups = new ArrayList<>();     // Индекс - номер группы, значение - номера строк в ней
        int[] linesInGroups = new int[inputLines.size()];  // Индекс - номер строки, значение - номер группы
        Arrays.fill(linesInGroups, -1);                // Не распределённым строкам присваиваем номер группы -1

        // Раскладываем по группам связанные строки
        for (Set<Integer> elementLocatedIn : elementsFromColumns.values()) {
            int groupNumber = elementLocatedIn.stream()
                    .filter(lineNumber -> (linesInGroups[lineNumber] >= 0))
                    .map(lineNumber -> linesInGroups[lineNumber])
                    .findFirst().orElse(-1);
            if (groupNumber < 0) { // Ни одна из строк, в которой присутствует элемент не состоит ни в одной группе => создаём новую.
                groups.add(new HashSet<>(elementLocatedIn));
                elementLocatedIn.forEach(lineNumber -> linesInGroups[lineNumber] = groups.size() - 1);
            } else { // Хотя бы одна строка, в которой присутствует элемент, уже распределена (состоит в группе).
                List<Integer> groupNumbers = elementLocatedIn.stream()
                        .filter(lineNumber -> linesInGroups[lineNumber] >= 0) // Распределённые строки
                        .map(lineNumber -> linesInGroups[lineNumber])
                        .distinct().toList();
                if (groupNumbers.size() > 1) {
                    mergeGroups(groupNumber, groupNumbers.subList(1, groupNumbers.size()), linesInGroups, groups);
                }

                elementLocatedIn.stream()
                        .filter(lineNumber -> (linesInGroups[lineNumber] < 0)) // Не распределённые строки
                        .forEach(lineNumber -> {
                            groups.get(groupNumber).add(lineNumber);
                            linesInGroups[lineNumber] = groupNumber;
                        });
            }
        }

        // Создаём группы для индивидуальных строк. По одной на строку.
        for (int lineNumber = 0; lineNumber < linesInGroups.length; lineNumber++) {
            if (linesInGroups[lineNumber] < 0) {
                Set<Integer> newGroup = new HashSet<>();
                newGroup.add(lineNumber);
                groups.add(newGroup);
            }
        }
        System.out.println(">>> Разбиение по группам произведено: " + new Date());

        // Сортируем по количеству строк в группе для вывода
        List<Set<Integer>> sortedResult = groups.stream().filter(Objects::nonNull).sorted((set1, set2) -> set2.size() - set1.size()).toList();
        long multiStringGroups = sortedResult.stream().filter(group -> group.size() > 1).count();

        System.out.println(">>> Формируем результирующий файл:" + new Date());
        InputOutput.writeResult(splitLines, sortedResult, multiStringGroups, outputFileName);
        System.out.println(">>> Окончание работы: " + new Date());
    }

    private static void mergeGroups(int groupNumber, List<Integer> groupNumbers, int[] linesInGroups, List<Set<Integer>> groups) {
        for (Integer currentGroupNumber : groupNumbers) {
            if (groupNumber == currentGroupNumber) continue;
            groups.get(currentGroupNumber).forEach(it -> linesInGroups[it] = groupNumber);
            groups.get(groupNumber).addAll(groups.get(currentGroupNumber));
            groups.set(currentGroupNumber, null);
        }
    }
}
