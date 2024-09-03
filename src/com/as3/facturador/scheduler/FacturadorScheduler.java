/*
 * FacturadorScheduler
 * FacturadorOmicrom®
 * © 2021, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since sep 2021 
 */
package com.as3.facturador.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FacturadorScheduler {
    
    private static final FacturadorScheduler INSTANCE = new FacturadorScheduler();
    private ScheduledExecutorService executor;

    public static FacturadorScheduler getInstance() {
        return INSTANCE;
    }

    private FacturadorScheduler() {
        executor = Executors.newScheduledThreadPool(4);
    }

    public ScheduledFuture scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return executor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public ScheduledFuture schedule(Runnable command, long initialDelay, TimeUnit unit) {
        return executor.schedule(command, initialDelay, unit);
    }
}
