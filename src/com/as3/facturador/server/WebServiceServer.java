/* 
 * WebServiceServer
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
package com.as3.facturador.server;

import com.detisa.commons.ServiceRegister;
import com.as3.facturador.commons.Configurator;
import com.as3.facturador.context.FacturadorOmicromContext;
import com.as3.facturador.exception.FacturacionException;
import com.as3.facturador.jobs.CheckCFDIStatusJob;
import com.as3.facturador.jobs.FacturaWindow;
import com.as3.facturador.jobs.FoliosAbiertosCheck;
import com.as3.facturador.jobs.LoggerMonitor;
import com.as3.facturador.jobs.ValidateCertificatesCheck;
import com.as3.facturador.scheduler.FacturadorScheduler;
import com.softcoatl.utils.PropertyLoader;
import com.softcoatl.utils.logging.LogManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.naming.NamingException;
import javax.xml.ws.Endpoint;

public class WebServiceServer {

    public static final String ENDPOINT = "ws.endpoint";
    public static final String MODULE = "Facturacion";
    public static final String VERSION  = "2.2.4.3";

    public static void main(String[] args) {

        Properties wsProperties;

        try {
            System.out.println("Iniciando Servidor de Facturación");
            LogManager.info("Iniciando Servidor de Facturación");

            wsProperties = PropertyLoader.load("facturador.properties");
            if (!wsProperties.containsKey(ENDPOINT)) {
                throw new FacturacionException("ERROR: endpoint not defined!");
            }

            // TODO disable in productive enviroment
            System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
            System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
            System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
            System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
            // Sets system properties
            System.setProperty("java.net.useSystemProxies", "false");
            System.setProperty("com.sun.xml.internal.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "false");
            System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "false");

            FacturadorOmicromContext context = FacturadorOmicromContext.getInstance();
            context.configure(wsProperties);
            context.initDataBaseService("DATASOURCE");
            new Configurator().configure();

            ServiceRegister.registerWSVersion(WebServiceServer.class, MODULE, VERSION);

            Endpoint endpoint = Endpoint.create(new FacturadorWSImpl());
            endpoint.publish(wsProperties.getProperty(ENDPOINT));
            LogManager.info("Endpoint started at "+wsProperties.getProperty(ENDPOINT));

            FacturadorScheduler.getInstance().scheduleAtFixedRate(new CheckCFDIStatusJob(), 0, 15, TimeUnit.MINUTES);
            FacturadorScheduler.getInstance().scheduleAtFixedRate(new FoliosAbiertosCheck(), 0, 1, TimeUnit.HOURS);
            FacturadorScheduler.getInstance().scheduleAtFixedRate(new LoggerMonitor(), 0, 60, TimeUnit.SECONDS);
            FacturadorScheduler.getInstance().schedule(new FacturaWindow(), 5, TimeUnit.MINUTES);

            new ValidateCertificatesCheck().run();
        } catch (FacturacionException | IOException | ReflectiveOperationException | URISyntaxException | NoSuchAlgorithmException | NamingException e) {
            LogManager.error("FATAL", e);
        }
    }
}
