package org.example.service;

import org.example.model.TestCase;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;

public class CodeExecutionService {

    public String executeCode(String userCode, String language, List<TestCase> testCases, BiConsumer<String, String> logger) {
        try {
            String fileName = language.equalsIgnoreCase("java") ? "Main.java" : "Solution.cpp";
            Files.writeString(Path.of(fileName), userCode);

            logger.accept("Compiling code...", "#CCCCCC");
            ProcessBuilder compileProcess;

            if (language.equalsIgnoreCase("java")) {
                compileProcess = new ProcessBuilder("javac", "Main.java");
            } else {
                compileProcess = new ProcessBuilder("g++", "Solution.cpp", "-o", "Solution.exe");
            }

            Process cp = compileProcess.start();
            cp.waitFor();

            if (cp.exitValue() != 0) {
                logger.accept("❌ Compilation Error!", "#FF3333");
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(cp.getErrorStream()))) {
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        logger.accept(errorLine, "#FF8888");
                    }
                }
                return "COMPILATION_ERROR";
            }

            logger.accept("Compilation Successful. Running Test Cases...\n", "#00FF00");

            int testCaseNumber = 1;
            for (TestCase tc : testCases) {
                logger.accept("▶ Running Test Case " + testCaseNumber + "...", "#4DA8DA");

                ProcessBuilder runProcess;
                if (language.equalsIgnoreCase("java")) {
                    runProcess = new ProcessBuilder("java", "Main");
                } else {
                    runProcess = new ProcessBuilder("Solution.exe");
                }

                Process rp = runProcess.start();

                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(rp.getOutputStream()))) {
                    writer.write(tc.getInputData());
                    writer.flush();
                }

                StringBuilder actualOutput = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(rp.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        actualOutput.append(line).append("\n");
                    }
                }

                rp.waitFor();

                String out = actualOutput.toString().trim();
                String exp = tc.getExpectedOutput().trim();

                logger.accept("Input Data:\n" + tc.getInputData().trim(), "#AAAAAA");
                logger.accept("Expected Output: " + exp, "#E8A317");
                logger.accept("Your Output:     " + out, "#E8A317");

                if (!out.equals(exp)) {
                    logger.accept("❌ Test Case " + testCaseNumber + " Failed!\n", "#FF3333");
                    return "WRONG_ANSWER";
                } else {
                    logger.accept("✅ Test Case " + testCaseNumber + " Passed!\n", "#00FF00");
                }
                testCaseNumber++;
            }

            return "PASSED";

        } catch (Exception e) {
            logger.accept("Execution Error: " + e.getMessage(), "#FF3333");
            return "RUNTIME_ERROR";
        }
    }
}