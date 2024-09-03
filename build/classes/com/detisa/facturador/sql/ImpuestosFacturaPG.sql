/* 
 * FacturaPosicion
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since dic 2019 
 */
SELECT 
    CAST( fcd.iva AS DECIMAL( 11, 6 ) ) TasaOCuota,
    SUM( importeiva ) Importe,
    SUM( fcd.importe - fcd.imported ) Base,
    '002' Impuesto,
    'Tasa' TipoFactor
FROM fc 
JOIN v_fcd fcd USING( id )
WHERE fc.id = ?
GROUP BY fcd.iva
UNION ALL
SELECT 
    CAST( fcd.ieps AS DECIMAL( 11, 6 ) ) TasaOCuota,
    SUM( IF( IFNULL( var.incorporaieps, 'N' ) = 'N', fcd.importeieps, 0.0000 ) ) Importe,
    SUM( IF( IFNULL( var.incorporaieps, 'N' ) = 'N', fcd.cantidad, 0.0000 ) ) Base,
    '003' Impuesto,
    'Cuota' TipoFactor
FROM fc 
JOIN v_fcd fcd USING( id )
JOIN variables var ON TRUE 
WHERE fc.id = ?
GROUP BY fcd.ieps