package serp.project.first_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

}
