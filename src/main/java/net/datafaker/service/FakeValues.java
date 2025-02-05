package net.datafaker.service;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FakeValues implements FakeValuesInterface {
    private static final Logger LOG = Logger.getLogger("faker");
    private final Locale locale;
    private final String filename;
    private final String path;
    private Map<String, Object> values;

    FakeValues(Locale locale) {
        this(locale, getFilename(locale), getFilename(locale));
    }

    private static String getFilename(Locale locale) {
        final StringBuilder filename = new StringBuilder(language(locale));
        if (!"".equals(locale.getCountry())) {
            filename.append("-").append(locale.getCountry());
        }
        return filename.toString();
    }

    /**
     * If you new up a locale with "he", it gets converted to "iw" which is old.
     * This addresses that unfortunate condition.
     */
    private static String language(Locale l) {
        if (l.getLanguage().equals("iw")) {
            return "he";
        }
        return l.getLanguage();
    }

    FakeValues(Locale locale, String filename, String path) {
        this.locale = locale;
        this.filename = filename;
        this.path = path;
    }

    @Override
    public Map<String, Object> get(String key) {
        if (values == null) {
            values = loadValues();
        }

        return values == null ? null : (Map) values.get(key);
    }

    private Map<String, Object> loadValues() {
        String pathWithLocaleAndFilename = "/" + locale.getLanguage() + "/" + this.filename;
        String pathWithFilename = "/" + filename + ".yml";
        String pathWithLocale = "/" + locale.getLanguage() + ".yml";

        List<String> paths = Arrays.asList(pathWithLocaleAndFilename, pathWithFilename, pathWithLocale);
        Map<String, Object> result = null;
        for (String path : paths) {
            try (InputStream stream = getClass().getResourceAsStream(path)) {
                if (stream != null) {
                   result = readFromStream(stream);
                } else {
                    try (InputStream stream2 = getClass().getClassLoader().getResourceAsStream(path)) {
                        result = readFromStream(stream2);
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, "Exception: ", e);
                    }
                }

            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Exception: ", e);
            }
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private Map<String, Object> readFromStream(InputStream stream) {
        if (stream == null) return null;
        final Map<String, Object> valuesMap = new Yaml().loadAs(stream, Map.class);
        Map<String, Object> localeBased = (Map<String, Object>) valuesMap.get(locale.getLanguage());
        if (localeBased == null) {
            localeBased = (Map<String, Object>) valuesMap.get(filename);
        }
        return (Map<String, Object>) localeBased.get("faker");
    }

    boolean supportsPath(String path) {
        return this.path.equals(path);
    }

    public String getPath() {
        return path;
    }
}
