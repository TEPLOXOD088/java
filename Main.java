import java.io.*;
import java.util.*;

public class Main {
    // Split a CSV line. Some cheese names have commas inside quotes.
    public static String[] splitLine(String line) {
        ArrayList<String> parts = new ArrayList<>();
        String current = "";
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char letter = line.charAt(i);
            if (letter == '"') {
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current += '"';
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (letter == ',' && !insideQuotes) {
                parts.add(current.trim());
                current = "";
            } else {
                current += letter;
            }
        }
        parts.add(current.trim());
        return parts.toArray(new String[0]);
    }

    public static void main(String[] args) {
        int pasteurized = 0;
        int raw = 0;
        int organicCheese = 0;
        HashMap<String, Integer> milkTypes = new HashMap<>();

        try (BufferedReader file = new BufferedReader(new FileReader("cheese_data.csv"))) {
            String firstLine = file.readLine();
            if (firstLine == null) {
                System.out.println("The file is empty.");
                return;
            }

            // Find the columns we need.
            String[] columns = splitLine(firstLine);
            int treatmentCol = -1;
            int organicCol = -1;
            int moistureCol = -1;
            int milkCol = -1;

            for (int i = 0; i < columns.length; i++) {
                String name = columns[i].replace("\uFEFF", "");
                if (name.equals("MilkTreatmentTypeEn")) treatmentCol = i;
                if (name.equals("Organic")) organicCol = i;
                if (name.equals("MoisturePercent")) moistureCol = i;
                if (name.equals("MilkTypeEn")) milkCol = i;
            }

            if (treatmentCol == -1 || organicCol == -1 || moistureCol == -1 || milkCol == -1) {
                System.out.println("Some columns are missing.");
                return;
            }

            // Read one cheese at a time.
            String line;
            while ((line = file.readLine()) != null) {
                String[] cheese = splitLine(line);
                if (cheese.length != columns.length) continue;

                String treatment = cheese[treatmentCol];
                if (treatment.equalsIgnoreCase("Pasteurized")) pasteurized++;
                if (treatment.equalsIgnoreCase("Raw Milk") || treatment.equalsIgnoreCase("Raw")) raw++;

                if (cheese[organicCol].equals("1")) {
                    try {
                        double moisture = Double.parseDouble(cheese[moistureCol]);
                        if (moisture > 41) organicCheese++;
                    } catch (NumberFormatException e) {
                        // Skip cheeses with no moisture number.
                    }
                }

                String milk = cheese[milkCol];
                if (!milk.isEmpty()) {
                    milkTypes.put(milk, milkTypes.getOrDefault(milk, 0) + 1);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read cheese_data.csv");
            return;
        }

        // Find the milk type with the biggest count.
        String topMilk = "Unknown";
        int topCount = 0;
        for (String milk : milkTypes.keySet()) {
            int count = milkTypes.get(milk);
            if (count > topCount) {
                topMilk = milk;
                topCount = count;
            }
        }

        // Save the answers in a text file.
        try (PrintWriter output = new PrintWriter("output.txt")) {
            output.println("Pasteurized cheeses: " + pasteurized);
            output.println("Raw milk cheeses: " + raw);
            output.println("Organic cheeses with more than 41% moisture: " + organicCheese);
            output.println("Most common milk: " + topMilk + " (" + topCount + ")");
            System.out.println("Answers saved to output.txt");
        } catch (FileNotFoundException e) {
            System.out.println("Could not create output.txt");
        }
    }
}
