/*
 * Configurator
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since nov 2019 
 */
package com.as3.facturador.commons;

import com.as3.facturador.exception.CertificadoReaderException;
import com.as3.facturador.process.AcuseSaverProcessor;
import com.as3.facturador.process.CancelacionProcessor;
import com.as3.facturador.process.CuentasPorCobrarProcessor;
import com.as3.facturador.process.NCUpdaterProcessor;
import com.as3.facturador.process.TimbradoProcessor;
import com.as3.facturador.process.MailerProcessor;
import com.as3.facturador.process.FacturaSaverProcessor;
import com.as3.facturador.process.FCUpdaterProcessor;
import com.as3.facturador.process.IngresosUpdaterProcessor;
import com.as3.facturador.process.SelloProcessor;
import com.as3.facturador.process.PagoUpdaterProcessor;
import com.as3.facturador.process.PDFGeneratorProcessor;
import com.as3.facturador.process.POSPeripheralPreProcessor;
import com.as3.facturador.process.POSPreProcessor;
import com.as3.facturador.process.POSTrPreProcessor;
import com.as3.facturador.process.TrasladosUpdaterProcessor;

public class Configurator {

    public void configure() throws CertificadoReaderException {

        POSPreProcessor.getInstance().setNext(SelloProcessor.getInstance());
        POSTrPreProcessor.getInstance().setNext(SelloProcessor.getInstance());
        POSPeripheralPreProcessor.getInstance().setNext(SelloProcessor.getInstance());

        SelloProcessor.getInstance().setNext(TimbradoProcessor.getInstance());
        TimbradoProcessor.getInstance().setNext(FCUpdaterProcessor.getInstance());
        FCUpdaterProcessor.getInstance().setNext(NCUpdaterProcessor.getInstance());
        NCUpdaterProcessor.getInstance().setNext(PagoUpdaterProcessor.getInstance());
        PagoUpdaterProcessor.getInstance().setNext(TrasladosUpdaterProcessor.getInstance());
        TrasladosUpdaterProcessor.getInstance().setNext(IngresosUpdaterProcessor.getInstance());
        IngresosUpdaterProcessor.getInstance().setNext(CuentasPorCobrarProcessor.getInstance());
        CuentasPorCobrarProcessor.getInstance().setNext(FacturaSaverProcessor.getInstance());
        FacturaSaverProcessor.getInstance().setNext(PDFGeneratorProcessor.getInstance());
        PDFGeneratorProcessor.getInstance().setNext(MailerProcessor.getInstance());

        CancelacionProcessor.getInstance().setNext(AcuseSaverProcessor.getInstance());
    }
}
