package com.teamproj.backend.Repository.image;

import com.teamproj.backend.model.image.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
