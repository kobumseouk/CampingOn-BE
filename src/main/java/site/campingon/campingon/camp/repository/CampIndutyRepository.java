package site.campingon.campingon.camp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.campingon.campingon.camp.entity.CampInduty;

@Repository
public interface CampIndutyRepository extends JpaRepository<CampInduty, Long> {
}
