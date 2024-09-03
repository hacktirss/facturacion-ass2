/* 
 * CreaDetalleFacturaGlobal
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
INSERT INTO fcd( id, producto, cantidad, precio, iva, ieps, importe, ticket, tipoc, preciob )
SELECT 
    ?,
    producto,
    IF( ABS( diferencia ) > 0, 
            ROUND( ( importe + diferencia )/( preciou ), 4 ), cantidad ) cantidad,
    preciou,
    iva,
    ieps,
    importe,
    folio,
    'I',
    precio
FROM (
        SELECT
                rm.id folio,
                com.id producto,
                ROUND( rm.importe/rm.precio, 4 ) cantidad,
                ROUND( rm.ieps, 4 ) ieps,
                ROUND( rm.iva, 2 ) iva,
                rm.precio,
                ROUND( ( rm.precio-rm.ieps )/( 1+rm.iva ), 4 ) preciou,
                ROUND( ( rm.importe/rm.precio ) * ROUND( (rm.precio-rm.ieps )/( 1+rm.iva ), 4 ), 2 ) importe,
                ROUND( rm.importe, 2 )
                        -ROUND( ( rm.importe/rm.precio ) * ROUND( (rm.precio-rm.ieps)/(1+rm.iva), 4 ), 2 )
                        -ROUND( ( rm.importe/rm.precio ) * ROUND( (rm.precio-rm.ieps)/(1+rm.iva), 4 ) * rm.iva, 2 )
                        -ROUND( ( rm.importe/rm.precio ) * rm.ieps, 2 ) diferencia
        FROM rm
        JOIN com ON com.clavei = rm.producto
        JOIN cli ON cli.id = rm.cliente
        WHERE true
                AND uuid = '-----'
                AND importe > 0
                AND tipo_venta = 'D'  
                AND (cliente = '0' OR cli.tipodepago = 'Contado')
                AND rm.id = ?
) rm
WHERE NOT EXISTS(
     SELECT fc.id FROM fc JOIN fcd ON fc.id = fcd.id JOIN rm ON rm.id = fcd.ticket WHERE rm = ? AND fc.status <> 'Cancelada'
)
