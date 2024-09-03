/* 
 * CreaConceptos
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since dic 2019 
 */
INSERT INTO fcd( id, producto, cantidad, precio, iva, ieps, importe, descuento, ticket, preciob )
SELECT * 
FROM (
        SELECT
                fc.id,
                com.id producto,
                ROUND( rm.importe/rm.precio, 4 ) cantidad,
                ROUND( ( rm.precio-rm.ieps )/( 1+rm.iva ), 4 ) preciou,
                ROUND( rm.iva, 6 ) iva,
                ROUND( rm.ieps, 6 ) ieps,
                ROUND( rm.importe, 2 ) importe,
                ROUND( rm.descuento, 2 ) descuento,
                rm.id ticket,
                ROUND( rm.precio, 2 ) precio
        FROM rm
        JOIN com ON com.clavei = rm.producto
        JOIN fc ON fc.status = 0 AND fc.uuid = rm.uuid
        WHERE rm.uuid LIKE 'FPG-%'
        AND NOT EXISTS ( SELECT fcd.ticket FROM fc JOIN fcd USING( id ) WHERE fc.status IN ( 0, 1 ) AND fcd.ticket = rm.id AND fcd.producto = com.id )
        UNION ALL 
        SELECT
            fc.id,
            inv.id producto,
            vta.cantidad,
            ROUND( vta.unitario/( 1 + vta.iva ), 2 ) preciou, 
            ROUND( vta.iva, 2 ) iva,
            0.00 ieps,
            ROUND( vta.total, 2 ) importe,
            0.00 descuento,
            vta.id ticket,
            ROUND( vta.unitario, 2 ) precio
        FROM vtaditivos vta 
        JOIN inv ON inv.id = vta.clave
        JOIN fc ON fc.status = 0 AND fc.uuid = vta.uuid
        WHERE vta.uuid LIKE 'FPG-%'
        AND NOT EXISTS ( SELECT fcd.ticket FROM fc JOIN fcd USING( id ) WHERE fc.status IN ( 0, 1 ) AND fcd.ticket = vta.id AND fcd.producto = inv.id )
) conceptos
