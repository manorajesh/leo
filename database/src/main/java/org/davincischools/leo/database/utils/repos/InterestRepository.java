package org.davincischools.leo.database.utils.repos;

import org.davincischools.leo.database.daos.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Integer> {}
