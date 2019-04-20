create table lattes
(
	lattes_id varchar(100) not null
		constraint lattes_pkey
			primary key,
	nome_completo varchar(200) not null,
	nome_citacao varchar(2048) not null,
	lattes_resumo text,
	lattes_resumo_en text,
	lattes_atualizacao date,
	especialidade text
)
;

alter table lattes owner to lattes
;

create table atuacao
(
	lattes_id varchar(100) not null
		constraint fk_atuacao
			references lattes
				on delete cascade,
	tipo varchar(30) not null,
	ano_inicio integer,
	ano_fim integer,
	instituicao varchar(512),
	titulo varchar(512),
	titulo_en varchar(512),
	descricao text,
	descricao_en text,
	especialidade text,
	palavras_chave text
)
;

alter table atuacao owner to lattes
;

create table especialidade
(
	lattes_id varchar(100) not null
		constraint fk_especialidade
			references lattes
				on delete cascade,
	especialidade text
)
;

alter table especialidade owner to lattes
;

create table formacao
(
	lattes_id varchar(100) not null
		constraint fk_formacao
			references lattes
				on delete cascade,
	nivel varchar(30) not null,
	ano_inicio integer not null,
	ano_conclusao integer not null,
	titulo varchar(512) not null,
	titulo_en varchar(512),
	especialidade text,
	palavras_chave text
)
;

alter table formacao owner to lattes
;

create table lattes_acoes
(
	lattes_id varchar(100) not null,
	acao varchar(20) not null,
	constraint lattes_acoes_pkey
		primary key (lattes_id, acao)
)
;

alter table lattes_acoes owner to lattes
;

create table lattes_pesos_topicos
(
	lattes_id varchar(100) not null
		constraint fk_lattes_pesos_topicos
			references lattes
				on delete cascade,
	ano integer not null,
	id_topico integer not null,
	peso double precision
)
;

alter table lattes_pesos_topicos owner to lattes
;

create table lattes_topicos
(
	lattes_id varchar(100) not null
		constraint fk_lattes_topicos
			references lattes
				on delete cascade,
	ano integer not null,
	id_topico integer not null,
	peso double precision,
	termos text
)
;

alter table lattes_topicos owner to lattes
;

create table participacao
(
	lattes_id varchar(100) not null
		constraint fk_participacao
			references lattes
				on delete cascade,
	tipo varchar(30) not null,
	ano integer not null,
	titulo varchar(512) not null,
	titulo_en varchar(512),
	descricao text,
	descricao_en text,
	especialidade text,
	palavras_chave text
)
;

alter table participacao owner to lattes
;

create index idx_participacao
	on participacao (lattes_id, titulo, ano, tipo)
;

create table producao
(
	lattes_id varchar(100) not null
		constraint fk_producao
			references lattes
				on delete cascade,
	tipo varchar(30) not null,
	ano integer,
	titulo varchar(512),
	titulo_en varchar(512),
	descricao text,
	descricao_en text,
	especialidade text,
	palavras_chave text
)
;

alter table producao owner to lattes
;

create index idx_producao
	on producao (lattes_id, titulo, ano, tipo)
;

create view vw_producao as
SELECT l.lattes_id, l.nome_completo, producao.titulo, producao.palavras_chave AS descricao, producao.ano
  FROM (producao
      JOIN lattes l ON (((producao.lattes_id) :: text = (l.lattes_id) :: text)))
  UNION ALL
  SELECT l2.lattes_id, l2.nome_completo, atuacao.titulo, atuacao.descricao, atuacao.ano_inicio AS ano
  FROM (atuacao
      JOIN lattes l2 ON (((atuacao.lattes_id) :: text = (l2.lattes_id) :: text)))
  UNION ALL
  SELECT l3.lattes_id, l3.nome_completo, participacao.titulo, participacao.palavras_chave AS descricao, participacao.ano
  FROM (participacao
      JOIN lattes l3 ON (((participacao.lattes_id) :: text = (l3.lattes_id) :: text)))
;

alter table vw_producao owner to lattes
;

