/*
 * FacturaSaverProcessor
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since nov 2019 
 */
package com.as3.facturador.process;

import com.as3.facturador.context.FacturadorOmicromContext;
import com.as3.facturador.dao.BitacoraDAO;
import com.as3.facturador.dao.FacturaDAO;
import com.as3.facturador.dao.VariablesDAO;
import com.as3.facturador.server.DocType;
import com.softcoatl.sat.cfdi.schema.CFDI;
import com.softcoatl.utils.logging.LogManager;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.commons.io.FileUtils;
import com.as3.facturador.schema.ComprobanteDetisa;
import static com.as3.facturador.server.DocType.TR;
import com.softcoatl.sat.cfdi.CFDIException;
import com.softcoatl.sat.cfdi.marshalling.CFDIJAXBMarshaller;
import com.softcoatl.sat.cfdi.v40.CFDI40;
import com.softcoatl.sat.cfdi.v40.schema.Comprobante40;
import com.softcoatl.utils.PropertyLoader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class FacturaSaverProcessor extends BaseProcessor implements FacturaProcessor {

    public static final String XML_STORAGE_PATH = "xml_storage";
    
    private static final class Loader {
        static final FacturaSaverProcessor INSTANCE = new FacturaSaverProcessor();
    }

    public static FacturaSaverProcessor getInstance() {
        return Loader.INSTANCE;
    }

    private FacturaSaverProcessor() {}
    
    private int decodeTabla(DocType type) {
        switch(type) {
            case FA:
            case FG: return 1;
            case CR: return 2;
            case RP: return 3;
            case AN: return 4;
            case TR: return 5;
            case CPI: return 6;
            default: return 1;
        }
    }
    private void saveInWorkPath(ComprobanteDetisa comprobante) {
        try {
            String storage = VariablesDAO.getVariable("xml_storage");
            FileUtils.writeByteArrayToFile(new File(storage, comprobante.getUuid() + ".xml"), comprobante.getXml());
        } catch (SQLException | IOException ex) {
            LogManager.error("Error almacenando archivo " + comprobante.getUuid() + "en ruta de trabajo: " + ex.getMessage());
            comprobante.setError(ex.getMessage());
        }
    }
    private void saveInDataBase(ComprobanteDetisa comprobante) {
        try {
            FacturaDAO.save(comprobante.getId(), decodeTabla(comprobante.getDocType()), (CFDI) comprobante.getCfdi(), comprobante.getXml());
        } catch (SQLException ex) {
            LogManager.error("Error almacenando archivo " + comprobante.getUuid() + "en base de datos: " + ex.getMessage());
            comprobante.setError(ex.getMessage());
        }
    }

    private void registerBitacora(ComprobanteDetisa comprobante) {
        if (!comprobante.getDespachador().isInvalid()) {
            BitacoraDAO.evento("Factura " + comprobante.getId() + " generada por " + comprobante.getDespachador().getNombre()+ "(" + comprobante.getDespachador().getId() + ")");
        }
    }

    @Override
    ComprobanteDetisa execute(ComprobanteDetisa comprobante) {

        LogManager.info("Guardando CFDI " + comprobante.getUuid() + " en facturas.");
        saveInDataBase(comprobante);
        saveInWorkPath(comprobante);
        registerBitacora(comprobante);
        if ( comprobante.isValid() ) {
            LogManager.info("CFDI guardado...");
        }
        return comprobante;
    }
    
    public static void main(String[] args) throws JAXBException, CFDIException, FileNotFoundException, SQLException, ReflectiveOperationException, NamingException {
        Properties wsProperties = PropertyLoader.load("facturador.properties");
        FacturadorOmicromContext context = FacturadorOmicromContext.getInstance();
        context.configure(wsProperties);
        context.initDataBaseService("DATASOURCE");
        File archivo = new File("5FE91800-0831-4D44-80CE-C5A9E4DE39BE.xml");
        FileInputStream input = new FileInputStream(archivo);
        CFDI cfdi = new CFDI40(new CFDIJAXBMarshaller(JAXBContext.newInstance(Comprobante40.class)), input);
        FacturaDAO.save(1, 1, cfdi, cfdi.toXML().getBytes());
    }
}
