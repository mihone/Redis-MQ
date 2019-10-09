package com.github.mihone.redismq.json;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.BaseJsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.io.IOException;
import java.util.Map;

public class ObjectNodeAdaptor extends ObjectNode {

    private  ValueNode valueNode ;

    public ObjectNodeAdaptor (ValueNode valueNode){
        super(new JsonNodeFactory(false));
        this.valueNode=valueNode;
    }

    public ObjectNodeAdaptor(ValueNode valueNode,Map<String, JsonNode> kids) {
        super(new JsonNodeFactory(false), kids);
        this.valueNode=valueNode;

    }
    public JsonNode getValue(){
        return valueNode;
    }

    @Override
    public String toString() {
        return "ObjectNodeAdaptor{ valueNode:"+valueNode+"," +super.toString()+
                '}';
    }

    @Override
    public void serialize(JsonGenerator g, SerializerProvider provider) throws IOException {

        @SuppressWarnings("deprecation")
        boolean trimEmptyArray = (provider != null) &&
                !provider.isEnabled(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        g.writeStartObject(this);
        g.writeFieldName(valueNode.getClass().getSuperclass().getSimpleName());
        valueNode.serialize(g,provider);
        for (Map.Entry<String, JsonNode> en : _children.entrySet()) {
            BaseJsonNode value = (BaseJsonNode) en.getValue();
            if (trimEmptyArray && value.isArray() && value.isEmpty(provider)) {
                continue;
            }
            g.writeFieldName(en.getKey());
            value.serialize(g, provider);
        }
        g.writeEndObject();


    }
}
