/*<<<<<i
 * SmtpVO
 * WSGlobalFAE®
 * © 2018, Detisa 
 * http://www.detisa.com.mx
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since feb 2018
 */
package com.as3.facturador.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class SmtpVO implements Serializable {

    private int id;
    private String server;
    private int port;
    private String sender;
    private boolean auth;
    private boolean secure;
    private String loginuser;
    private String loginpass;
    private int status;
}
