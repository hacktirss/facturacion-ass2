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
    SUM( IF( IFNULL( cli.desgloseIEPS, 'N' ) = 'S', fcd.importeieps, 0.00 ) ) Importe,
    SUM( IF( IFNULL( cli.desgloseIEPS, 'N' ) = 'S', fcd.cantidad, 0.00 ) ) Base,
    '003' Impuesto,
    'Cuota' TipoFactor
FROM fc 
JOIN v_fcd fcd USING( id )
JOIN cli ON fc.cliente = cli.id
WHERE fc.id = ?
GROUP BY fcd.ieps