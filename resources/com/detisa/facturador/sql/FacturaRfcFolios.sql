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
    cunidad ClaveUnidad,
    cproducto ClaveProdServ,
    descripcion Descripcion,
    cantidad Cantidad,
    CAST( subtotal AS DECIMAL( 11, 2 ) ) ValorUnitario,
    CAST( cantidad AS DECIMAL( 11, 4 ) ) base_ieps,
    CAST( baseiva AS DECIMAL( 11, 2 ) ) base_iva,
    factoriva,
    factorieps,
    CAST( subtotal AS DECIMAL( 11, 2 ) ) Importe,
    CAST( importeiva AS DECIMAL( 11, 2 ) ) tax_iva,
    CAST( importeieps AS DECIMAL( 11, 2 ) ) tax_ieps,
    CAST( total AS DECIMAL( 11, 2 ) ) Total
    FROM (
        SELECT
            'CMB' tipo,
            fecha,
            id,
            clave,
            descripcion,
            cunidad,
            cproducto,
            IF( ABS( diferencia ) > 0, 
                    ROUND( ( importe + IF( IFNULL( incorporaieps, 'N' ) REGEXP 'N', 0.00, importeieps ) + diferencia )/( preciou + IF( IFNULL( incorporaieps, 'N' ) REGEXP 'N', 0.0000, ieps ) ), 4 ), cantidad ) cantidad,
            preciou + IF( IFNULL( incorporaieps, 'N' ) REGEXP 'N', 0.0000, ieps ) preciou,
            CAST( iva AS DECIMAL( 11, 6 ) ) factoriva,
            CAST( ieps AS DECIMAL( 11, 6 ) ) factorieps,
            importe + IF( IFNULL( incorporaieps, 'N' ) REGEXP 'N', 0.00, importeieps ) + diferencia subtotal,
            importe baseiva,
            importeiva,
            IF( IFNULL( incorporaieps, 'N' ) REGEXP 'S', 0.00, importeieps ) importeieps,
            total
        FROM (
                SELECT
                        rm.id,
                        com.clave,
                        com.descripcion,
                        inv.inv_cunidad cunidad,
                        inv.inv_cproducto cproducto,
                        rm.fin_venta fecha,
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
                JOIN com ON com.clavei = rm.producto
                JOIN inv ON inv.descripcion = com.descripcion
                JOIN cli ON cli.id = rm.cliente
                JOIN variables ON TRUE
                WHERE true
                        AND uuid = '-----'
                        AND importe > 0
                        AND tipo_venta = 'D'  
                        AND ( ( rfc = ? AND cli.tipodepago = ? ) OR cliente = 0 )
                        AND fin_venta 
                        AND FIND_IN_SET( rm.id, ? )
        ) rm
        UNION ALL
) fcd
