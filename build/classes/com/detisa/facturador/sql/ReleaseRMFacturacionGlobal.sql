/* 
 * ReserveRMFacturacionGlobal
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since dic 2019 
 */
UPDATE rm
JOIN cli ON rm.cliente = cli.id
JOIN ( 
    SELECT * FROM (
        SELECT IFNULL( ( SELECT valor FROM variables_corporativo WHERE llave = 'fact_global_window' ), 10 ) ventana FROM DUAL ) a
        JOIN ( SELECT IFNULL( ( SELECT TIMESTAMP( valor ) FROM variables_corporativo WHERE llave = 'fact_global_last' ), 10 ) inicio FROM DUAL ) b ON TRUE            
) filtro ON TRUE
SET uuid = CONCAT( 'FPG-', filtro.inicio )
WHERE TRUE
AND uuid = '-----'
AND importe > 0.05
AND tipo_venta = 'D'
AND ( rm.cliente = 0 OR ( cli.tipodepago REGEXP 'Contado|Puntos' ) OR ( cli.tipodepago = 'Tarjeta' AND cli.formadepago REGEXP '01|04|28' ) )
AND fin_venta 
    BETWEEN filtro.inicio 
    AND DATE_ADD( filtro.inicio,  INTERVAL filtro.ventana HOUR )