/* 
 * FacturaGlobalPeriodo
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
SELECT 
    tipo,
    fecha,
    clave NoIdentificacion,
    inv_cunidad ClaveUnidad,
    inv_cproducto ClaveProdServ,
    CONCAT( com.descripcion, CONCAT( ' Ticket no: ' , fcd.id ) ) Descripcion,
    CAST( cantidad AS DECIMAL( 11, 4 ) ) Cantidad,
    preciou ValorUnitario,
    CAST( cantidad AS DECIMAL( 11, 4 ) ) base_ieps,
    CAST( baseiva AS DECIMAL( 11, 2 ) ) base_iva,
    fcd.factoriva,
    fcd.factorieps,
    CAST( subtotal AS DECIMAL( 11, 2 ) ) Importe,
    CAST( importeiva AS DECIMAL( 11, 2 ) ) tax_iva,
    CAST( importeieps AS DECIMAL( 11, 2 ) ) tax_ieps,
    CAST( total AS DECIMAL( 11, 2 ) ) Total
    FROM (
        SELECT
            'CMB' tipo,
            descripcion,
            COUNT( * ) movimientos,
            cunidad,
            cproducto,
            SUM( IF( ABS( diferencia ) > 0, 
                    ROUND( ( importe + IF( IFNULL( desgloseIEPS, 'S' ) REGEXP 'S', 0.00, importeieps ) + diferencia )/( preciou + IF( IFNULL( desgloseIEPS, 'S' ) REGEXP 'S', 0.0000, ieps ) ), 4 ), cantidad ) ) cantidad,
            preciou + IF( IFNULL( desgloseIEPS, 'S' ) REGEXP 'S', 0.0000, ieps ) preciou,
            CAST( iva AS DECIMAL( 11, 6 ) ) factoriva,
            CAST( ieps AS DECIMAL( 11, 6 ) ) factorieps,
            SUM( importe + IF( IFNULL( desgloseIEPS, 'S' ) REGEXP 'S', 0.00, importeieps ) + diferencia ) subtotal,
            SUM( importe ) baseiva,
            SUM( importeiva ) importeiva,
            SUM( IF( IFNULL( desgloseIEPS, 'S' ) REGEXP 'N', 0.00, importeieps ) ) importeieps,
            total
        FROM (
                SELECT
                        rm.id,
                        rm.fin_venta fecha,
                        producto,
                        inv_cunidad cunidad,
                        inv_cproducto cproducto,
                        com.descripcion,
                        cli.id cliente,
                        cli.desgloseIEPS,
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
                JOIN com ON com.clavei = rm.producto
                JOIN inv ON com.id = inv.id
                JOIN cli ON cli.id = rm.cliente
                WHERE true
                        AND uuid = '-----'
                        AND importe > 0
                        AND tipo_venta = 'D'  
                        AND (cliente = '0' OR cliente = 115)
                        AND cli.tipodepago = 'Contado'
                        AND fin_venta 
                        AND rm.id IN ( 19070, 19071, 19072, 19073, 19074, 19075, 19076, 19077 )
    ) rm
    GROUP BY producto, ieps
) fcd
