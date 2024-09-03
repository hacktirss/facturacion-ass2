/*
 * Folio
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since nov 2019 
 */
package com.as3.facturador.server;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import lombok.Data;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "tipo",
    "folio",
    "uuid"
})
@Data
public class Folio {
    @XmlElement(name = "tipo")
    private TipoFolio tipo;
    @XmlElement(name = "folio")
    private int folio;
    @XmlElement(name = "uuid")
    private String uuid;

    public Folio() {
    }

    public Folio(TipoFolio tipo, int folio, String uuid) {
        this.tipo = tipo;
        this.folio = folio;
        this.uuid = uuid;
    }

    public static enum TipoFolio {
        RM, VA, OT;
    }
}
