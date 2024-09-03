package com.as3.facturador.global;

import com.as3.facturador.dao.VariablesDAO;
import com.as3.facturador.exception.FacturacionException;
import java.sql.SQLException;

public abstract class FacturaPublicoGeneralFactory {

    private static final String GLOBAL = "fact_global_global";

    public static FacturaPublicoGeneral instanciate() throws FacturacionException {
        try {
            return "1".equals(VariablesDAO.getVariable(GLOBAL)) ?
                    new Periodo() : new Consumo();
        } catch (SQLException ex) {
            throw new FacturacionException(ex);
        }
    }
}
