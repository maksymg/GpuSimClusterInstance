package com.mgnyniuk.core.distributed;

import com.gpusim2.config.GridSimConfig;
import com.mgnyniuk.core.MainClass;
import com.mgnyniuk.core.parallel.NotifyingThread;
import com.mgnyniuk.core.parallel.ThreadListener;
import com.mgnyniuk.core.parallel.WorkerThread;

import java.beans.XMLEncoder;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by maksym on 5/24/14.
 */
public class SimulationRunner implements Callable<Boolean>, Serializable {

    private final String CONFIG = "config%s.xml";
    private final String OUTPUT = "output%s.xml";
    private int overallProcessesQuantity;
    private int partProcessesQuantity;
    private int startIndex;

    public SimulationRunner() {}

    public SimulationRunner(int overallProcessesQuantity, int partProcessesQuantity, int startIndex) {
        this.overallProcessesQuantity = overallProcessesQuantity;
        this.partProcessesQuantity = partProcessesQuantity;
        this.startIndex = startIndex;
    }

    private void serializeConfigs(Map<Integer, GridSimConfig> configMap) throws FileNotFoundException {

        for (Integer i=startIndex; i < overallProcessesQuantity; i++) {

            GridSimConfig gridSimConfig = configMap.get(i);

            FileOutputStream out = new FileOutputStream("config" + i + ".xml");
            XMLEncoder xmlEncoder = new XMLEncoder(out);
            xmlEncoder.writeObject(gridSimConfig);
            xmlEncoder.flush();
            xmlEncoder.close();
        }

    }

    @Override
    public Boolean call() throws Exception {
        System.out.println("Hello Hazelcast from Slave!!!");

        System.out.print("configMap Size: " + MainClass.configMap.size());

        serializeConfigs(MainClass.configMap);

        //ConfigurationUtil.serializeConfigs(gridSimConfigList, startIndex);
        ThreadListener threadListener = new ThreadListener();


        for (int j = 0; j < overallProcessesQuantity/partProcessesQuantity; j++) {
            ExecutorService es = Executors.newCachedThreadPool();
            for (int i = startIndex; i < startIndex + partProcessesQuantity; i++) {

                NotifyingThread notifyingThread = new WorkerThread("GpuSimV2.jar",
                        String.format(CONFIG, (i + j * partProcessesQuantity)),
                        String.format(OUTPUT, (i + j * partProcessesQuantity)));
                notifyingThread.addListener(threadListener);
                //notifyingThread.start();
                es.execute(notifyingThread);
            }

            es.shutdown();

            while(!es.isTerminated()) {
                continue;
            }
        }

        return true;
    }
}
