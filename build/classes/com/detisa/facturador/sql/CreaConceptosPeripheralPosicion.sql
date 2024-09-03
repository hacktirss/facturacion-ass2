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
        JOIN cli ON cli.id = vta.cliente
        WHERE vta.id = (  
                SELECT MAX( id )  
                FROM vtaditivos 
                WHERE posicion = ? AND tm = 'C' 
        ) 
        AND vta.uuid = '-----'
        AND vta.cantidad > 0
        AND ( cli.id = 0 OR ( cli.tipodepago REGEXP 'Contado|Tarjeta' AND cli.formadepago REGEXP '01|04|28' ) )
        AND NOT EXISTS ( SELECT fcd.ticket FROM fc JOIN fcd USING( id ) WHERE fc.status NOT REGEXP 'Cancelada' AND (fcd.ticket = vta.id AND fcd.producto = inv.id) )
) fcd