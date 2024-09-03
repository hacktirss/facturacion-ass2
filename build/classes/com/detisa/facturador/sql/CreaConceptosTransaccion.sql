/* 
 * CreaConceptos
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since dic 2019 
 */
INSERT INTO fcd( id, producto, cantidad, precio, iva, ieps, importe, descuento, ticket, preciob )
SELECT ?, fcd.* 
FROM (
        SELECT
                com.id producto,
                ROUND( rm.importe/rm.precio, 4 ) cantidad,
                ROUND( ( rm.precio-rm.ieps )/( 1+rm.iva ), 4 ) preciou,
                ROUND( rm.iva, 6 ) iva,
                ROUND( rm.ieps, 6 ) ieps,
                ROUND( rm.importe, 2 ) importe, ROUND( rm.descuento, 2 ) descuento,
                rm.id ticket,
                ROUND( rm.precio, 2 ) precio
        FROM rm
        JOIN com ON com.clavei = rm.producto
        JOIN cli ON cli.id = rm.cliente
        WHERE rm.id = ? 
        AND rm.uuid = '-----'
        AND rm.importe > 0
        AND rm.tipo_venta = 'D' 
        AND ( cli.id = 0 OR ( cli.tipodepago REGEXP 'Contado|Tarjeta' AND cli.formadepago REGEXP '01|04|28' ) )
        AND NOT EXISTS ( SELECT fcd.ticket FROM fc JOIN fcd USING( id ) WHERE fc.status NOT REGEXP 'Cancelada' AND fcd.ticket = rm.id AND fcd.producto = com.id )
        UNION ALL 
        SELECT
            inv.id producto,
            vta.cantidad,
            ROUND( vta.unitario/( 1 + vta.iva ), 2 ) preciou, 
            ROUND( vta.iva, 2 ) iva,
            0.00 ieps,
            ROUND( vta.total, 2 ) importe, 0.00 descuento,
            vta.id ticket,
            ROUND( vta.unitario, 2 ) precio
        FROM vtaditivos vta 
        JOIN inv ON inv.id = vta.clave
        JOIN rm ON rm.id = vta.referencia 
        JOIN com ON com.clavei = rm.producto
        JOIN cli ON cli.id = rm.cliente
        WHERE rm.id = ? 
        AND vta.uuid = '-----'
        AND rm.uuid = '-----'
        AND ( cli.id = 0 OR ( cli.tipodepago REGEXP 'Contado|Tarjeta' AND cli.formadepago REGEXP '01|04|28' ) )
        AND NOT EXISTS ( SELECT fcd.ticket FROM fc JOIN fcd USING( id ) WHERE fc.status NOT REGEXP 'Cancelada' AND (fcd.ticket = rm.id AND fcd.producto = com.id) OR (fcd.ticket = vta.id AND fcd.producto = inv.id) )
) fcd