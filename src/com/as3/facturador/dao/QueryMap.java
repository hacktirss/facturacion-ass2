/*
 * QueryFacturas
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since may 2019 
 */
package com.as3.facturador.dao;

import com.softcoatl.database.entity.dao.BaseDAO;
import com.softcoatl.utils.logging.LogManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QueryMap {

    private final Map<String, String> queries = new HashMap<>();

    public String get(String key) {
        try {
            if (!queries.containsKey(key)) {
                String query = BaseDAO.loadSQLSentenceAsResource(key);
                queries.put(key, query);
            }
            return queries.get(key);
        } catch (IOException ex) {
            LogManager.error(ex);
        }
        return null;
    }
}
