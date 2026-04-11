package com.nikhil.urlshortener.repository;

import com.nikhil.urlshortener.model.Url;
import com.nikhil.urlshortener.model.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    @Query("SELECT COUNT(DISTINCT v.visitorIp) FROM Visitor v WHERE v.url = :url")
    long countDistinctVisitorIpByUrl(@Param("url") Url url);
}
