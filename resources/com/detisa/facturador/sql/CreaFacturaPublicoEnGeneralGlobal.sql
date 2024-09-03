/* 
 * CreaFacturaPublicoEnGeneralGlobal
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since dec 2019 
 */
SET @item=0;
INSERT INTO fc( serie, folio, fecha, cliente, ticket, observaciones, usocfdi, formadepago, uuid, usr, periodo, meses, ano )
SELECT 
    serie.serie,
    IFNULL( ( SELECT MAX( folio ) FROM fc JOIN cia ON TRUE WHERE fc.serie = serie.serie ), 0 ) + @item:=@item+1 folio, 
    NOW(), 0, 0, 
            CASE 
                WHEN periodo = '01' THEN CONCAT( 'Factura Global ', REPLACE( uuid, 'FPG-', '' ) )
                WHEN periodo = '02' THEN CONCAT( 'Factura Global Semana ', REPLACE( uuid, 'FPG-', '' ) )
                WHEN periodo = '03' THEN CONCAT( 'Factura Global Quincena ', REPLACE( uuid, 'FPG-', '' ) )
                WHEN periodo = '04' THEN CONCAT( 'Factura Global Mes ', REPLACE( uuid, 'FPG-', '' ) )
                WHEN periodo = '05' THEN CONCAT( 'Factura Global Bimestre ', REPLACE( uuid, 'FPG-', '' ) )
            END
         observaciones, 
    'P01', '01', vta.uuid, 'WS', f.periodo, DATE_FORMAT( f.inicio, '%m' ), DATE_FORMAT( f.inicio, '%Y' )
FROM ( 
        SELECT uuid FROM rm JOIN com ON com.clavei = rm.producto WHERE uuid LIKE 'FPG-%' 
        UNION DISTINCT 
        SELECT uuid FROM vtaditivos vta JOIN inv ON inv.id = vta.clave WHERE uuid LIKE 'FPG-%'
) vta
JOIN filtro_global f
JOIN ( SELECT valor serie FROM variables_corporativo WHERE llave = 'serie_general' ) serie ON TRUE
WHERE vta.uuid NOT IN ( SELECT uuid FROM fc WHERE uuid LIKE 'FPG-%' AND fc.status IN ( 0, 1 ) );
