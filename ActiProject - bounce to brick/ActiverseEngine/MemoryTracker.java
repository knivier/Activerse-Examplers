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

/**
 * MemoryTracker class is used to track memory usage of the game along with other statistics
 * @Author: Knivier
 * @Version: 1.2.2
 * 
 */
public class MemoryTracker {
    private long lastTime;
    private long lastMemoryUsed;
    private double memoryUsedPerSecond;
    private boolean loggingEnabled;
    private int targetFPS;
    private boolean newSessionNotified = false;
    private int logNum = 0;

    /**
     * Constructor for MemoryTracker class
     * @return none valid
     * @throws none
     */
    public MemoryTracker() {
        loadProperties();
        lastTime = System.nanoTime();
        lastMemoryUsed = getMemoryUsed();
    }

    
    /** 
     * Returns FPS target
     * @return int FPStarget
     */
    public int getTargetFPS() {
        return targetFPS;
    }

    /**
     * Loads properties from the Activerse.properties file.
     * @return none valid
     */
    private void loadProperties() {
        Properties props = new Properties();
        try {
            String propertiesFile = "Activerse.properties";
            props.load(getClass().getClassLoader().getResourceAsStream(propertiesFile));
            loggingEnabled = Boolean.parseBoolean(props.getProperty("logging", "false"));
            targetFPS = Integer.parseInt(props.getProperty("fps", "60"));
        } catch (IOException e) {
            System.out.println("An error occurred while loading properties from Activerse.properties");
            System.out.println("Default values will be used. Contact ActiverseEngine support for bugs. Otherwise, please provide a properties file.");
            e.printStackTrace();
        }
    }

    
    /** 
     * Returns the memory used
     * @return long
     */
    private long getMemoryUsed() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    
    /** 
     * Returns the memory heap usage
     * @return MemoryUsage obj
     * @throws none
     */
    private MemoryUsage getHeapMemoryUsage() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        return memoryMXBean.getHeapMemoryUsage();
    }

    
    /** 
     * Returns the non heap memory usage
     * @return MemoryUsage
     */
    private MemoryUsage getNonHeapMemoryUsage() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        return memoryMXBean.getNonHeapMemoryUsage();
    }

    /**
     * Returns the garbage collection time
     * @return long garbage collection time
     */
    private long getGarbageCollectionTime() {
        long totalGarbageCollectionTime = 0;
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            totalGarbageCollectionTime += gcBean.getCollectionTime();
        }
        return totalGarbageCollectionTime;
    }

    /**
     * Updates the memory usage statistics and writes them to a file if logging is enabled.
     * @return none valid
     * @throws none
     * @see #writeMemoryUsageToFile()
     */
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

    /**
     * Returns the memory usage per second in a formatted string.
     * @return String
     */
    public String getMemoryUsagePerSecond() {
        return String.format("MPS: %.2f MB/s", memoryUsedPerSecond);
    }

    /**
     * Writes the memory usage statistics in the logs.log file along with other statistics.
     * @return none valid
     * @throws IOException if an I/O error occurs (file writer)
     * @see #getHeapMemoryUsage()
     * @see #getNonHeapMemoryUsage()
     * @see #getGarbageCollectionTime()
     * @see #getMemoryUsagePerSecond()
     * @see #writeMemoryUsageToFile()
     * @see #logNum
     * @see #newSessionNotified
     * @see #targetFPS
     * @see #World.getFPS()
     * @see #System.currentTimeMillis()
     */
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
            System.out.println("An error occurred while writing memory usage statistics to logs.log");
            System.out.println("Please check if the file is writeable and has enough space. Contact ActiverseEngine support for bugs.");
            e.printStackTrace();
        }
    }
}