package com.hotmail.intrinsic;

import org.bukkit.Bukkit;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class IntrinsicLogger {

    private String fileName = "log.txt";
    private Intrinsic plugin;

    public IntrinsicLogger(Intrinsic plugin) {
        this.plugin = plugin;
        File loggerFile = new File(plugin.getDataFolder() + File.separator + this.fileName);

        try {
            loggerFile.createNewFile(); // if file already exists will do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(Level level, String message) {
        Bukkit.getLogger().log(level, "[Intrinsic] " + message);
    }

    /**
     * Log a message to the log file
     * @param level
     * @param message
     */
    public void logFile(Level level, String message) {
        File fout = new File(plugin.getDataFolder() + File.separator + this.fileName);
        try {
            FileWriter fileWriter = new FileWriter(fout, true);

            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("[ " + getCurrentTimeUsingDate() + " ] " + message);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentTimeUsingDate() {

        Date date = new Date();
        String strDateFormat = "dd:MM:yy hh:mm:ss a";

        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

        String formattedDate= dateFormat.format(date);

        return formattedDate;

    }

}
