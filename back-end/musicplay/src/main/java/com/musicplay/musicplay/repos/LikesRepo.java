package com.musicplay.musicplay.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musicplay.musicplay.modelos.Cancion;
import com.musicplay.musicplay.modelos.Likes;

@Repository
public interface LikesRepo extends JpaRepository<Likes, Long> {

    @Query("select l from Likes l where l.usuario_id = :usuarioId and l.cancion_id = :cancionId")
    Optional<Likes> buscarPorUsuarioYCancion(@Param("usuarioId") Long usuarioId, @Param("cancionId") Long cancionId);

    @Query("select count(l) > 0 from Likes l where l.usuario_id = :usuarioId and l.cancion_id = :cancionId")
    boolean existePorUsuarioYCancion(@Param("usuarioId") Long usuarioId, @Param("cancionId") Long cancionId);

    @Query("select l.cancion_id from Likes l where l.usuario_id = :usuarioId")
    List<Long> buscarCancionesIdsPorUsuario(@Param("usuarioId") Long usuarioId);

    @Query("select l.cancion from Likes l where l.usuario_id = :usuarioId order by l.id desc")
    List<Cancion> buscarCancionesPorUsuario(@Param("usuarioId") Long usuarioId);
}
