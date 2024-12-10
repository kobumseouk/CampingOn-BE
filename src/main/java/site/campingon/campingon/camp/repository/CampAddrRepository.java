package site.campingon.campingon.camp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.campingon.campingon.camp.entity.CampAddr;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampAddrRepository extends JpaRepository<CampAddr, Long> {
    Optional<CampAddr> findByCampId(Long id);

    @Modifying
    @Query(value = "INSERT INTO camp_addr (camp_id, city, state, zipcode, street_addr, detailed_addr, location) " +
            "VALUES (:campId, :city, :state, :zipcode, :streetAddr, :detailedAddr, ST_GeomFromText(:location, 4326))", nativeQuery = true)
    void saveWithPoint(@Param("campId") Long campId,
                       @Param("city") String city,
                       @Param("state") String state,
                       @Param("zipcode") String zipcode,
                       @Param("streetAddr") String streetAddr,
                       @Param("detailedAddr") String detailedAddr,
                       @Param("location") String location);

    @Modifying
    @Query(value = "UPDATE camp_addr " +
            "SET city = :city, state = :state, zipcode = :zipcode, " +
            "street_addr = :streetAddr, detailed_addr = :detailedAddr, " +
            "location = ST_GeomFromText(:location, 4326) " +
            "WHERE camp_id = :campId", nativeQuery = true)
    void updateWithPoint(@Param("campId") Long campId,
                         @Param("city") String city,
                         @Param("state") String state,
                         @Param("zipcode") String zipcode,
                         @Param("streetAddr") String streetAddr,
                         @Param("detailedAddr") String detailedAddr,
                         @Param("location") String location);

    @Query("SELECT c.id FROM Camp c JOIN c.campAddr ad WHERE ad.city LIKE :city")
    List<Long> findCampIdsByCity(@Param("city") String city);
}