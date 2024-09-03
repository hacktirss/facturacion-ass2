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
SET rm.uuid = 
            CASE 
                WHEN periodo = '01' THEN CONCAT( 'FPG-', DATE_FORMAT( rm.fin_venta, '%Y-%m-%d' ) )
                WHEN periodo = '02' THEN CONCAT( 'FPG-', STR_TO_DATE( CONCAT( DATE_FORMAT( rm.fin_venta, '%x%v' ), ' Monday' ), '%x%v %W' ), '-', STR_TO_DATE( CONCAT( DATE_FORMAT( rm.fin_venta, '%x%v' ), ' Sunday' ), '%x%v %W' ) )
                WHEN periodo = '03' THEN 
                                        CASE 
                                                WHEN DAY( rm.fin_venta ) < 16 THEN
                                                        CONCAT( 'FPG-', DATE_FORMAT( rm.fin_venta, '%Y-%m-01' ), '-', DATE_FORMAT( rm.fin_venta, '%Y-%m-15' ) )
                                            ELSE
                                                        CONCAT( 'FPG-', DATE_FORMAT( rm.fin_venta, '%Y-%m-16' ), '-', DATE_FORMAT( LAST_DAY( rm.fin_venta ), '%Y-%m-%d' ) )
                                        END                                    
                WHEN periodo = '04' THEN CONCAT( 'FPG-', DATE_FORMAT( rm.fin_venta, '%Y-%m-01' ), '-', DATE_FORMAT( LAST_DAY( rm.fin_venta ), '%Y-%m-%d' ) )
                WHEN periodo = '05' THEN
                                        CASE 
                                                WHEN MONTH( rm.fin_venta )%2 = 0 THEN
                                                        CONCAT( 'FPG-', DATE_FORMAT( DATE_SUB( rm.fin_venta, INTERVAL 1 MONTH ), '%Y-%m-01' ), '-', DATE_FORMAT( LAST_DAY( rm.fin_venta ), '%Y-%m-%d' ) )
                                                ELSE
                                                        CONCAT( 'FPG-', DATE_FORMAT( rm.fin_venta, '%Y-%m-01' ), '-', DATE_FORMAT( LAST_DAY( DATE_ADD( rm.fin_venta, INTERVAL 1 MONTH ) ), '%Y-%m-%d' ) )
                                        END
            END
WHERE rm.fecha_venta BETWEEN CAST( DATE_FORMAT( f.inicio, '%Y%m%d' ) AS UNSIGNED ) AND CAST( DATE_FORMAT( f.finejecucion, '%Y%m%d' ) AS UNSIGNED )
AND rm.fin_venta BETWEEN f.inicio AND f.finejecucion 
AND rm.uuid = '-----'
AND rm.tipo_venta = 'D' AND rm.importe > 0.05
AND ( rm.cliente = 0 OR ( cli.tipodepago REGEXP 'Contado|Puntos' ) OR ( cli.tipodepago = 'Tarjeta' AND cli.formadepago REGEXP '01|04|28' ) )
AND NOT EXISTS ( SELECT fc.id, fcd.ticket, com.clavei FROM fcd USE INDEX( ticket ) JOIN fc USING( id ) JOIN com ON fcd.producto = com.id WHERE fc.status IN ( 0, 1 ) AND fcd.ticket = rm.id AND com.clavei = rm.producto )
