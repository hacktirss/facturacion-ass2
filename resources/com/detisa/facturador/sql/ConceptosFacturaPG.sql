/* 
 * ConceptosFactura
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since dic 2019 
 */
SELECT 
    ( CASE WHEN inv.rubro = 'Combustible' THEN 'CMB' ELSE 'ADT' END ) tipo,
    CONCAT( cia.cre, '-', fcd.ticket ) NoIdentificacion,
    inv.inv_cunidad ClaveUnidad,
    IF( inv.id > 10, '15121500', inv.inv_cproducto ) ClaveProdServ,
    inv.descripcion Descripcion,
    fcd.ticket,
    ROUND( IF( ABS( fcd.diferencia ) > 0, 
                ( fcd.importe + IF( IFNULL( var.incorporaieps, 'N' ) = 'N', 0.0000, fcd.importeieps ) + fcd.diferencia )/
                ( fcd.preciou + IF( IFNULL( var.incorporaieps, 'N' ) = 'N', 0.0000, fcd.ieps ) ), 
        fcd.cantidad ), 6 ) cantidad,
    fcd.preciou + IF( IFNULL( var.incorporaieps, 'N' ) = 'N', 0.0000, fcd.ieps ) ValorUnitario,
    CAST( fcd.iva AS DECIMAL( 11, 6 ) ) factoriva,
    CAST( fcd.ieps AS DECIMAL( 11, 6 ) ) factorieps,
    fcd.importe + IF( IFNULL( var.incorporaieps, 'N' ) = 'N', 0.00, fcd.importeieps ) + fcd.diferencia Importe,
    ROUND( fcd.imported, 2 ) Descuento,
    ROUND( fcd.importe - fcd.imported, 6 ) base_iva,
    fcd.cantidad base_ieps,
    importeiva tax_iva,
    IF( IFNULL( var.incorporaieps, 'N' ) = 'N', fcd.importeieps, 0.00 ) tax_ieps,
    fcd.total
FROM fc 
JOIN v_fcd fcd USING( id )
JOIN variables var ON TRUE
JOIN ( SELECT IFNULL( ( SELECT permiso FROM permisos_cre WHERE catalogo = 'VARIABLES_EMPRESA' AND llave = 'PERMISO_CRE' ), ''  ) cre ) cia ON TRUE
JOIN inv ON fcd.producto = inv.id
WHERE fc.id = ?
