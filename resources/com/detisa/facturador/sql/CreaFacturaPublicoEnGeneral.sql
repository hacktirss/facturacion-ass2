/* 
 * CreaFacturaPublicoEnGeneral
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since dec 2019 
 */
SET @item=0;
INSERT INTO fc( serie, folio, fecha, cliente, ticket, observaciones, usocfdi, formadepago, uuid, usr, periodo, meses, ano )
SELECT 
    serie.serie,
    IFNULL( ( SELECT MAX( folio ) FROM fc JOIN cia ON TRUE WHERE fc.serie = serie.serie ), 0 ) + @item:=@item+1 folio, 
    fecha, cliente, ticket, observaciones, 'P01', fp, uuid, 'WS', '01', DATE_FORMAT( STR_TO_DATE( uuid , 'FPG-%Y-%m-%d %H:%i:%s'), '%m' ), DATE_FORMAT( STR_TO_DATE( uuid , 'FPG-%Y-%m-%d %H:%i:%s'), '%Y' ) 
FROM ( 
            SELECT
                NOW() fecha,
                0 cliente, 
                rm.id ticket,
                CONCAT( 'rm-', rm.id ) observaciones,
                IF( cli.formadepago IS NULL OR cli.formadepago = '', '01', cli.formadepago ) fp,
                rm.uuid
            FROM rm
            JOIN cli ON rm.cliente = cli.id
            WHERE rm.uuid LIKE 'FPG-%'
            AND NOT EXISTS ( SELECT fc.ticket FROM fc WHERE fc.status IN ( 0, 1 ) AND fc.ticket = rm.id AND fc.observaciones = CONCAT('rm-', rm.id ) )
            UNION ALL
            SELECT
                NOW() fecha,
                0 cliente, 
                vta.id ticket,
                CONCAT( 'vta-', vta.id ) observaciones,
                IF( cli.formadepago IS NULL OR cli.formadepago = '', '01', cli.formadepago ) fp,
                vta.uuid
            FROM vtaditivos vta
            JOIN cli ON vta.cliente = cli.id
            WHERE vta.uuid LIKE 'FPG-%'
            AND NOT EXISTS ( SELECT fc.ticket FROM fc WHERE fc.status IN ( 0, 1 ) AND fc.ticket = vta.id AND fc.observaciones = CONCAT('vta-', vta.id ) )
) ventas
JOIN ( SELECT valor serie FROM variables_corporativo WHERE llave = 'serie_general' ) serie;
