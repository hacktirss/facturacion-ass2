package com.as3.facturador.commons;

import com.softcoatl.commons.NumberToLetterConvertionException;
import java.math.BigDecimal;

public interface ComprobantePrintable {
    public String getDescripcionTipoComprobante();
    public String getDescripcionUsoCFDI();
    public String getTotalLetra() throws NumberToLetterConvertionException;
    public String getDescripcionFormaDePago();
    public String getDescripcionMetodoDePago();
    public String getDescripcionTipoRelacion();
    public BigDecimal getImporteTotalDocumento();
    public String getFolioDocumento();
}
