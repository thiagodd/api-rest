-- A partir de 10.03, todo produto nasce com o seu estoque. Os produtos criados antes ganham um estoque zerado.

insert into estoque (produto_id)
select p.id
  from produto p
 where not exists (select 1 from estoque e where e.produto_id = p.id);
