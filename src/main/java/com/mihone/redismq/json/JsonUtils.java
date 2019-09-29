package com.mihone.redismq.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.mihone.redismq.exception.JSONConvertException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtils {
    private static final ObjectMapper om = new ObjectMapper();

    private JsonUtils() {
    }

    public static String convertToJson(Object obj) {
        try {
            return om.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new JSONConvertException("can not convert Object to String");
        }
    }

    public static byte[] convertToBytes(Object obj) {
        try {
            return om.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new JSONConvertException("can not convert Object to bytes");
        }
    }

    public static JsonNode addAttributes(Object src, Map<String, Object> attrs) {
        ObjectNode node = (ObjectNode) om.valueToTree(src);
        HashMap<String, JsonNode> properties = new HashMap<String, JsonNode>();
        attrs.forEach((key, value) -> properties.put(key, om.valueToTree(value)));
        node.setAll(properties);
        return node;
    }

    public static JsonNode addAttribute(Object src, String fieldName, Object fieldValue) {
        ObjectNode node = (ObjectNode) om.valueToTree(src);
        node.set(fieldName, om.valueToTree(fieldValue));
        return node;
    }

    public static String addAttributesAndToString(Object src, Map<String, Object> attrs) {
        JsonNode node = addAttributes(src, attrs);
        return convertToJson(node);
    }

    public static String addAttributeAndToString(Object src, String fieldName, Object fieldValue) {
        JsonNode node = addAttribute(src, fieldName, fieldValue);
        return convertToJson(node);
    }
    public static byte[] addAttributesAndToBytes(Object src, Map<String, Object> attrs) {
        JsonNode node = addAttributes(src, attrs);
        return convertToBytes(node);
    }

    public static byte[] addAttributeAndToBytes(Object src, String fieldName, Object fieldValue) {
        JsonNode node = addAttribute(src, fieldName, fieldValue);
        return convertToBytes(node);
    }
    public static <T> T  convertObjectFromString(String jsonString,Class<T> clazz){
        try {
            return om.readValue(jsonString, clazz);
        } catch (IOException e) {
            throw new JSONConvertException("can not convert json String to specified Object");
        }
    }
    public static <T> T  convertObjectFromBytes(byte[] bytes,Class<T> clazz){
        try {
            return om.readValue(bytes, clazz);
        } catch (IOException e) {
            throw new JSONConvertException("can not convert json String to specified Object");
        }
    }
    public static <T> List<T> convertObjectAsList(String jsonString,Class<T> clazz){
        CollectionType collectionType = om.getTypeFactory().constructCollectionType(List.class, clazz);
        try {
            return om.readValue(jsonString, collectionType);
        } catch (IOException e) {
            throw new JSONConvertException("can not convert json String to list");
        }
    }
    public static <T> List<T> convertObjectAsList(byte[] bytes,Class<T> clazz){
        CollectionType collectionType = om.getTypeFactory().constructCollectionType(List.class, clazz);
        try {
            return om.readValue(bytes, collectionType);
        } catch (IOException e) {
            throw new JSONConvertException("can not convert json String to list");
        }
    }
    public static <T> List<T> convertObjectAsMap(byte[] bytes,Class<T> keyClass,Class<T> valueClass){
        MapType mapType = om.getTypeFactory().constructMapType(Map.class, keyClass, valueClass);
        try {
            return om.readValue(bytes, mapType);
        } catch (IOException e) {
            throw new JSONConvertException("can not convert json String to map");
        }
    }
    public static <T> List<T> convertObjectAsMap(String jsonString,Class<T> keyClass,Class<T> valueClass){
        MapType mapType = om.getTypeFactory().constructMapType(Map.class, keyClass, valueClass);
        try {
            return om.readValue(jsonString, mapType);
        } catch (IOException e) {
            throw new JSONConvertException("can not convert json String to map");
        }
    }
}
