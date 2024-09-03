package com.as3.facturador.global;

import com.as3.facturador.exception.FacturacionException;
import java.util.List;

public interface FacturaPublicoGeneral {

    public boolean createComprobantes() throws FacturacionException;
    public List<Integer> comprobantes() throws FacturacionException;
    public void deslizaVentana() throws FacturacionException;
}
