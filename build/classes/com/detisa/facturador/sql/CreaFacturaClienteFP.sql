/* 
 * CreaFacturaCliente
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
INSERT INTO fc( serie, folio, fecha, cliente, usocfdi, formadepago, origen, usr )
SELECT
    IFNULL( serie, '' ),
    IFNULL( ( SELECT MAX( folio ) FROM fc WHERE fc.serie = IFNULL( serie, '' ) ), 0 ) + 1,
    NOW(),
    cli.id, 
    'G03',
    ?,
    2,
    ?
FROM cli
JOIN ( SELECT valor serie FROM variables_corporativo WHERE llave = 'serie_contado' ) serie
WHERE cli.id = ?