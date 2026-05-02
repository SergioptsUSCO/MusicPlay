package com.musicplay.musicplay.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.Likes;

@Repository
public interface LikesRepo extends JpaRepository<Likes, Long> {

}
