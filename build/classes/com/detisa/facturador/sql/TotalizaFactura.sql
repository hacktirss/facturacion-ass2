/* 
 * TotalizaFactura
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since dic 2019 
 */
UPDATE fc
JOIN (
    SELECT 
        fcd.id,
        SUM( ROUND( IF( ABS( fcd.diferencia ) > 0, ( fcd.importe + fcd.diferencia )/( fcd.preciou ), fcd.cantidad ), 2 ) ) cantidad,
        SUM( fcd.importe + fcd.diferencia ) importe, SUM( fcd.descuento ) descuento, 
        SUM( fcd.importeiva ) iva,
        SUM( fcd.importeieps ) ieps,
        SUM( fcd.total ) total
    FROM fc
    JOIN v_fcd fcd USING( id )
    WHERE fc.id = ?
) fcd ON fc.id = fcd.id
SET 
    fc.cantidad = fcd.cantidad,
    fc.importe = fcd.importe,
    fc.descuento = fcd.descuento,
    fc.iva = fcd.iva,
    fc.ieps = fcd.ieps,
    fc.total = fcd.total
WHERE fc.id = ?
