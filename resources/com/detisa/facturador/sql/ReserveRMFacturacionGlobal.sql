/* 
 * ReserveRMFacturacionGlobal
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since dic 2019 
 */
UPDATE rm USE INDEX( fechaindex_idx )
JOIN cli ON rm.cliente = cli.id 
JOIN filtro_global filtro ON TRUE
SET uuid = CONCAT( 'FPG-', filtro.ejecucion )
WHERE rm.fecha_venta BETWEEN CAST( DATE_FORMAT( DATE_SUB( filtro.fechahorar, INTERVAL 1 HOUR ), '%Y%m%d' ) AS UNSIGNED ) AND CAST( DATE_FORMAT( filtro.fechahorar, '%Y%m%d' ) AS UNSIGNED )
AND fin_venta BETWEEN DATE_SUB( filtro.fechahorar, INTERVAL 1 HOUR ) AND filtro.fechahorar
AND uuid = '-----'
AND tipo_venta = 'D' AND importe > 0.05
AND ( rm.cliente = 0 OR ( cli.tipodepago REGEXP 'Contado|Puntos' ) OR ( cli.tipodepago = 'Tarjeta' AND cli.formadepago REGEXP '01|04|28' ) )
AND NOT EXISTS ( SELECT fc.id, fcd.ticket, com.clavei FROM fcd USE INDEX( ticket ) JOIN fc USING( id ) JOIN com ON fcd.producto = com.id WHERE fc.status IN ( 0, 1 ) AND fcd.ticket = rm.id AND com.clavei = rm.producto )
