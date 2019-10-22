package com.github.mihone.redismq.yaml;

import com.github.mihone.redismq.exception.FileNotFoundException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public final class YmlUtils {
    private static final HashMap<String, Object> basicMap = new HashMap<>();
    private static final HashMap<String, Object> extendMap = new HashMap<>();

    static {
        if (!initByYml()) {
            if (!initByProperties()) {
                throw new FileNotFoundException("there is no application.yml or application.properties in the root dictionary of the project");
            }
        }
    }

    private YmlUtils() {
    }

    /**
     * Get the value of the specified key.
     * return null if not exists.<br>
     * eg of key: spring.profiles.active
     * @param key key in the configuration file
     * @return Object value of key.Null if not has key or value of key is null
     * @throws IllegalArgumentException if the pattern of key is not correspond with rules<br>
     * @author mihone
     * @since 2019/9/28
     */
    public static Object getValue(final String key) {
        if (!key.matches("[a-zA-Z.-]+")) {
            throw new IllegalArgumentException("key is invalid");
        }
        return basicMap.get(key) == null ? extendMap.get(key) : basicMap.get(key);
    }

    /**
     * init application config by .properties
     *
     * @return true if init success
     * @throws FileNotFoundException if load application.properties failed
     * @author mihone
     * @since 2019/9/28
     */
    @SuppressWarnings("unchecked")
    private static boolean initByProperties() {
        InputStream in = YmlUtils.class.getClassLoader().getResourceAsStream("application.properties");
        Properties prop = new Properties();
        if (in != null) {
            try {
                prop.load(in);
                prop.forEach((key, value) -> basicMap.put((String) key, value));
                Object active = basicMap.get("spring.profiles.active");
                if (active != null) {
                    InputStream extendIn = YmlUtils.class.getClassLoader().getResourceAsStream("application-" + active + ".properties");
                    if (extendIn != null) {
                        prop.load(extendIn);
                        prop.forEach((key, value) -> extendMap.put((String) key, value));
                    }

                }
            } catch (IOException e) {
                throw new FileNotFoundException("load application.properties failed.cause:" + e.getMessage());
            }
            return true;
        }
        return false;
    }

    /**
     * init application config by .yml
     *
     * @return true if init success
     * @throws FileNotFoundException if load application.yml failed
     * @author mihone
     * @since 2019/9/28
     */
    @SuppressWarnings("unchecked")
    private static boolean initByYml() {
        InputStream in = YmlUtils.class.getClassLoader().getResourceAsStream("application.yml");
        if (in != null) {
            Yaml yml = new Yaml();
            HashMap<String, Object> basicMapSrc = yml.loadAs(in, HashMap.class);
            setInMap(basicMap, basicMapSrc, null);
            Object active = basicMap.get("spring.profiles.active");
            if (active != null) {
                InputStream extendIn = YmlUtils.class.getClassLoader().getResourceAsStream("application-" + active + ".yml");
                if (extendIn != null) {
                    HashMap<String, Object> extendMapSrc = yml.loadAs(extendIn, HashMap.class);
                    setInMap(extendMap, extendMapSrc, null);
                }
            }
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static void setInMap(HashMap<String, Object> dest, HashMap<String, Object> src, String key) {
        Set<Map.Entry<String, Object>> entries = src.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            if (!(entry.getValue() instanceof Map)) {
                if (key == null) {
                    dest.put(entry.getKey(), entry.getValue());
                } else {
                    dest.put(key + "." + entry.getKey(), entry.getValue());
                }
            } else {
                setInMap(dest, (HashMap<String, Object>) entry.getValue(), key == null ? "" + entry.getKey() : key + "." + entry.getKey());
            }
        }
    }
}
