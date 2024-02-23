package org.saur.util;

import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class InputOutput {
    public static Set<String> readLines(String[] args) throws IOException {
        Pattern pattern = Pattern.compile("(\"\\d*\";)+(\"\\d*\")$");
        URL reference = new URL("https://github.com/PeacockTeam/new-job/releases/download/v1.0/lng-4.txt.gz");

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
        return inputLines;
    }

    public static void writeResult(List<String[]> splitLines, List<Set<Integer>> sortedResult, long multiStringGroups,
                                   String outputFileName) throws IOException {
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
    }
}
