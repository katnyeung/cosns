package org.cosns.dao;

import org.cosns.repository.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageDAO extends JpaRepository<Image, Long> {

}
