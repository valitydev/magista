package dev.vality.magista.query.parser;

import dev.vality.magista.query.QueryParameters;

import java.util.List;

/**
 * Created by vpankrashkin on 24.08.16.
 */
public class QueryPart {
    public static final Object DEFAULT_DESCRIPTOR = "default_descriptor";
    private final Object descriptor;
    private final QueryParameters parameters;
    private final QueryPart parent;
    private List<QueryPart> children;

    public QueryPart(Object descriptor, QueryParameters parameters) {
        this(descriptor, parameters, null);
    }

    public QueryPart(Object descriptor, QueryParameters parameters, QueryPart parent) {
        if (descriptor == null || parameters == null) {
            throw new NullPointerException("Null arguments're not allowed");
        }
        this.descriptor = descriptor;
        this.parameters = parameters;
        this.parent = parent;
    }

    public Object getDescriptor() {
        return descriptor;
    }

    public QueryParameters getParameters() {
        return parameters;
    }

    public List<QueryPart> getChildren() {
        return children;
    }

    public void setChildren(List<QueryPart> children) {
        this.children = children;
    }

    public QueryPart getParent() {
        return parent;
    }

    public boolean isEmpty() {
        return descriptor.equals(DEFAULT_DESCRIPTOR) && parameters.getParametersMap().isEmpty()
                && (children == null || children.isEmpty());
    }

    @Override
    public String toString() {
        return "QueryPart{" +
                "descriptor=" + descriptor +
                ", parameters=" + parameters +
                ", children=" + children +
                ", parent=" + (parent == null ? "null" : "notnull") +
                '}';
    }
}
