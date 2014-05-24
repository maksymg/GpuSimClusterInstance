package com.mgnyniuk.core.distributed;

import com.mgnyniuk.core.MainClass;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * Created by maksym on 5/24/14.
 */
public class SimulationRunner implements Callable<Boolean>, Serializable {

    @Override
    public Boolean call() throws Exception {
        System.out.println("Hello Hazelcast from Slave!!!");

        System.out.print("configMap Size: " + MainClass.configMap.size());

        return true;
    }
}
