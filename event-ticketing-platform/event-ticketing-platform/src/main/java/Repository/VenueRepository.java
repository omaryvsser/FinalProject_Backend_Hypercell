package Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Entity.VenueEntity;

@Repository
public interface VenueRepository extends JpaRepository<VenueEntity, Long> {

    boolean existsByName(String name); // Check if a venue name already exists
    boolean existsByNameAndIdNot(String name, Long id); // Check duplicate name excluding current venue
}
