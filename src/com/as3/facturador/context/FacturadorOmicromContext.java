/* 
 * FacturadorOmicromContext
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
package com.as3.facturador.context;

import com.softcoatl.context.APPContext;
import com.softcoatl.context.services.DBService;
import com.softcoatl.database.DBException;
import com.softcoatl.database.mysql.MySQLHelper;
import com.softcoatl.integration.ServiceLoader;
import com.softcoatl.sat.cfdi.CFDIFactory;
import com.softcoatl.sat.cfdi.v40.CFDI40Factory;
import com.softcoatl.utils.logging.LogManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.naming.NamingException;
import lombok.Getter;

public class FacturadorOmicromContext {

    private static FacturadorOmicromContext instance;

    @Getter
    private final Map<String, CFDIFactory> cfdiMapper = new HashMap<>();

    private FacturadorOmicromContext() { 

        cfdiMapper.put("4.0", new CFDI40Factory());

        APPContext.setInitParameter("CFDIObjectFactory", "com.as3.facturador.schema.bridge.ObjectFactory40");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LogManager.info("Received SIGTERM signal. Terminating process...");
            }
        });
    }

    public static FacturadorOmicromContext getInstance() {
        if (FacturadorOmicromContext.instance==null) {
            FacturadorOmicromContext.instance = new FacturadorOmicromContext();
        }
        return FacturadorOmicromContext.instance;
    }

    private void setBDSession() {

        try {
            MySQLHelper.getInstance().forceExecute("SET lc_time_names = 'es_MX'");
            LogManager.info("Configurado BD Omicrom v1.0");
        } catch (DBException ex) {
            LogManager.fatal("Error iniciando contexto en MySQL", ex);
        }
    }

    public void configure(Properties poSystemProperties) {
        APPContext.getInstance().getProperties().putAll(poSystemProperties);
    }

    public void initDataBaseService(String psDataBaseName) throws ReflectiveOperationException, NamingException {
        ServiceLoader.loadService(DBService.class.getName(), psDataBaseName, psDataBaseName);
        setBDSession();
    }
}
