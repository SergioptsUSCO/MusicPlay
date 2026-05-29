ALTER TABLE usuario
ADD COLUMN IF NOT EXISTS usuario_preferencias_configuradas BOOLEAN;

UPDATE usuario
SET usuario_preferencias_configuradas = false
WHERE usuario_preferencias_configuradas IS NULL;

ALTER TABLE usuario
ALTER COLUMN usuario_preferencias_configuradas SET DEFAULT false;

ALTER TABLE usuario
ALTER COLUMN usuario_preferencias_configuradas SET NOT NULL;

CREATE TABLE IF NOT EXISTS usuario_genero_preferido (
    usuario_id BIGINT NOT NULL,
    genero_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, genero_id),
    CONSTRAINT fk_usuario_genero_preferido_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario (usuario_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_usuario_genero_preferido_genero
        FOREIGN KEY (genero_id)
        REFERENCES genero (id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS usuario_artista_preferido (
    usuario_id BIGINT NOT NULL,
    artista_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, artista_id),
    CONSTRAINT fk_usuario_artista_preferido_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario (usuario_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_usuario_artista_preferido_artista
        FOREIGN KEY (artista_id)
        REFERENCES artista (artista_id)
        ON DELETE CASCADE
);
