/*
 * SmtpDAO
 * WSGlobalFAE®
 * © 2018, Detisa 
 * http://www.detisa.com.mx
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since feb 2018
 */
package com.as3.facturador.dao;

import com.as3.facturador.vo.SmtpVO;
import com.softcoatl.database.mysql.MySQLHelper;
import com.softcoatl.utils.logging.LogManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SmtpDAO {
    
    /**
     * FacturasDAO.getActive
     *  Recupera los datos de facturación asociados a la venta
     * @return Objeto de facturación
     */
    public static SmtpVO getActive() {

        String sql = "SELECT * FROM smtp WHERE smtpvalido = 1";
        SmtpVO smtp = new SmtpVO();

        try (Connection connection = MySQLHelper.getInstance().getConnection();
                Statement stmnt = connection.createStatement();
                ResultSet rs = stmnt.executeQuery(sql)) {

            if (rs.next()) {

                smtp = new SmtpVO();
                smtp.setId(rs.getInt("id"));
                smtp.setServer(rs.getString("smtpname"));
                smtp.setPort(rs.getInt("smtpport"));
                smtp.setSender(rs.getString("smtpsender"));
                smtp.setAuth(rs.getBoolean("smtpauth"));
                smtp.setSecure(rs.getBoolean("smtpsecure"));
                smtp.setLoginuser(rs.getString("smtploginuser"));
                smtp.setLoginpass(rs.getString("smtploginpass"));
                smtp.setStatus(rs.getInt("smtpvalido"));
            }
        } catch (Exception exc) {
            LogManager.error(exc);
        }

        return smtp;
    }//getActive    
}//SmtpDAO
