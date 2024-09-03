/*
 * TemplateLoader
 * DetisaCommons®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since nov 2020 
 */
package com.as3.facturador.commons;

import com.softcoatl.sat.cfdi.schema.CFDI;

public interface XSLTemplateLoader {

    public XSLTemplate getTemplate(CFDI cfdi);
}
