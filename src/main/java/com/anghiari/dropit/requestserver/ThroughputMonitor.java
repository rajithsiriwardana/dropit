package com.anghiari.dropit.requestserver;
public class ThroughputMonitor extends Thread {  
  
    /** 
     * Constructor 
     */  
    public ThroughputMonitor() {  
    }  
  
    @Override  
    public void run() {  
        try {  
            long oldCounter = ObjectHandler.getTransferredBytes();  
            long startTime = System.currentTimeMillis();  
            for (;;) {  
                Thread.sleep(3000);  
  
                long endTime = System.currentTimeMillis();  
                long newCounter = ObjectHandler.getTransferredBytes();  
                System.err.format("%4.3f MiB/s%n", (newCounter - oldCounter) *  
                        1000 / (endTime - startTime) / 1048576.0);  
                oldCounter = newCounter;  
                startTime = endTime;  
            }  
            
        } catch (InterruptedException e) {  
            // Stop monitoring asked  
            return;  
        }  
    }  
}  