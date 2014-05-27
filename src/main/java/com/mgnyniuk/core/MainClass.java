package com.mgnyniuk.core;

import com.gpusim2.config.GridSimConfig;
import com.gpusim2.config.GridSimOutput;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by maksym on 5/24/14.
 */
public class MainClass {

    public static Map<Integer, GridSimConfig> configMap;
    public static Map<Integer, GridSimOutput> outputMap;

    public static void main(String[] args) {
        HazelcastInstance hzInstance = Hazelcast.newHazelcastInstance();
        configMap = hzInstance.getMap("configMap");
        outputMap = hzInstance.getMap("outputMap");

    }
}
