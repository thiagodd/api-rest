-- As imagens dos produtos (22.01). O arquivo fica fora do banco, no armazenamento; aqui ficam os metadados e a chave
-- com que o arquivo foi gravado, gerada pelo Bazar e nunca a partir do nome que o cliente mandou.
create table imagem_produto (
    id                 uuid         primary key,
    produto_id         uuid         not null references produto (id),
    chave              varchar(100) not null unique,
    nome_original      varchar(200) not null,
    tipo_conteudo      varchar(50)  not null,
    tamanho_bytes      bigint       not null,
    largura            integer      not null,
    altura             integer      not null,
    texto_alternativo  varchar(300),
    ordem              integer      not null,
    criada_em          timestamptz  not null,
    unique (produto_id, ordem)
);
