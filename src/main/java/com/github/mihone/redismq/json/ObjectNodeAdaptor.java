package com.github.mihone.redismq.json;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.util.Map;

public class ObjectNodeAdaptor extends ObjectNode {

    private ValueNode valueNode;


    public ObjectNodeAdaptor (ValueNode valueNode){
        super(new JsonNodeFactory(false));
        this.valueNode=valueNode;
    }

    public ObjectNodeAdaptor(ValueNode valueNode,Map<String, JsonNode> kids) {
        super(new JsonNodeFactory(false), kids);
        this.valueNode = valueNode;
    }
    public JsonNode getValue(){
        return valueNode;
    }
}
