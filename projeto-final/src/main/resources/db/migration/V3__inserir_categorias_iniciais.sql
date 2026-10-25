-- Dados de referência: a árvore de categorias com que o Bazar começa, em todos os ambientes.
-- Dados de teste não entram aqui; cada teste cria os seus.

insert into categoria (id, nome, slug, categoria_pai_id, versao) values
    ('0192f3a0-5555-7000-8000-000000000001', 'Livros',      'livros',      null, 0),
    ('0192f3a0-5555-7000-8000-000000000002', 'Eletrônicos', 'eletronicos', null, 0),
    ('0192f3a0-5555-7000-8000-000000000003', 'Programação', 'programacao', '0192f3a0-5555-7000-8000-000000000001', 0);
