package com.github.mihone.redismq.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.github.mihone.redismq.log.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JsonUtils {
    private static final ObjectMapper om = new ObjectMapper();
    private static final Log log = Log.getLogger(JsonUtils.class);

    private JsonUtils() {
    }

    public static String convertToJson(Object obj) {
        try {
            return om.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("can not convert Object to String.Param:{},Cause:{}",obj,e);
            return  null;
        }
    }

    public static byte[] convertToBytes(Object obj) {
        try {
            return om.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.error("can not convert Object to bytes.Param:{},Cause:{}",obj,e);
            return null;
        }
    }

    public static JsonNode addAttributes(Object src, Map<String, Object> attrs) {
        JsonNode jsonNode =  om.valueToTree(src);
        HashMap<String, JsonNode> properties = new HashMap<>();
        attrs.forEach((key, value) -> properties.put(key, om.valueToTree(value)));
        if (jsonNode instanceof ValueNode) {
            return new ObjectNodeAdaptor((ValueNode) jsonNode,properties);
        }else{
           return ((ObjectNode)jsonNode).setAll(properties);
        }
    }

    public static JsonNode addAttribute(Object src, String fieldName, Object fieldValue) {
        JsonNode jsonNode = om.valueToTree(src);
        if (jsonNode instanceof ValueNode) {
            ObjectNode node = new ObjectNodeAdaptor((ValueNode) jsonNode);
            return node.set(fieldName, om.valueToTree(fieldValue));
        }else{
            return ((ObjectNode)jsonNode).set(fieldName,om.valueToTree(fieldValue));
        }
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

    public static JsonNode removeAttributes(byte[] dataBytes,String... filedNames){
        try {
            ObjectNode node = (ObjectNode)om.readTree(dataBytes);
            for (String filedName : filedNames) {
                JsonNode remove = node.remove(filedName);
            }
            return node;
        } catch (IOException e) {
            log.error("can not read value from byte array ,please check the byte array.Param:{},{},Cause:{}",dataBytes,filedNames,e);
            return null;
        }
    }
    public static<T> T removeAndConvertObject(byte[] dataBytes,Class<T> clazz,String... filedNames){
        JsonNode node = removeAttributes(dataBytes, filedNames);
        return convertObjectFromBytes(convertToBytes(node), clazz);
    }

    public static ObjectNode read(byte[] bytes){
        try {
            return (ObjectNode)om.readTree(bytes);
        } catch (IOException e) {
            log.error("can not read value from byte array ,please check the byte array.Param:{},Cause:{}",bytes,e);
            return null;
        }
    }
    public static <T> T  convertObjectFromString(String jsonString,Class<T> clazz){
        try {
            return om.readValue(jsonString, clazz);
        } catch (IOException e) {
            log.error("can not convert json String to specified Object.Param:{},{},Cause:{}",jsonString,clazz,e);
            return null;
        }
    }
    public static <T> T  convertObjectFromBytes(byte[] bytes,Class<T> clazz){
        try {
            return om.readValue(bytes, clazz);
        } catch (IOException e) {
            log.error("can not convert byte[] to specified Object.Param:{},{},Cause:{}",bytes,clazz,e);
            return null;
        }
    }
    public  static <T> T convertObjectFromJsonNode(JsonNode node,Class<T> clazz){
        if (node instanceof ObjectNodeAdaptor) {
            return convertObjectFromBytes(convertToBytes(((ObjectNodeAdaptor)node).getValue()), clazz);
        }
        return convertObjectFromBytes(convertToBytes(node), clazz);
    }
    public static <T> List<T> convertObjectAsList(String jsonString,Class<T> clazz){
        CollectionType collectionType = om.getTypeFactory().constructCollectionType(List.class, clazz);
        try {
            return om.readValue(jsonString, collectionType);
        } catch (IOException e) {
            log.error("can not convert JsonNode to specified Object.Param:{},{},Cause:{}",jsonString,clazz,e);
            return null;
        }
    }
    public static <T> List<T> convertObjectAsList(byte[] bytes,Class<T> clazz){
        CollectionType collectionType = om.getTypeFactory().constructCollectionType(List.class, clazz);
        try {
            return om.readValue(bytes, collectionType);
        } catch (IOException e) {
            log.error("can not convert json String to list.Param:{},{},Cause:{}",bytes,clazz,e);
            return null;
        }
    }
    public static <K,V> Map<K,V> convertObjectAsMap(byte[] bytes,Class<K> keyClass,Class<V> valueClass){
        MapType mapType = om.getTypeFactory().constructMapType(Map.class, keyClass, valueClass);
        try {
            return om.readValue(bytes, mapType);
        } catch (IOException e) {
            log.error("can not convert json String to map.Param:{},{},{},Cause:{}",bytes,keyClass,valueClass,e);
            return null;
        }
    }
    public static <K,V> Map<K,V> convertObjectAsMap(String jsonString,Class<K> keyClass,Class<V> valueClass){
        MapType mapType = om.getTypeFactory().constructMapType(Map.class, keyClass, valueClass);
        try {
            return om.readValue(jsonString, mapType);
        } catch (IOException e) {
            log.error("can not convert json String to map.Param:{},{},{},Cause:{}",jsonString,keyClass,valueClass,e);
            return null;
        }
    }
}
