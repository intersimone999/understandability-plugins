package it.unimol.understandability.utils;

import java.io.*;
import java.util.*;

/**
 * Created by simone on 01/02/17.
 */
@SuppressWarnings("Duplicates")
public class CacheManager<T> {
    private static CacheManager<Double> numericInstance;
    private static CacheManager<String> stringInstance;

    public static CacheManager<Double> getNumericInstance() {
        if (numericInstance == null)
            numericInstance = new CacheManager(Double.class);

        return numericInstance;
    }

    public static CacheManager<String> getStringInstance() {
        if (stringInstance == null)
            stringInstance = new CacheManager(String.class);

        return stringInstance;
    }

    private Map<String, Map<String, T>> maps;
    private Map<String, Set<T>> lists;
    private Class<T> valueClass;

    private CacheManager(Class<T> klass) {
        this.maps = new HashMap<>();
        this.lists = new HashMap<>();
        this.valueClass = klass;
    }

    public Set<T> loadSet(String pFilename) throws IOException {
        if (this.lists.containsKey(pFilename)) {
            return this.lists.get(pFilename);
        }

        Set<T> toAdd = new HashSet<>();

        File file = new File(pFilename);

        if (!file.exists())
            return toAdd;

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            toAdd.add(translateData(line));
        }

        this.lists.put(pFilename, toAdd);

        return toAdd;
    }

    public Map<String, T> loadMap(String pFilename) throws IOException {
        if (this.maps.containsKey(pFilename)) {
            return this.maps.get(pFilename);
        }

        Map<String, T> toAdd = new HashMap<>();

        File file = new File(pFilename);

        if (!file.exists())
            return toAdd;

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",", -1);
            String library = parts[0];
            try {
                toAdd.put(library, translateData(parts[1]));
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new RuntimeException("Impossible expansion for " + line);
            }
        }

        this.maps.put(pFilename, toAdd);

        return toAdd;
    }

    public void addCacheMapEntry(String pFilename, String key, T value) {
        Writer fileWriter = null;
        BufferedWriter output = null;
        try {
            fileWriter = new FileWriter(pFilename, true);
            output = new BufferedWriter(fileWriter);

            output.write(key + "," + value + "\n");

            output.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (output != null)
                    output.close();

                if (fileWriter != null)
                    fileWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addCacheSetEntry(String pFilename, T value) {
        Writer fileWriter = null;
        BufferedWriter output = null;
        try {
            File file = new File(pFilename);
            if (!file.exists())
                file.createNewFile();

            fileWriter = new FileWriter(file, true);
            output = new BufferedWriter(fileWriter);

            output.write(value + "\n");

            output.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (output != null)
                    output.close();

                if (fileWriter != null)
                    fileWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private T translateData(String part) {
        if (this.valueClass == String.class) {
            return (T)part;
        } else if (this.valueClass == Double.class) {
            return (T)new Double(Double.parseDouble(part));
        } else
            throw new RuntimeException("No suitable type!");
    }
}
