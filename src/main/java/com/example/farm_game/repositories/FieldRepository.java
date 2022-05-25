package com.example.farm_game.repositories;

import com.example.farm_game.models.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface FieldRepository extends JpaRepository<Field,Long> {

    String tablesToFindTotal = "WITH NEW_TABLE AS ( " +
            "SELECT FIELDS.ID AS ID, FIELDS.FARM_ID AS farm, CROPS.SELLPRICE as price, " +
            "FIELD_TYPES.SIZE AS size, CROP_SOIL_TYPES.SOIL_TYPES as soil_types, CROP_SOIL_EFFECTS.SOIL_EFFECTS as effects," +
            "FROM FIELDS " +
            "INNER JOIN FIELD_TYPES " +
            "ON FIELDS.FIELD_ID = FIELD_TYPES.ID " +
            "INNER JOIN CROPS " +
            "ON FIELDS.CROP_ID = CROPS.ID " +
            "LEFT JOIN CROP_SOIL_EFFECTS  " +
            "ON CROPS.ID = CROP_SOIL_EFFECTS.CROP_ID " +
            "LEFT JOIN CROP_SOIL_TYPES " +
            "ON FIELDS.CROP_ID = CROP_SOIL_TYPES.SOIL_TYPES " +
            "WHERE (SOIL_TYPES IS NULL AND SOIL_EFFECTS IS NULL) OR NOT  (SOIL_TYPES IS NULL AND SOIL_EFFECTS = 3) " +
            "AND FIELDS.TIMELEFT = 0 AND FIELDS.CROP_ID IS NOT NULL), " +
            "NO_DUPLICATE AS (SELECT " +
            "    ID, farm, price, size, soil_types, MAX(effects) AS max " +
            "FROM " +
            "    NEW_TABLE " +
            "GROUP BY " +
            "    ID " +
            "), " +
            "DOUBLE_YIELDS AS (SELECT * FROM NO_DUPLICATE WHERE MAX = 3), " +
            "HALF_YIELDS AS (SELECT * FROM NO_DUPLICATE WHERE MAX = 2 AND SOIL_TYPES IS NULL), " +
            "STANDARD_YIELD AS (SELECT * FROM NO_DUPLICATE WHERE (SOIL_TYPES IS NULL AND MAX IS NULL) " +
            " OR (MAX IN (0,1,2) OR MAX IS NULL) AND NOT (MAX = 2 AND SOIL_TYPES IS NULL) ) ";

    @Query(value = tablesToFindTotal
            + " SELECT SUM(PRICE*SIZE*2) FROM DOUBLE_YIELDS WHERE FARM = :FARM_ID", nativeQuery = true)
    Integer doubleYieldTotals(@Param("FARM_ID") Long farmID);

    @Query(value = tablesToFindTotal +
            "SELECT ROUND(SUM(PRICE*SIZE*0.5)) FROM HALF_YIELDS WHERE FARM = :FARM_ID", nativeQuery = true)
    Integer halfYieldTotals(@Param("FARM_ID") Long farmID);

    @Query(value = tablesToFindTotal +
            " SELECT SUM(PRICE*SIZE) FROM STANDARD_YIELD WHERE FARM = :FARM_ID",
    nativeQuery = true)
    Integer normalYieldTotals(@Param("FARM_ID") Long farmID);

    @Modifying
    @Query(value = "UPDATE FIELDS SET (CROP_ID, TIMELEFT) = (:CROP_ID, :NEW_TIME) WHERE FIELDS.ID = :FIELD_ID",
            nativeQuery = true)
    @Transactional
    void assignCropToField(@Param("FIELD_ID") Long fieldID, @Param("CROP_ID") Long cropID,
                           @Param("NEW_TIME") int newTime);

    @Query(value = "WITH MONEY_BACK_FROM_FIELD AS ( "
            + "SELECT CROPS.SELLPRICE*FIELD_TYPES.SIZE AS MONEY "
            + "FROM FIELDS INNER JOIN CROPS "
            + "ON CROPS.ID = FIELDS.CROP_ID "
            + "INNER JOIN FIELD_TYPES "
            + "ON FIELDS.FIELD_ID = FIELD_TYPES.ID "
            + "WHERE FIELDS.FARM_ID = :FARM_ID "
            + "AND FIELDS.TIMELEFT = 0 AND FIELDS.CROP_ID IS NOT NULL) " +
            "SELECT SUM(MONEY) FROM MONEY_BACK_FROM_FIELD", nativeQuery = true)
    Integer updateMoneyUponAutomaticSelling(@Param("FARM_ID") Long farmID);

    @Modifying
    @Query(value = "UPDATE FIELDS SET CROP_ID = NULL WHERE FARM_ID = :FARM_ID AND TIMELEFT=0", nativeQuery = true)
    @Transactional
    void emptyFields(@Param("FARM_ID") Long farmID);


}
