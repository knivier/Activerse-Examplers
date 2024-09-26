package ActiverseEngine;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

public class MemoryTracker {
    private long lastTime;
    private long lastMemoryUsed;
    private double memoryUsedPerSecond;
    private boolean loggingEnabled;
    private int targetFPS;
    private boolean newSessionNotified = false;
    private int logNum = 0;

    public MemoryTracker() {
        loadProperties();
        lastTime = System.nanoTime();
        lastMemoryUsed = getMemoryUsed();
    }

    public int getTargetFPS() {
        return targetFPS;
    }

    private void loadProperties() {
        Properties props = new Properties();
        try {
            String propertiesFile = "Activerse.properties";
            props.load(getClass().getClassLoader().getResourceAsStream(propertiesFile));
            loggingEnabled = Boolean.parseBoolean(props.getProperty("logging", "false"));
            targetFPS = Integer.parseInt(props.getProperty("fps", "60"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long getMemoryUsed() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private MemoryUsage getHeapMemoryUsage() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        return memoryMXBean.getHeapMemoryUsage();
    }

    private MemoryUsage getNonHeapMemoryUsage() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        return memoryMXBean.getNonHeapMemoryUsage();
    }

    private long getGarbageCollectionTime() {
        long totalGarbageCollectionTime = 0;
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            totalGarbageCollectionTime += gcBean.getCollectionTime();
        }
        return totalGarbageCollectionTime;
    }

    public void update() {
        long now = System.nanoTime();
        if (now - lastTime >= 1_000_000_000) { // One second has passed
            long currentMemoryUsed = getMemoryUsed();
            memoryUsedPerSecond = (currentMemoryUsed - lastMemoryUsed) / (1024.0 * 1024.0); // Convert to MB
            lastMemoryUsed = currentMemoryUsed;
            lastTime = now;

            if (loggingEnabled) {
                writeMemoryUsageToFile();
            }
        }
    }

    public String getMemoryUsagePerSecond() {
        return String.format("MPS: %.2f MB/s", memoryUsedPerSecond);
    }

    private void writeMemoryUsageToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("logs.log", true))) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = now.format(formatter);
            if (!newSessionNotified) {
                writer.println("New Log Session @ " + formattedDateTime);
                newSessionNotified = true;
            }
            MemoryUsage heapMemoryUsage = getHeapMemoryUsage();
            MemoryUsage nonHeapMemoryUsage = getNonHeapMemoryUsage();
            long gcTime = getGarbageCollectionTime();
            writer.println(logNum + " | " + formattedDateTime + " | " + getMemoryUsagePerSecond() + " | FPS: " + World.getFPS() + " targeting " + targetFPS + " | Current Sys Time " + System.currentTimeMillis());
            logNum++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}