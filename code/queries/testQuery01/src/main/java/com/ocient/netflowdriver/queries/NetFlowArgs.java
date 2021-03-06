package com.ocient.netflowdriver.queries;

import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

public final class NetFlowArgs implements Serializable, Cloneable

{
    private static final long serialVersionUID = 1L;

    @Parameter(names = { "--cassandra-host" })
    public String cassandraHost = "localhost:2181";

    @Parameter(names = { "--keyspace" })
    public String keyspace = "netflow";

    @Parameter(names = { "--table" })
    public String table = "connections";

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) { throw new RuntimeException(); }
    }
}
