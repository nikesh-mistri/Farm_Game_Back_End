INSERT INTO FARMS (ID, NAME, FUNDS, DATE) VALUES (1, 'Bamfurlong', 200, 0);

INSERT INTO CROPS (ID, NAME, PRICE, STOCK, GROWTIME) VALUES (1, 'Wheat', 20, 300, 20);

INSERT INTO FIELD_TYPES (ID, NAME, SIZE, COST) VALUES (1, 'small field', 2, 10);

INSERT INTO FIELDS (ID, NAME, TIMELEFT, FIELD_ID, CROP_ID, FARM_ID) VALUES (1, 'lower river', 0, 1, 1, 1);