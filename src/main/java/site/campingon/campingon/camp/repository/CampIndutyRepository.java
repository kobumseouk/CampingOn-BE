package site.campingon.campingon.camp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.entity.CampInduty;
import site.campingon.campingon.camp.entity.Induty;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampIndutyRepository extends JpaRepository<CampInduty, Long> {
    List<CampInduty> findAllByCamp(Camp camp);
}
