/*
 * Cancelacion
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since dic 2019 
 */
package com.as3.facturador.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import lombok.Data;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AcuseCancelacion", propOrder = {
    "canceled",
    "acuse",
    "error"
})
@XmlRootElement(name = "AcuseCancelacion")
@Data
public class Cancelacion {

    @XmlElement(name = "canceled")
    private boolean canceled;
    @XmlElement(name = "acuse")
    private byte[] acuse;
    @XmlElement(name = "error")
    private String error;

    public Cancelacion() {
    }
    
    public Cancelacion(byte[] acuse) {
        this.canceled = true;
        this.acuse = acuse;
    }
    public Cancelacion(String error) {
        this.canceled = false;
        this.error = error;
    }
}
