CREATE OR REPLACE VIEW `CapitalFlow`
AS
  SELECT
    concat('RL',r.ID) AS `ID`,
    r.USER_ID         AS `USER_ID`,
    r.ID              AS `ORDER_ID`,
    r.CREATETIME      AS `HAPPEN_TIME`,
    0                 AS `TYPE`,
    r.AMOUNT          AS `CHANGED`
  FROM RechargeLog AS r
UNION
  SELECT
    concat('RC',rc.ID) AS `ID`,
    rc.USER_ID         AS `USER_ID`,
    rc.ID              AS `ORDER_ID`,
    rc.USEDTIME        AS `HAPPEN_TIME`,
    1                 AS `TYPE`,
    rc.AMOUNT          AS `CHANGED`
  FROM RechargeCard AS rc
UNION
  SELECT
    concat('RO',ro.ORDERID) AS `ID`,
    ro.PAYER_ID         AS `USER_ID`,
    ro.ORDERID              AS `ORDER_ID`,
    ro.PAYTIME      AS `HAPPEN_TIME`,
    2                AS `TYPE`,
    ro.AMOUNT          AS `CHANGED`
  FROM RechargeOrder AS ro WHERE ORDERSTATUS=2
UNION
  SELECT
    concat('MO',o.ORDERID) AS `ID`,
    o.PAYER_ID         AS `USER_ID`,
    o.ORDERID              AS `ORDER_ID`,
    o.PAYTIME      AS `HAPPEN_TIME`,
    3                AS `TYPE`,
    -o.FINALAMOUNT          AS `CHANGED`
  FROM MainOrder AS o WHERE ORDERSTATUS=2