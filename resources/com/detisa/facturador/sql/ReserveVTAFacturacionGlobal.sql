/* 
 * ReserveVTAFacturacionGlobal
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since dic 2019 
 */
UPDATE vtaditivos vta
JOIN cli ON vta.cliente = cli.id
JOIN filtro_global filtro ON TRUE
SET uuid = CONCAT( 'FPG-', filtro.ejecucion )
WHERE uuid = '-----'
AND vta.fecha BETWEEN DATE_SUB( filtro.fechahorar, INTERVAL 1 HOUR ) AND filtro.fechahorar
AND cantidad > 0 AND tm = 'C'
AND ( vta.cliente = 0 OR ( cli.tipodepago REGEXP 'Contado|Puntos' ) OR ( cli.tipodepago = 'Tarjeta' AND cli.formadepago REGEXP '01|04|28' ) )
AND NOT EXISTS ( SELECT fc.id, fcd.ticket, inv.id FROM fcd USE INDEX( ticket ) JOIN fc USING( id ) JOIN inv ON fcd.producto = inv.id WHERE fc.status IN ( 0, 1 ) AND fcd.ticket = vta.id AND inv.id = vta.clave )
