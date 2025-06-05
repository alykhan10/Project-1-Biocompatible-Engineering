import java.io.*;
import java.util.*;

public class HeartAttackRiskAnalyzer {

    // Class to store each blood flow data point
    static class BloodFlowReading {
        String artery;
        double timestamp;
        double flowRate;

        BloodFlowReading(String artery, double timestamp, double flowRate) {
            this.artery = artery;
            this.timestamp = timestamp;
            this.flowRate = flowRate;
        }
    }

    public static void main(String[] args) throws IOException {
        // Load blood flow data from CSV file
        List<BloodFlowReading> data = loadCSV("bloodflow_data.csv");

        // Perform artery-to-artery comparison at each timestamp
        detectArterialDiscrepancies(data);

        // Check against personal baseline values
        double baselineFlow = 75.0;  // Example average flow rate (mL/s)
        double threshold = 0.2;      // 20% deviation threshold
        detectDeviationFromBaseline(data, baselineFlow, threshold);
    }

    // Load data from CSV file in the format: artery,timestamp,flowRate
    private static List<BloodFlowReading> loadCSV(String filePath) throws IOException {
        List<BloodFlowReading> readings = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;

        reader.readLine(); // Skip header row

        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(",");
            readings.add(new BloodFlowReading(
                tokens[0], 
                Double.parseDouble(tokens[1]), 
                Double.parseDouble(tokens[2])
            ));
        }

        return readings;
    }

    // Detect major differences in flow between arteries at the same time
    private static void detectArterialDiscrepancies(List<BloodFlowReading> data) {
        // Map timestamps to (artery -> flow rate) mappings
        Map<Double, Map<String, Double>> timeMap = new HashMap<>();

        // Group readings by timestamp
        for (BloodFlowReading reading : data) {
            timeMap
                .computeIfAbsent(reading.timestamp, k -> new HashMap<>())
                .put(reading.artery, reading.flowRate);
        }

        // Check for significant discrepancies in artery flows
        for (var entry : timeMap.entrySet()) {
            double time = entry.getKey();
            Collection<Double> flows = entry.getValue().values();
            double max = Collections.max(flows);
            double min = Collections.min(flows);

            // Alert if the difference exceeds 10 mL/s
            if (max - min > 10.0) {
                System.out.println("Significant artery flow discrepancy at " + time + "s");
            }
        }
    }

    // Compare each flow reading to the baseline to detect anomalies
    private static void detectDeviationFromBaseline(List<BloodFlowReading> data, double baseline, double threshold) {
        for (BloodFlowReading reading : data) {
            double deviation = Math.abs(reading.flowRate - baseline) / baseline;

            // Alert if deviation exceeds the acceptable threshold
            if (deviation > threshold) {
                System.out.printf("Abnormal flow in %s at %.2fs: %.2f mL/s\n",
                        reading.artery, reading.timestamp, reading.flowRate);
            }
        }
    }
}