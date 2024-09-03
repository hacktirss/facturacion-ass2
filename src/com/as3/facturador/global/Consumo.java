package com.as3.facturador.global;

import com.as3.facturador.dao.BitacoraDAO;
import com.as3.facturador.dao.FacturaDAO;
import com.as3.facturador.dao.VariablesDAO;
import com.as3.facturador.exception.FacturacionException;
import com.softcoatl.utils.logging.LogManager;
import java.util.List;

public class Consumo implements FacturaPublicoGeneral {

    @Override
    public boolean createComprobantes() throws FacturacionException {
        return VariablesDAO.mustExecute() && new FacturaDAO().cfdiPublicoEnGeneralVentana();
    }

    public List<Integer> comprobantes() throws FacturacionException {
        return FacturaDAO.loadComprobantePublicoEnGeneral();
    }

    @Override
    public void deslizaVentana() throws FacturacionException {
        LogManager.info("Deslizando ventana de facturación");
        BitacoraDAO.evento("Deslizando ventana de facturación");
        VariablesDAO.slideWindow();
    }
}
