package com.ocient.netflowdriver.queries;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.Serializable;

@Parameters(separators="=")
public final class NetFlowArgs implements Serializable {

    private static final long serialVersionUID = 1L;

    @Parameter(names = { "--local_ip" })
    public Long local_ip = 0L;

    @Parameter(names = { "--remote_ip" })
    public Long remote_ip = 0L;

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) { throw new RuntimeException(); }
    }

}
