/* 
 * Response
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
package com.as3.facturador.server;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CFDI", propOrder = {
    "valid",
    "folios",
    "pdf",
    "xml",
    "uuid",
    "error"
})
@XmlRootElement(name = "cfdi")
@Data
public class Response {
	
    @XmlElement(name = "valid")
    private boolean valid = true;
    @XmlElement(name = "folios")
    @Getter(AccessLevel.NONE)
    private List<Folio> folios;
    @XmlElement(name = "pdf")
    private byte[] pdf;
    @XmlElement(name = "xml")
    private byte[] xml;
    @XmlElement(name = "uuid")
    private String uuid;
    @XmlElement(name = "error")
    private String error;

    public List<Folio> getFolios() {
        if (folios == null) {
            folios = new ArrayList<>();
        }
        return folios;
    }
}
