package com.mgnyniuk.core.distributed;

import com.gpusim2.config.GridSimConfig;
import com.gpusim2.config.GridSimOutput;
import com.gpusim2.config.IncompatibleVersionException;
import com.mgnyniuk.core.MainClass;
import com.mgnyniuk.core.parallel.NotifyingThread;
import com.mgnyniuk.core.parallel.ThreadListener;
import com.mgnyniuk.core.parallel.WorkerThread;
import com.mgnyniuk.core.util.FileManager;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
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

    private void serializeConfigs(Map<Integer, GridSimConfig> configMap) {

        System.out.println("Overall : " + overallProcessesQuantity);

        for (Integer i = 0; i < overallProcessesQuantity; i++) {

            GridSimConfig gridSimConfig = configMap.get(i);

            try {
                FileOutputStream out = new FileOutputStream("config" + (i + startIndex) + ".xml");
                XMLEncoder xmlEncoder = new XMLEncoder(out);
                xmlEncoder.writeObject(gridSimConfig);
                xmlEncoder.flush();
                xmlEncoder.close();
            } catch (FileNotFoundException ex) {
                System.out.println(ex.getMessage());
            }

        }

    }

    private void deserializeOutputs(Map<Integer, GridSimOutput> outputMap) throws FileNotFoundException, IncompatibleVersionException {

        GridSimOutput gridSimOutput;

        for (int i = 0; i < overallProcessesQuantity; i++) {

            FileInputStream in = new FileInputStream("output" + (i + startIndex) + ".xml");
            XMLDecoder xmlDecoder = new XMLDecoder(in);
            gridSimOutput = (GridSimOutput) xmlDecoder.readObject();
            xmlDecoder.close();

            outputMap.put(i + startIndex, gridSimOutput);
        }
    }

    @Override
    public Boolean call() throws Exception {
        System.out.println("Hello Hazelcast from Slave!!!");

        System.out.println("configMap Size: " + MainClass.configMap.size());

        System.out.println("OverallProcessesQuantity: " + overallProcessesQuantity);
        System.out.println("PartProcessesQuantity: " + partProcessesQuantity);
        System.out.println("StartIndex: " + startIndex);

        // delete configs files from previous experiment
        FileManager.deleteFilesFromCurrentDir("config.*\\.xml");
        // delete outputs files from previous experiment
        FileManager.deleteFilesFromCurrentDir("output.*\\.xml");

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

        deserializeOutputs(MainClass.outputMap);

        return true;
    }
}
