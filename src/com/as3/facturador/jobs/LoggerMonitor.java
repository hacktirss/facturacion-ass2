/*
 * LoggerMonitor
 * DPOS®
 * © 2016, Detisa 
 * http://www.detisa.com.mx
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since jun 2016
 */
package com.as3.facturador.jobs;

import com.as3.facturador.dao.VariablesDAO;
import com.softcoatl.utils.logging.LogManager;
import com.softcoatl.utils.DateUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;

public class LoggerMonitor implements Runnable {

    private final Logger LOG = LogManager.getRootLogger();
    public static final String CRON = "jobs.pos.alive.cron";

    private void setLoggerLevel() {
        Level confLevel = Level.toLevel(VariablesDAO.getVariable("fact_log_level", "'INFO'").toUpperCase());
        LogManager.setLevel(confLevel);
    }


    @Override
    public void run() {
        LOG.info("Proceso en ejecución " + DateUtils.fncsFormat("dd/MM/yyyy HH:mm:ss"));
        LOG.debug("Proceso en ejecución y debug " + DateUtils.fncsFormat("dd/MM/yyyy HH:mm:ss"));
        setLoggerLevel();
    }
}
