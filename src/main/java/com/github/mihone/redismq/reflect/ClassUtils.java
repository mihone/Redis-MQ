package com.github.mihone.redismq.reflect;

import com.github.mihone.redismq.exception.ClassResolveFailedException;
import com.github.mihone.redismq.log.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public final class ClassUtils {
    private static final Log log = Log.getLogger(ClassUtils.class);
    /**
     * Get all classes by recursive under the dictionary which the given class in
     *
     * @param clazz class in the root dictionary
     * @return classes all in the root dictionary
     * @author mihone
     * @since 2019/10/2
     */
    public static <T> List<Class<?>> getAllClasses(Class<T> clazz) {
        String root = clazz.getName().substring(0, clazz.getName().lastIndexOf(clazz.getSimpleName()) - 1);
        String rootPkgName = root.replace(".", "/");
        try {
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(rootPkgName);
            List<Class<?>> list = new ArrayList<Class<?>>();
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String path = URLDecoder.decode(url.getPath(), "utf-8");
                File rootDictionary = new File(path);
                list.addAll(getClasses(root, rootDictionary));
            }
            return list;
        } catch (IOException e) {
            throw new ClassResolveFailedException("class reslove failed,cause:" + e);
        }
    }

    public static boolean isRealClass(Class<?> clazz) {
        return !(clazz.isInterface() || clazz.isAnnotation() || clazz.isEnum() || Modifier.isAbstract(clazz.getModifiers()));
    }

    public static Object[] getDefaultArgs(Parameter[] parameters) {
        Class<?>[] argClasses = getArgClasses(parameters);
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < argClasses.length; i++) {
            if (argClasses[i].isPrimitive()) {
                if (argClasses[i].getTypeName().equals("char")) {
                    args[i] = ' ';
                } else {
                    args[i] = 0;
                }
            } else {
                args[i] = null;
            }
        }
        return args;
    }

    public static Class<?>[] getArgClasses(Parameter[] parameters) {
        Class<?>[] argsClasses = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            argsClasses[i] = parameters[i].getType();
        }
        return argsClasses;
    }

    private static List<Class<?>> getClasses(String root, File rootDictionary) {
        List<Class<?>> list = Arrays.stream(rootDictionary.listFiles(file -> !file.isDirectory())).filter(file->file.getName().contains(".class")).map(file -> {
            try {
                String name = root + "." + file.getName().substring(0, file.getName().lastIndexOf("."));
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                log.error("Class not found.Cause:",e);
                throw new RuntimeException();
            }
        }).collect(Collectors.toList());
        if (rootDictionary.listFiles(File::isDirectory).length > 0) {
            Arrays.stream(rootDictionary.listFiles(File::isDirectory)).forEach(dic -> list.addAll(getClasses(root + "." + dic.getName(), dic)));
        }
        return list;

    }
}

