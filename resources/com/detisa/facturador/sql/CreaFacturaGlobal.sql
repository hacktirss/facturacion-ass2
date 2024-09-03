/* 
 * CreaFacturaGlobal
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
INSERT INTO fc( serie, folio, fecha, cliente, cantidad, importe, iva, ieps, total, uuid, observaciones, usocfdi )
SELECT
    'FG',
    IFNULL( ( SELECT MAX( folio ) FROM fc WHERE serie = 'FG' ), 0 ) + 1,
    NOW(),
    0, 
    IF( ABS( diferencia ) > 0, 
            ROUND( ( importe + IF( IFNULL( incorporaieps, 'N' ) REGEXP 'N', 0.00, importeieps ) + diferencia )/( preciou + IF( IFNULL( incorporaieps, 'N' ) REGEXP 'N', 0.0000, ieps ) ), 4 ), cantidad ),
    importe + IF( IFNULL( incorporaieps, 'N' ) REGEXP 'N', 0.00, importeieps ) + diferencia,
    importeiva,
    IF( IFNULL( incorporaieps, 'N' ) REGEXP 'S', 0.00, importeieps ),
    total,
    '-----',
    'Factura al Público en General',
    'P01'
FROM (
        SELECT
                rm.id,
                rm.fin_venta fecha,
                cli.id cliente,
                variables.incorporaieps,
                ROUND( rm.importe/rm.precio, 4 ) cantidad,
                ROUND( rm.importe, 2 ) total,
                rm.iva,
                ROUND( rm.ieps, 4 ) ieps,
                ROUND( ( rm.precio-rm.ieps )/( 1+rm.iva ), 4 ) preciou,
                ROUND( ( rm.importe/rm.precio ) * ROUND( (rm.precio-rm.ieps )/( 1+rm.iva ), 4 ), 2 ) importe,
                ROUND( ( rm.importe/rm.precio ) * ROUND( (rm.precio-rm.ieps )/( 1+rm.iva ), 4 ) * rm.iva, 2 ) importeiva,
                ROUND( ( rm.importe/rm.precio ) * rm.ieps, 2 ) importeieps,
                ROUND( rm.importe, 2 )
                        -ROUND( ( rm.importe/rm.precio ) * ROUND( (rm.precio-rm.ieps)/(1+rm.iva), 4 ), 2 )
                        -ROUND( ( rm.importe/rm.precio ) * ROUND( (rm.precio-rm.ieps)/(1+rm.iva), 4 ) * rm.iva, 2 )
                        -ROUND( ( rm.importe/rm.precio ) * rm.ieps, 2 ) diferencia
        FROM rm
        JOIN cli ON cli.id = rm.cliente
        JOIN variables ON TRUE
        WHERE true
                AND uuid = '-----'
                AND importe > 0
                AND tipo_venta = 'D'  
                AND (cliente = '0' OR cli.tipodepago = 'Contado') 
                AND rm.id = ?
) rm;

