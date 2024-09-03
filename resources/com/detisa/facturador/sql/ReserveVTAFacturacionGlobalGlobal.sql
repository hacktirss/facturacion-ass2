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
JOIN (
    SELECT filtro_global.*, 
            CASE 
                WHEN periodo = '01' THEN fechar
                WHEN periodo = '02' THEN semanaini
                WHEN periodo = '03' THEN quincenaini
                WHEN periodo = '04' THEN mesini
                WHEN periodo = '05' THEN biini
                ELSE CURDATE()
            END finejecucion
    FROM filtro_global    
) f
SET vta.uuid = 
            CASE 
                WHEN periodo = '01' THEN CONCAT( 'FPG-', DATE_FORMAT( vta.fecha, '%Y-%m-%d' ) )
                WHEN periodo = '02' THEN CONCAT( 'FPG-', STR_TO_DATE( CONCAT( DATE_FORMAT( vta.fecha, '%x%v' ), ' Monday' ), '%x%v %W' ), '-', STR_TO_DATE( CONCAT( DATE_FORMAT( vta.fecha, '%x%v' ), ' Sunday' ), '%x%v %W' ) )
                WHEN periodo = '03' THEN 
                                        CASE 
                                                WHEN DAY( vta.fecha ) < 16 THEN
                                                        CONCAT( 'FPG-', DATE_FORMAT( vta.fecha, '%Y-%m-01' ), '-', DATE_FORMAT( vta.fecha, '%Y-%m-15' ) )
                                            ELSE
                                                        CONCAT( 'FPG-', DATE_FORMAT( vta.fecha, '%Y-%m-16' ), '-', DATE_FORMAT( LAST_DAY( vta.fecha ), '%Y-%m-%d' ) )
                                        END                                    
                WHEN periodo = '04' THEN CONCAT( 'FPG-', DATE_FORMAT( vta.fecha, '%Y-%m-01' ), '-', DATE_FORMAT( LAST_DAY( vta.fecha ), '%Y-%m-%d' ) )
                WHEN periodo = '05' THEN
                                        CASE 
                                                WHEN MONTH( vta.fecha )%2 = 0 THEN
                                                        CONCAT( 'FPG-', DATE_FORMAT( DATE_SUB( vta.fecha, INTERVAL 1 MONTH ), '%Y-%m-01' ), '-', DATE_FORMAT( LAST_DAY( vta.fecha ), '%Y-%m-%d' ) )
                                                ELSE
                                                        CONCAT( 'FPG-', DATE_FORMAT( vta.fecha, '%Y-%m-01' ), '-', DATE_FORMAT( LAST_DAY( DATE_ADD( vta.fecha, INTERVAL 1 MONTH ) ), '%Y-%m-%d' ) )
                                        END
            END
WHERE vta.uuid = '-----'
AND vta.fecha BETWEEN f.inicio AND f.finejecucion
AND f.finejecucion > f.inicio
AND vta.cantidad > 0 AND tm = 'C'
AND ( vta.cliente = 0 OR ( cli.tipodepago REGEXP 'Contado|Puntos' ) OR ( cli.tipodepago = 'Tarjeta' AND cli.formadepago REGEXP '01|04|28' ) )
AND NOT EXISTS ( SELECT fc.id, fcd.ticket, inv.id FROM fcd USE INDEX( ticket ) JOIN fc USING( id ) JOIN inv ON fcd.producto = inv.id WHERE fc.status IN ( 0, 1 ) AND fcd.ticket = vta.id AND inv.id = vta.clave )
